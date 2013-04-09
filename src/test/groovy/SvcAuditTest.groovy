import se.su.it.svc.commons.SvcAudit
import spock.lang.Specification

class SvcAuditTest extends Specification {
  def "Test attributes"() {
    expect:
    new SvcAudit().properties.keySet().containsAll(['class', 'uid', 'client', 'ipAddress', 'serialVersionUID', 'metaClass'])
  }
}
