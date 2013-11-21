/*
 * Copyright (c) 2013, IT Services, Stockholm University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Stockholm University nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package se.su.it.svc.aspect

import groovy.util.logging.Slf4j
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.apache.cxf.phase.PhaseInterceptorChain

import javax.servlet.http.HttpServletRequest
import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.sql.Timestamp

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-08-21
 * Time: 12:59
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
public class AuditAspect implements MethodInterceptor {
  private static final String STATE_INPROGRESS = "IN PROGRESS"
  private static final String STATE_SUCCESS = "SUCCESS"
  private static final String STATE_EXCEPTION = "EXCEPTION"
  private static final String UNKNOWN = "<unknown>"
  private static final String HIDDEN_VALUE = "******"

  public Object invoke(MethodInvocation invocation) throws Throwable {

    Object rval

    // Log the invocation attempt, keep a reference to the opaque aspect object
    Object auditRef
    Method method = invocation.getMethod()
    try {
      auditRef = logBefore(method, invocation.getArguments())
    } catch (Exception e) {
      log.warn("logBefore failed for " + method.getName() + " - Not proceeding.")
      throw new Exception("Audit Engine is DOWN - not performing any actions at the moment", e)
    }

    // Attempt to execute the method
    try {
      rval = invocation.proceed()
    } catch (Throwable e) {
      if (auditRef != null) {
        logException(auditRef, e)
      }
      log.debug("Method invocation threw exception, re-throwing to caller")
      throw e
    }

    // Add the return value to the aspect log entry
    if (auditRef != null) {
      if (method?.isAnnotationPresent(AuditHideReturnValue)) {
        logAfter(auditRef, HIDDEN_VALUE)
      }
      else {
        logAfter(auditRef, rval)
      }
    }
    // Return the method return value to the caller
    return rval
  }

  protected Object logBefore(Method mi, Object[] args) throws Exception {
    // Surround the aspect logging with a global try/catch so that we can do softFail in a single catch block

    try {

      String id = getId()

      log.info("[$id] Invoked: ${mi.getName()} with ${args.length} params")

      //Generate MethodDetails from annotation on method to be able to describe
      //functions that will be invoked by this method
      List<String> methodDetails = []
      if (mi.isAnnotationPresent(AuditAspectMethodDetails)) {
        Annotation annotation = mi.getAnnotation(AuditAspectMethodDetails)
        def details = annotation.details()?.split(',') ?: new String[0]
        details.each { entry ->
          methodDetails << entry.replace(" ","")
        }
      }

      // Serialize the argument Object list into a ByteArray
      ByteArrayOutputStream bsArgs = new ByteArrayOutputStream()
      ObjectOutputStream outArgs = new ObjectOutputStream(bsArgs)
      outArgs.writeObject(args)
      outArgs.close()

      // Create an AuditEntity based on the gathered information
      AuditEntity ae = AuditEntity.getInstance(
              new Timestamp(new Date().getTime()).toString(),
              mi?.getName(),
              objectToString(args),
              bsArgs.toByteArray().toString(),
              UNKNOWN,
              UNKNOWN,
              STATE_INPROGRESS,
              methodDetails
      )

      //TODO: Call RabbitMQ here to transmit the "aspect before data"
      log.info "[$id] Received: $ae"
      // Return a reference to the ae for reuse in success/exception logs
      return ae

    } catch (Exception e) {
        log.warn("Audit logging failed, catching exception due to SoftFail-mode being set to true", e)
        return null
    }
  }

  protected void logAfter(Object ref, Object ret) {

    try {

      String id = getId()

      AuditEntity ae = (AuditEntity) ref

      //log.info("Decorating ae " + ae + " with return value: " + ret)

      // Serialize the Return object into a ByteArray
      ByteArrayOutputStream bsRet = new ByteArrayOutputStream()
      ObjectOutputStream outRet = new ObjectOutputStream(bsRet)
      outRet.writeObject(ret)
      outRet.close()

      // Append return value to the aspect entity
      ae.text_return = objectToString(ret)
      ae.raw_return = bsRet.toByteArray().toString()
      ae.state = STATE_SUCCESS

      //TODO: Call RabbitMQ here to transmit the "aspect after data"
      log.info("[$id] Returned: $ae")

    } catch (Exception e) {
      log.warn("Audit logging failed, no return value will be stored", e)
    }
  }

  protected void logException(Object ref, Throwable t) {

    try {
      AuditEntity ae = (AuditEntity) ref

      // TBD: Check for cast exception and nullity of ref
      //log.info("Decorating ae " + ae + " with exception " + t.getClass().getCanonicalName())

      // Append return value to the aspect entity
      ae.text_return = t.toString()
      ae.state = STATE_EXCEPTION

      //TODO: Call RabbitMQ here to transmit the "aspect after data"
      log.info("Audit exception occured -\r\n" + ae)

    } catch (Exception e) {
      log.warn("Audit logging of thrown exception failed", e)
    }
  }

  protected String objectToString(Object o) {
    if (o == null) {
      return "null"
    }

    return o.toString()
  }

  protected String objectToString(Object[] o) {
    if (o == null) {
      return "null"
    }

    return '['.plus(o?.collect { it?.toString() }?.join(',')).plus(']')
  }

  protected String getId() {
    String id = ''

    try {
      HttpServletRequest request = (HttpServletRequest) PhaseInterceptorChain.getCurrentMessage().get("HTTP.REQUEST")
      id = request.getSession().id
    } catch (ex) {
      log.debug("Failed to get id from session", ex)
    }

    return id
  }
}
