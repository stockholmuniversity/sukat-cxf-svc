package se.su.it.svc.manager

import gldapo.*

class GldapoManager {
  public GldapoManager() {
    System.out.println("Gldapo Init")

    Gldapo.initialize(
      directories: [Directory1:
      [url: "ldap://ldap-test.su.se",
        base: "",
        userDn: "",
        password: "",
        ignorePartialResultException: false,
        env: [
          "java.naming.security.authentication": "GSSAPI",
          "javax.security.sasl.server.authentication": "true"
        ],
        searchControls: [
          countLimit: 500,
          timeLimit: 120000,
          searchScope: "subtree"
        ]
      ]
      ],
      schemas: [se.su.it.svc.ldap.SuPerson,
        se.su.it.svc.ldap.SuOrganization,
        se.su.it.svc.ldap.SuGroup,
        se.su.it.svc.ldap.SuRole,
        se.su.it.svc.ldap.SuCard]
    )
  }
}
