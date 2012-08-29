package se.su.it.svc.domains

import grails.persistence.Entity
/**
 * This is a test domain class for the grails gorm implementation in this application context
 */
@Entity
class Test {
  String name
  Date visitdate

  static constraints = {
		name(nullable: false)
		visitdate(nullable: false)
	}
}

