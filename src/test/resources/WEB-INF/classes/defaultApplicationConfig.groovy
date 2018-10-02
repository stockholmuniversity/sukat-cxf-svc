test = "test"

customConfig = ""

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
  }
  rw {
    name = "ldapreadwrite"
  }
}

sucard {
  database {
    driver = "com.mysql.jdbc.Driver"
    url = "jdbc:mysql://localhost:3306/sucard?autoReconnect=true"
    user = ""
    password = ""
  }
}

sukat {
  database {
    driver = "com.mysql.jdbc.Driver"
    url = "jdbc:mysql://localhost:3306/sukat?autoReconnect=true"
    user = ""
    password = ""
  }
}
