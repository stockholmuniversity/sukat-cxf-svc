package se.su.it.svc

enrollment {
  create {
    skip = "true"
  }
}

soap {
  publishedEndpointUrl = "localhost"
}

ldap {
  accounts {
    parent = "foo"
  }
  ro {
    name = "ldapreadonly"
    url = "ldap://ldap-test.su.se"
  }
  rw {
    name = "ldapreadwrite"
    url = "ldap://ldap-test.su.se"
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
