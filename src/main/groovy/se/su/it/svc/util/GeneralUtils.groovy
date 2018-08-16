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

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import groovy.transform.Synchronized

import groovy.util.logging.Slf4j

import java.lang.reflect.Modifier

@Slf4j
class GeneralUtils {

  public static final String SU_SE_SCOPE = "@su.se"

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

    /**
     * Execute an external helper and handle errors.
     *
     * @param helper Helper to execute.
     * @param args Arguments.
     *
     * @return Map with command output.
     */
    static Map execHelper(String helper, String args)
    {
        def out = new StringBuffer()

        def cmd = "/local/sukat/libexec/" + helper + " " + args

        def proc = cmd.execute()

        proc.waitForProcessOutput(out, out)

        def json = new JsonSlurper();
        try
        {
            return json.parseText(out.toString());
        }
        catch(ex)
        {
            log.error("${helper}: Execution of external helper failed.")
            log.error("${helper}: ${cmd}")

            log.error("${helper}: --- Begin helper output ---")
            out.eachLine { line ->
                log.error("${helper}: ${line}")
            }
            log.error("${helper}: --- End helper output ---")

            throw ex
        }
    }

    /**
     * Publish a message by writing a file that the daemon will pickup
     *
     * @param message A map with the message
     */
    @Synchronized
    static void publishMessage(Map message)
    {
        def now = new Date()
        def messageId = now.time

        log.info("Publishing message with id ${messageId}")

        def file = "/local/sukat/work/messages/${messageId}.json"

        def fh = new File("${file}.tmp")

        fh.write(JsonOutput.toJson(message))

        // This should be an atomic rename so that the message is ready when the
        // producer sees it.
        fh.renameTo(file)

        // Sleep a millisecond to make sure that the next messageId will be different, requires
        // syncronization.
        Thread.sleep(1)
    }

    /**
     * Qualify a 10 digit ssn with 19 or 20 for century
     *
     * @param ssn The ssn to qualify
     *
     * @return A 12 digit nin
     */
    static String ssnToNin(String ssn)
    {
        // Test- and Archiveaccounts are always in the "year" 2000.
        if (ssn ==~ /^00[0-9].00A\d\d\d$/)
        {
            return "20${ssn}"
        }

        // Use a rolling window in the hope that 100-yearolds are scarce.
        def now = Calendar.getInstance()
        if (ssn.substring(0, 2) > now.format("yy"))
        {
            return "19${ssn}";
        }
        else
        {
            return "20${ssn}";
        }
    }
}

