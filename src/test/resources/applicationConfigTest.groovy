soap {
  publishedEndpointUrl = "localhost"
}

ldap {
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