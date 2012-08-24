package se.su.it.svc.audit

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-08-21
 * Time: 12:59
 * To change this template use File | Settings | File Templates.
 */

import org.apache.log4j.Logger
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import java.lang.reflect.Method
import java.sql.Timestamp
import se.su.it.svc.commons.SvcAudit

public class AuditAspect implements MethodInterceptor {
  private static final Logger logger = Logger.getLogger(AuditAspect.class)
  private static final String STATE_INPROGRESS = "IN PROGRESS"
  private static final String STATE_SUCCESS = "SUCCESS"
  private static final String STATE_EXCEPTION = "EXCEPTION"
  private static final String UNKNOWN = "<unknown>"

  public Object invoke(MethodInvocation invocation) throws Throwable {

    Object rval

    // Log the invocation attempt, keep a reference to the opaque audit object
    Object auditRef = null
    try {
      auditRef = logBefore(invocation.getMethod(), invocation.getArguments())
    } catch (Exception e) {
      logger.warn("logBefore failed for " + invocation.getMethod().getName() + " - Not proceeding.")
      throw new Exception("Audit Engine is DOWN - not performing any actions at the moment", e)
    }

    // Attempt to execute the method
    try {
      rval = invocation.proceed()
    } catch (Throwable e) {
      if (auditRef != null) {
        logException(auditRef, e)
      }
      logger.debug("Method invocation threw exception, re-throwing to caller")
      throw e
    }

    // Add the return value to the audit log entry
    if (auditRef != null) {
      logAfter(auditRef, rval)
    }
    // Return the method return value to the caller
    return rval
  }

  protected Object logBefore(Method mi, Object[] args) throws Exception {
    // Surround the audit logging with a global try/catch so that we can do softFail in a single catch block
    try {
      logger.info("Invoked " + mi.getName() + " with " + args.length + " params")

      // Serialize the argument Object list into a ByteArray
      ByteArrayOutputStream bsArgs = new ByteArrayOutputStream()
      ObjectOutputStream outArgs = new ObjectOutputStream(bsArgs)
      outArgs.writeObject(args)
      outArgs.close()

      // Determine uid and ip to use in the audit entry
      String auditIp
      String auditUid
      String auditClient

      int lastArgIdx = args.length - 1
      if (args[lastArgIdx] != null && args[lastArgIdx] instanceof SvcAudit) {
        logger.debug("Found a non-null SvcAudit as last argument to method, extracting uid and ip")
        SvcAudit audit = (SvcAudit) args[lastArgIdx]
        auditIp = audit.getIpAddress()
        auditUid = audit.getUid()
        auditClient = audit.getClient()
      } else {
        logger.warn("No suitable SvcAudit supplied for call to " + mi.getName() +
          " - will not be able to log originator IP/UID.")
        auditIp = UNKNOWN
        auditUid = UNKNOWN
        auditClient = UNKNOWN
      }

      // Create an AuditEntity based on the gathered information
      String ae = "Created: " + new Timestamp(new Date().getTime()).toString() + "\r\n"
      ae += "Ip_address: " + auditIp + "\r\n"
      ae += "Uid: " +  auditUid + "\r\n"
      ae += "Client: " + auditClient + "\r\n"
      ae += "Operation: " + mi.getName() + "\r\n"
      ae += "Text_args: " + objectToString(args) + "\r\n"
      ae += "Raw_args: " + bsArgs.toByteArray().toString() + "\r\n"
      // These will be filled in with actual values by the logAfter method
      ae += "Text_return: " + UNKNOWN + "\r\n"
      ae += "Raw_return: " + UNKNOWN + "\r\n"
      ae += "State: " + STATE_INPROGRESS + "\r\n"
      //TODO: Call RabbitMQ here to transmit the "audit before data"

      // Return a reference to the ae for reuse in success/exception loggers
      return ae

    } catch (Exception e) {
        logger.warn("Audit logging failed, catching exception due to SoftFail-mode being set to true", e)
        return null
    }
  }

  protected void logAfter(Object ref, Object ret) {

    try {
      String ae = (String) ref

      logger.info("Decorating ae " + ae + " with return value: " + ret)

      // Serialize the Return object into a ByteArray
      ByteArrayOutputStream bsRet = new ByteArrayOutputStream()
      ObjectOutputStream outRet = new ObjectOutputStream(bsRet)
      outRet.writeObject(ret)
      outRet.close()

      // Append return value to the audit entity
      ae += "Text_return: " + objectToString(ret) + "\r\n"
      ae += "Raw_return: " + bsRet.toByteArray().toString() + "\r\n"
      ae += "State: " + STATE_SUCCESS + "\r\n"

      //TODO: Call RabbitMQ here to transmit the "audit after data"

    } catch (Exception e) {
      logger.warn("Audit logging failed, no return value will be stored", e)
    }
  }

  protected void logException(Object ref, Throwable t) {

    try {
      String ae = (String) ref

      // TBD: Check for cast exception and nullity of ref
      logger.info("Decorating ae " + ae + " with exception " + t.getClass().getCanonicalName())

      // Append return value to the audit entity
      ae += "Text_return: " + t.toString() + "\r\n"
      ae += "State: " + STATE_EXCEPTION + "\r\n"

      //TODO: Call RabbitMQ here to transmit the "audit after data"

    } catch (Exception e) {
      logger.warn("Audit logging of thrown exception failed", e)
    }
  }

  protected String objectToString(Object o) {
    if (o == null)
      return "null"

    return o.toString()
  }

  protected String objectToString(Object[] o) {
    if (o == null)
      return "null"

    StringBuilder strBuilder = new StringBuilder()
    strBuilder.append("[")
    for (Object obj : o) {
      strBuilder.append(objectToString(obj))
      strBuilder.append(",")
    }
    strBuilder.deleteCharAt(strBuilder.length() - 1)
    strBuilder.append("]")
    return strBuilder.toString()

  }
}
