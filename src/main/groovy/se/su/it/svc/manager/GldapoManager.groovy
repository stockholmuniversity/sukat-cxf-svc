package se.su.it.svc.manager

import gldapo.*

class GldapoManager {
  public GldapoManager() {
    System.out.println("Gldapo Init")
    def props = Properties.getInstance().props

    Gldapo.initialize(
      directories: [Directory1:
      [url: props.ldap.server,
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
        se.su.it.svc.ldap.SuCard]
    )
  }
}
