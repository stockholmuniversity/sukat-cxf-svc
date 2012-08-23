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

public class AuditAspect implements MethodInterceptor{
  private static final Logger logger = Logger.getLogger(AuditAspect.class)

  public Object invoke(MethodInvocation invocation) throws Throwable {
    def method = invocation.getMethod()
    System.out.println("JACK ÄR FÖR JÄVLIGT ROLIG")
    logger.debug("JACK ÄR FÖR JÄVLIGT ROLIG")
    invocation.proceed()
  }
}
