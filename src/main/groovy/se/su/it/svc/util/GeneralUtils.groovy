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

package se.su.it.svc.util

import java.lang.reflect.Modifier

class GeneralUtils {

  public static final String SU_SE_SCOPE = "@su.se"

  /**
   * Transform any 12 char pnr to 10 char for use in finding by socialSecurityNumber
   *
   * @param pnr
   * @return
   */
  public static String pnrToSsn(String pnr) {
    return (pnr?.length() == 12) ? pnr[2..11] : pnr
  }

  /**
   * Convert a uid to a principal string
   *
   * @param uid the uid to convert
   * @return <uid>@<SCOPE>
   */
  public static String uidToPrincipal(uid) {
    return uid == null ? null : uid + SU_SE_SCOPE
  }

  /**
   * Transform uid of syntax 'uid.service' to uid/service
   *
   * @param uid the uid to transform
   * @return the transformed uid
   */
  public static String uidToKrb5Principal(String uid) {
    return uid?.replaceFirst("\\.", "/")
  }

  /**
   * Copy properties from one object to another
   *
   * @param source the source object
   * @param target the target object
   */
  public static void copyProperties(GroovyObject source, GroovyObject target) {
    source.metaClass.properties.each { MetaProperty prop ->
      if (target.hasProperty(prop.name) && !(prop.name in ['class', 'metaClass']) &&
              !Modifier.isStatic(prop.modifiers)) {
        target.setProperty(prop.name, source.getProperty(prop.name))
      }
    }
  }
}
