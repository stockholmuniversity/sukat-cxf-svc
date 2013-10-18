package se.su.it.svc.aspect

import groovy.util.logging.Slf4j
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation

import java.lang.reflect.Method

@Slf4j
class SanitizeWebParametersAspect implements MethodInterceptor {

  @Override
  Object invoke(MethodInvocation methodInvocation) throws Throwable {
    Method method = methodInvocation.getMethod()
    Object[] args = methodInvocation.getArguments()
    Object caller = methodInvocation.getThis()

    Object[] washedArgs = args
    try {
      washedArgs = washArgs(args)
    } catch (ex) {
      log.error "Failed to sanitize arguments for method ${method.name}, attributes supplied were: ${args.join(", ")}", ex
    }

    return caller.invokeMethod(method.name, washedArgs)
  }

  private static Object[] washArgs(Object[] args) {
    List washedArgs = []

    for (arg in args) {
      if (arg instanceof String) {
        washedArgs << arg?.trim()
      } else {
        washedArgs << arg
      }
    }
    return washedArgs.toArray()
  }
}
