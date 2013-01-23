package se.su.it.svc.manager

import gldapo.*
import org.apache.log4j.Logger
/**
 * This class specifies the ldap integration<br />
 * We have 2 directories that use different sukat servers.<br />
 * LDAP_RO are the slave servers and we use them to do searches and queries.<br />
 * LDAP_RW is the master server, we use this one for modifications of data on sukat.<br />
 */
public class GldapoManager {
  private static final Logger logger = Logger.getLogger(GldapoManager.class)
  public static final String LDAP_RO = "ldapreadonly"
  public static final String LDAP_RW = "ldapreadwrite"

  public GldapoManager() {
    logger.info("Gldapo Init")
    def props = Properties.getInstance().props

    Gldapo.initialize(
      directories: [(this.LDAP_RO):
      [url: props.ldap.serverro,
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
      ],(this.LDAP_RW):
        [url: props.ldap.serverrw,
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
                se.su.it.svc.ldap.SuRole,
                se.su.it.svc.ldap.SuCard,
                se.su.it.svc.ldap.SuServiceDescription,
                se.su.it.svc.ldap.SuService,
                se.su.it.svc.ldap.SuSubAccount]
    )
  }
}
