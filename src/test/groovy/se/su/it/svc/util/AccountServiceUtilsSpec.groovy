package se.su.it.svc.util

import gldapo.GldapoSchemaRegistry
import spock.lang.Specification
import spock.lang.Unroll

class AccountServiceUtilsSpec extends Specification {

  def setup() {
    GldapoSchemaRegistry.metaClass.add = { Object registration -> }
  }

  def cleanup() {
    GeneralUtils.metaClass = null
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

  def 'createSubAccount: happy path'()
  {
        setup:
        GeneralUtils.metaClass.static.execHelper = { String a, String b -> [uid: "${b}", password: "csaP4ssw0rd"]}

        when:
        def ret = AccountServiceUtils.createSubAccount('happy', 'path')

        then:
        ret.uid == "happy/path"
        ret.password == "csaP4ssw0rd"
  }

  def 'deleteSubAccount: happy path'()
  {
        setup:
        GeneralUtils.metaClass.static.execHelper = { String a, String b -> }

        when:
        AccountServiceUtils.deleteSubAccount('happy', 'path')

        then:
        notThrown(Exception)
  }

  def 'getSubAccount: happy path'()
  {
        setup:
        GeneralUtils.metaClass.static.execHelper = { String a, String b -> [uid: 'gsaTest'] }

        when:
        def res = AccountServiceUtils.getSubAccount('happy', 'path')

        then:
        res.uid == 'gsaTest'
  }
}
