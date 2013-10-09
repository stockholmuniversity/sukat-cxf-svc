import se.su.it.svc.ldap.SuCard
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.ldap.SuPersonStub
import se.su.it.svc.ldap.SuRole
import se.su.it.svc.ldap.SuService
import se.su.it.svc.ldap.SuServiceDescription
import se.su.it.svc.ldap.SuSubAccount

//soap {
//  publishedEndpointUrl = "http://localhost:80"
//}

ldap {
  ro {
    name = "ldapreadonly"
    url = "ldap://ldap-test.su.se"
  }
  rw {
    name = "ldapreadwrite"
    url = "ldap://sukat-test-ldaprw02.it.su.se"
  }
}

sucard {
  database {
    driver = ""
    url = ""
    password = ""
    user = ""
  }
}

directories {
  "${ldap.ro.name}" {
    url = "${ldap.ro.url}"
    base = ""
    userDn = ""
    password = ""
    ignorePartialResultException = false
    env = [
        "java.naming.security.authentication": "GSSAPI",
        "javax.security.sasl.server.authentication": "true"
    ]
    searchControls {
      countLimit = 500
      timeLimit = 120000
      searchScope = "subtree"
    }
  }
  "${ldap.rw.name}" {
    url = "${ldap.rw.url}"
    base = ""
    userDn = ""
    password = ""
    ignorePartialResultException = false
    env = [
        "java.naming.security.authentication": "GSSAPI",
        "javax.security.sasl.server.authentication": "true"
    ]
    searchControls {
      countLimit = 500
      timeLimit = 120000
      searchScope = "subtree"
    }
  }
}
schemas = [
    SuPersonStub,
    SuRole,
    SuCard,
    SuPerson,
    SuServiceDescription,
    SuService,
    SuSubAccount,
]
