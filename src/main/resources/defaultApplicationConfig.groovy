log {
  debug = "false"
  file = "sukat-cxf-dev.log"
}

'cxf-server' {
  http {
    port = "8080"
  }
  bind {
    address = "127.0.0.1"
  }
  ssl {
    enabled = "false"
    keystore = "cxf-svc-server.keystore"
    password = "changeit"
  }
  spnego {
    conf = "etc/spnego.conf"
    properties = "etc/spnego.properties"
    realm = "SU.SE"
    kdc = "kerberos.su.se"
  }
  spocp {
    enabled = "true"
    server = "spocp-test.su.se"
    port = "4751"
  }
}

/**
*  enrollment.create.skip does the following.
*  true = Create user in SUKAT, DO NOT create in AFS and KDC.
*  false = Create user in SUKAT, AFS and KDC.
*/
enrollment {
  create {
    skip = "true"
  }
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
    url = "ldap://ldap-test.su.se"
  }
  rw {
    name = "ldapreadwrite"
    url = "ldap://sukat-test-ldaprw02.it.su.se"
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

ehcache {
  maxElementsInMemory             = 10000
  eternal                         = false
  timeToIdleSeconds               = 120
  timeToLiveSeconds               = 600
  overflowToDisk                  = false
  diskPersistent                  = false
  diskExpiryThreadIntervalSeconds = 120
  memoryStoreEvictionPolicy       = "LRU"
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
    se.su.it.svc.ldap.SuPersonStub,
    se.su.it.svc.ldap.SuRole,
    se.su.it.svc.ldap.SuCard,
    se.su.it.svc.ldap.SuPerson,
    se.su.it.svc.ldap.SuServiceDescription,
    se.su.it.svc.ldap.SuService,
    se.su.it.svc.ldap.SuSubAccount,
]
