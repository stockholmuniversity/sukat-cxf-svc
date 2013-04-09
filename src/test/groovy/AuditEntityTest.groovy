import se.su.it.svc.audit.AuditEntity
import spock.lang.Specification

class AuditEntityTest extends Specification {
  def "Constructor: Test constructor"() {
    when: 'Constructor should never be called directly, thus private.'
    AuditEntity auditEntity = new AuditEntity()

    then:
    auditEntity.created       == null
    auditEntity.ip_address    == null
    auditEntity.uid           == null
    auditEntity.client        == null
    auditEntity.operation     == null
    auditEntity.text_args     == null
    auditEntity.raw_args      == null
    auditEntity.text_return   == null
    auditEntity.raw_return    == null
    auditEntity.state         == null
    auditEntity.methodDetails == null
  }

  def "getInstance: Test factory method"() {
    when:
    AuditEntity auditEntity = AuditEntity.getInstance('1','2','3','4','5','6','7','8','9','10',['11','12'])

    then:
    auditEntity.created       == '1'
    auditEntity.ip_address    == '2'
    auditEntity.uid           == '3'
    auditEntity.client        == '4'
    auditEntity.operation     == '5'
    auditEntity.text_args     == '6'
    auditEntity.raw_args      == '7'
    auditEntity.text_return   == '8'
    auditEntity.raw_return    == '9'
    auditEntity.state         == '10'
    auditEntity.methodDetails == ['11','12']
  }
  def "test toString"() {
    given:
    AuditEntity auditEntity = AuditEntity.getInstance('1','2','3','4','5','6','7','8','9','10',['11','12'])

    expect:
    auditEntity.toString() == "se.su.it.svc.audit.AuditEntity(created:1, ip_address:2, uid:3, client:4, operation:5, text_args:6, text_return:8, state:10, methodDetails:[11, 12])"
  }
}
