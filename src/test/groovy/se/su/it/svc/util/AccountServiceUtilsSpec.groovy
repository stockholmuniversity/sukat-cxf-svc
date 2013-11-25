package se.su.it.svc.util

import gldapo.GldapoSchemaRegistry
import spock.lang.Specification
import spock.lang.Unroll

class AccountServiceUtilsSpec extends Specification {

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
  }

  def cleanup() {
    GldapoSchemaRegistry.metaClass = null
  }

  @Unroll
  def "domainToDN returns dn='#dn' for domain='#domain'"() {
    expect: AccountServiceUtils.domainToDN(domain) == dn

    where:
    domain     | dn
    null       | ''
    ''         | ''
    'se'       | 'dc=se'
    'su.se'    | 'dc=su,dc=se'
    'it.su.se' | 'dc=it,dc=su,dc=se'
  }

  def "domainToDN handles null"() {
    expect: AccountServiceUtils.domainToDN(null) == ''
  }
}
