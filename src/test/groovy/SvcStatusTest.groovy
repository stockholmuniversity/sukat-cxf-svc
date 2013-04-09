import se.su.it.svc.commons.SvcStatus
import spock.lang.Specification

class SvcStatusTest extends Specification {
  def "Test attributes"() {
    expect:
    new SvcStatus().properties.keySet().containsAll(
        ['sname', 'class', 'buildtime', 'sbuildtime', 'version', 'serialVersionUID', 'metaClass', 'name', 'sversion']
    )
  }
}
