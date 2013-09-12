package se.su.it.svc.aspect

import org.aopalliance.intercept.MethodInvocation
import spock.lang.Specification

import java.lang.reflect.Method

class SanitizeWebParametersAspectSpec extends Specification {
  def setup() {}
  def cleanup() {}

  def "invoke: Test method invocation override."() {
    given:
    Object[] args = ["foo ", "kaka", new Object()]

    SanitizeWebParametersAspect wspa = GroovySpy(SanitizeWebParametersAspect)
    MethodInvocation methodInvocation = GroovyMock(MethodInvocation) {
      1 * getMethod(*_) >> {
        Method
        return GroovyMock(Method) {
          getName() >> "superiorMethod"
        }
      }
      1 * getArguments(*_) >> {
        return args
      }
    }
    when:
    def resp = wspa.invoke(methodInvocation)

    then:
    resp == null
  }

  def "washArgs: Test that Strings are properly trimmed and washed."() {
    given:
    Object[] args = ["foo", "  bar  ", new Object()]

    when:
    def resp = SanitizeWebParametersAspect.washArgs(args)

    then:
    resp[0] == "foo"
    resp[1] == "bar"
  }
}
