/*
 * Copyright (c) 2013, IT Services, Stockholm University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of Stockholm University nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package se.su.it.svc.query

import se.su.it.svc.ldap.SuEnrollPerson
import se.su.it.svc.ldap.SuInitPerson
import se.su.it.svc.ldap.SuPerson
import se.su.it.svc.manager.EhCacheManager
import se.su.it.svc.manager.GldapoManager

/**
 * This class is a helper class for doing GLDAPO queries on the SuPerson GLDAPO schema.
 */
public class SuPersonQuery {

  /**
   * the CacheManager provides an instance of EhCache and some overridden methods (get/put/remove)
   * !important: when getting an object from LDAP which is to be changed, we always need to get it from the master,
   *             ie: using the props.ldap.serverrw (readWrite, to ensure that we are changing the up-to-date value)
   *             and NOT fetching the object from the cache.
   */
  def static cacheManager = EhCacheManager.getInstance()

  /**
   * Returns a SuPerson object, specified by the parameter uid.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param uid  the uid (user id) for the user that you want to find.
   * @return an <code><SuPerson></code> or null.
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuPerson getSuPersonFromUID(String directory, String uid) {
    return SuPerson.find(directory: directory, base: "") {
      and {
        eq("uid", uid)
        eq("objectclass", "suPerson")
      }
    }
  }

  /**
   * Returns a SuPerson object, specified by the parameter uid.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param uid  the uid (user id) for the user that you want to find.
   * @return an <code><SuPerson></code> or null.
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuPerson getSuPersonFromNin(String directory, String nin) {
    def query = { qDirectory, qNin ->
      SuPerson.find(directory: qDirectory, base: "") {
        and {
          eq("norEduPersonNIN", qNin)
          eq("objectclass", "suPerson")
        }
      }
    }

    def params = [key: ":getSuPersonFromNin:${nin}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suPerson = (SuPerson) cacheManager.get(params, { query(directory, nin) })

    return suPerson
  }

  /**
   * Returns a SuInitPerson object, specified by the parameter nin.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param nin  the nin (12 digit social security number) for the user that you want to find.
   * @return an <code><SuPerson></code> or null.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuInitPerson getSuInitPersonFromNin(String directory, String nin) {
    def query = { qDirectory, qNin ->
      SuInitPerson.find(directory: qDirectory, base: "") {
        and {
          eq("norEduPersonNIN", qNin)
          eq("objectclass", "person")
        }
      }
    }

    def params = [key: ":getSuInitPersonFromNin:${nin}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suInitPerson = (SuInitPerson) cacheManager.get(params, { query(directory, nin) })

    return suInitPerson
  }

  /**
   * Returns a SuInitPerson object, specified by the parameter ssn.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param ssn  the ssn (social security number) for the user that you want to find.
   * @return an <code><SuPerson></code> or null.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuInitPerson getSuInitPersonFromSsn(String directory, String ssn) {
    def query = { qDirectory, qSsn ->
      SuInitPerson.find(directory: qDirectory, base: "") {
        and {
          eq("socialSecurityNumber", qSsn)
          eq("objectclass", "person")
        }
      }
    }

    def params = [key: ":getSuInitPersonFromSsn:${ssn}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suInitPerson = (SuInitPerson) cacheManager.get(params, { query(directory, ssn) })

    return suInitPerson
  }

  /**
   * Returns a SuPerson object, specified by the parameter ssn.
   *
   * @param directory which directory to use, see GldapoManager.
   * @param ssn  the ssn (social security number) for the user that you want to find.
   * @return an <code><SuPerson></code> or null.
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuPerson getSuPersonFromSsn(String directory, String ssn) {
    def query = { qDirectory, qSsn ->
      SuPerson.find(directory: qDirectory, base: "") {
        and {
          eq("socialSecurityNumber", qSsn)
          eq("objectclass", "person")
        }
      }
    }

    def params = [key: ":getSuPersonFromSsn:${ssn}", ttl: cacheManager.DEFAULT_TTL,
        cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suPerson = (SuPerson) cacheManager.get(params, { query(directory, ssn) })

    return suPerson
  }
  /**
   * Returns a SuEnrollPerson object, specified by the parameter nin.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param nin  the nin (12 digit social security number) for the user that you want to find.
   * @return an <code><SuPerson></code> or null.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuEnrollPerson getSuEnrollPersonFromNin(String directory, String nin) {
    def query = { qDirectory, qNin ->
      SuEnrollPerson.find(directory: qDirectory, base: "") {
        and {
          eq("norEduPersonNIN", qNin)
          eq("objectclass", "person")
        }
      }
    }

    def params = [key: ":getSuEnrollPersonFromNin:${nin}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suEnrollPerson = (SuEnrollPerson) cacheManager.get(params, { query(directory, nin) })

    return suEnrollPerson
  }

  /**
   * Returns a SuEnrollPerson object, specified by the parameter ssn.
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param ssn  the ssn (social security number) for the user that you want to find.
   * @return an <code><SuPerson></code> or null.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static SuEnrollPerson getSuEnrollPersonFromSsn(String directory, String ssn) {
    def query = { qDirectory, qSsn ->
      SuEnrollPerson.find(directory: qDirectory, base: "") {
        and {
          eq("socialSecurityNumber", qSsn)
          eq("objectclass", "person")
        }
      }
    }

    def params = [key: ":getSuEnrollPersonFromSsn:${ssn}", ttl: cacheManager.DEFAULT_TTL, cache: cacheManager.DEFAULT_CACHE_NAME, forceRefresh: (directory == GldapoManager.LDAP_RW)]
    def suEnrollPerson = (SuEnrollPerson) cacheManager.get(params, { query(directory, ssn) })

    return suEnrollPerson
  }

  /**
   * Save a SuPerson object to ldap.
   * and putting the changed object in the cache so that the objects returned by this svc is always up-to-date.
   *
   * @return void.
   * @see se.su.it.svc.ldap.SuPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void saveSuPerson(SuPerson person) {
    person.save()
  }

  /**
   * Init SuPerson entry in sukat
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param suInitPerson a SuInitPerson object to be saved in SUKAT.
   * @return void.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void initSuPerson(String directory, SuInitPerson suInitPerson) {
    suInitPerson.directory = directory
    suInitPerson.save()
  }

  /**
   * Init SuEnrollPerson entry in sukat
   *
   *
   * @param directory which directory to use, see GldapoManager.
   * @param suInitPerson a SuInitPerson object to be saved in SUKAT.
   * @return void.
   * @see se.su.it.svc.ldap.SuEnrollPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void initSuEnrollPerson(String directory, SuEnrollPerson suEnrollPerson) {
    suEnrollPerson.directory = directory
    suEnrollPerson.save()
  }

  /**
   * Save a SuEnrollPerson object to ldap.
   *
   * @return void.
   * @see se.su.it.svc.ldap.SuInitPerson
   * @see se.su.it.svc.manager.GldapoManager
   */
  static void saveSuEnrollPerson(SuEnrollPerson person) {
    person.save()
  }

}
