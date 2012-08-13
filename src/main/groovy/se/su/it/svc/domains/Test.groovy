package se.su.it.svc.domains

import grails.persistence.Entity

@Entity
class Test {
  String name
  Date visitdate

  static constraints = {
		name(nullable: false)
		visitdate(nullable: false)
	}
}

