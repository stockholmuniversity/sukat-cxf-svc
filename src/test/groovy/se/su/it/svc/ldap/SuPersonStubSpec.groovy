package se.su.it.svc.ldap

import spock.lang.Specification

class SuPersonStubSpec extends Specification {

  def setup() {
    SuPersonStub.metaClass.parent = ""
    SuPersonStub.metaClass.directory = ""
  }
  def cleanup() {
    SuPersonStub.metaClass = null
  }

  def "test custom constructor(attrs...) "() {
    given:

    String uid = "a"
    Set objectClass = ['suPerson', 'sSNObject', 'inetOrgPerson']
    String socialSecurityNumber = "c"
    String sn = "d"
    String givenName = "e"
    String parent = "g"
    String directory = "h"
    String displayName = givenName + " " + sn
    String cn = displayName

    when:
    SuPersonStub stub = new SuPersonStub(uid, givenName, sn, socialSecurityNumber, parent, directory)

    then:
    stub.uid == uid
    stub.givenName == givenName
    stub.sn == sn
    stub.socialSecurityNumber == socialSecurityNumber
    stub.displayName == displayName
    stub.cn == cn
    stub.objectClass == objectClass
    stub.parent == parent
    stub.directory == directory
  }

  def "test newInstance 6 String argument invocation."() {
    when:
    def resp = SuPersonStub.newInstance("-", "-", "-", "-", "-", "-")

    then:
    resp instanceof SuPersonStub

    and:
    resp.objectClass.containsAll(['suPerson', 'sSNObject', 'inetOrgPerson'])
  }
}
