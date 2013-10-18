package se.su.it.svc.aspect

import org.aopalliance.intercept.MethodInvocation
import spock.lang.Specification

class SanitizeWebParametersAspectSpec extends Specification {
  def setup() {}
  def cleanup() {}

  private class DummyClass {
    public String foobar(String foo) {
      return foo
    }
  }

  def "invoke: Test method invocation override."() {
    given:
    Object[] args = ["foo"]
    def caller  = new DummyClass()

    SanitizeWebParametersAspect wspa = GroovySpy(SanitizeWebParametersAspect)
    MethodInvocation methodInvocation = GroovyMock(MethodInvocation) {
      1* getMethod() >> {
        return caller.class.getMethod("foobar", String)
      }
      1 * getArguments() >> {
        return args
      }
      1 * getThis() >> {
        return caller
      }
    }
    when:
    def resp = wspa.invoke(methodInvocation)

    then:
    resp == 'foo'
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
