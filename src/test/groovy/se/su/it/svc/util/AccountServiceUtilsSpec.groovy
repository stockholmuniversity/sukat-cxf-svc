package se.su.it.svc.util

import se.su.it.svc.query.AccountQuery
import se.su.it.svc.query.SuPersonQuery

import spock.lang.Specification
import spock.lang.Unroll

class AccountServiceUtilsSpec extends Specification
{
    def cleanup()
    {
        AccountQuery.metaClass = null
        GeneralUtils.metaClass = null
        SuPersonQuery.metaClass = null
    }

    def "generateUID: correct generation of uid"()
    {
        setup:
        AccountQuery.metaClass.static.findAccountByUid = { String d, String uid -> }
        SuPersonQuery.metaClass.static.findSuPersonByUID = { String d, String uid -> }

        when:
        def uid = AccountServiceUtils.generateUid('Magnus', 'Svensson')

        then:
        uid.length() == 8
        uid.substring(0, 4) ==~ /^masv$/
        uid.substring(4) ==~ /^[0-9]{4}$/
    }

    @Unroll
    def "generateUID: correct generation of prefix for #givenName #sn"()
    {
        setup:
        AccountQuery.metaClass.static.findAccountByUid = { String d, String uid -> }
        SuPersonQuery.metaClass.static.findSuPersonByUID = { String d, String uid -> }

        expect:
        AccountServiceUtils.generateUid(givenName, sn).substring(0, 4) == pfx

        where:
        givenName | sn         | pfx
        'Kalle'   | 'Karlsson' | 'kaka'
        'Anna'    | 'Aldin'    | 'aald' // Avoid bad words such as 'anal'
        'Åke'     | 'Åkesson'  | 'akak' // Diacritics should be stripped
        'Dash'    | 'A-Dash'   | 'daad' // '-' should be stripped
        'Nils'    | "O'Neil"   | 'nion' // "'" should be stripped
    }

    def "generateUID: an account is found"()
    {
        setup:
        int i = 0
        AccountQuery.metaClass.static.findAccountByUid = { String d, String uid -> i++; i == 1 ? [uid: 'masv0000'] : null }
        SuPersonQuery.metaClass.static.findSuPersonByUID = { String d, String uid -> }

        when:
        def uid = AccountServiceUtils.generateUid('Magnus', 'Svensson')

        then:
        uid.substring(0, 4) ==~ /^masv$/
        i == 2
    }

    def "generateUID: a person is found"()
    {
        setup:
        AccountQuery.metaClass.static.findAccountByUid = { String d, String uid -> }
        int i = 0
        SuPersonQuery.metaClass.static.findSuPersonByUID = { String d, String uid -> i++; i == 1 ? [uid: 'masv0000'] : null }

        when:
        def uid = AccountServiceUtils.generateUid('Magnus', 'Svensson')

        then:
        uid.substring(0, 4) ==~ /^masv$/
        i == 2
    }

    def "generateUID: An invalid uid is generated"()
    {
        setup:
        AccountQuery.metaClass.static.findAccountByUid = { String d, String uid -> }
        SuPersonQuery.metaClass.static.findSuPersonByUID = { String d, String uid -> }

        when:
        def uid = AccountServiceUtils.generateUid('M=', 'S=')

        then:
        thrown(RuntimeException)
    }

    def "generateUID: no uid can be generated"()
    {
        setup:
        AccountQuery.metaClass.static.findAccountByUid = { String d, String uid -> [uid: "masv0000"] }

        when:
        def uid = AccountServiceUtils.generateUid('Magnus', 'Svensson')

        then:
        thrown(RuntimeException)
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
