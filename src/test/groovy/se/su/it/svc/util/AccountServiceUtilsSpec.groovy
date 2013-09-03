package se.su.it.svc.util

import spock.lang.Specification
import spock.lang.Unroll

class AccountServiceUtilsSpec extends Specification {

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
}
