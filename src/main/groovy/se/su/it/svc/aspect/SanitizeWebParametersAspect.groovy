package se.su.it.svc.aspect

import groovy.util.logging.Slf4j
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation

import java.lang.reflect.Method

@Slf4j
class SanitizeWebParametersAspect implements MethodInterceptor {

  @Override
  Object invoke(MethodInvocation methodInvocation) throws Throwable {

    Object response = null

    Method method = methodInvocation.getMethod()
    String methodName = method.name
    Object[] args = methodInvocation.getArguments()
    Object[] washedArgs = []

    try {
      for (arg in args) {
        if (arg instanceof String) {
          String washedArg = washAttribute(arg)
          washedArgs << washedArgs

          if (log.debugEnabled) {
            log.debug("($methodName): sanitized \'$arg\' => \'$washedArg\'")
          }

        } else {
          washedArgs << arg
        }
      }

      response = invokeMethod(method.name, washedArgs)
    } catch (ex) {
      log.error "Failed to sanitize arguments for method $methodName, attributes supplied were: ${args.join(", ")}", ex
    }

    return response
  }

  private static String washAttribute(String arg) {
    return arg?.trim()
  }
}
