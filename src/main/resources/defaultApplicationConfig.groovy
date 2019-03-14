customConfig = "/local/sukat/conf/applicationConfig.groovy"

spocp {
  enabled = "true"
  server = "spocp-test.su.se"
  port = "4751"
}

/**
 * Configure which authorization bean to use.
 */
authorizor {
  bean = 'spocpRoleAuthorizor'
}

soap {
  publishedEndpointUrl = "http://localhost:8080"
}

ldap {
  accounts {
    parent = "dc=student,dc=su,dc=se"
  }
  ro {
    name = "ldapreadonly"
  }
  rw {
    name = "ldapreadwrite"
  }
}

sucard {
  database {
    driver = "com.mysql.jdbc.Driver"
    url = "jdbc:mysql://localhost:3306/sucard?autoReconnect=true"
    user = "sucard"
    password = "sucard"
  }
}

sukat {
  database {
    driver = "com.mysql.jdbc.Driver"
    url = "jdbc:mysql://localhost:3306/sukat?autoReconnect=true"
    user = "sukat"
    password = "sukat"
  }
}

directories {
  ldapreadonly {
    url = "ldap://ldap-test.su.se"
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
  ldapreadwrite {
    url = "ldap://sukat-test-ldaprw02.it.su.se"
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
    se.su.it.svc.ldap.Account,
    se.su.it.svc.ldap.GroupOfUniqueNames,
    se.su.it.svc.ldap.NamedObject,
    se.su.it.svc.ldap.SuPersonStub,
    se.su.it.svc.ldap.SuRole,
    se.su.it.svc.ldap.SuCard,
    se.su.it.svc.ldap.SuPerson,
]
