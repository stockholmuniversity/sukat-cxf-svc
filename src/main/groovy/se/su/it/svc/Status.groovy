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

package se.su.it.svc

import org.springframework.core.io.Resource
import se.su.it.svc.commons.SvcStatus
import se.su.it.svc.manager.ApplicationContextProvider

import javax.jws.WebService

/**
 * Implementing class for Status CXF Web Service.
 * This Class handles all Status information for this web service application.
 */
@WebService
public class Status {
  /**
   * Returns a SvcStatus object with status information regarding the web service application and server.
   *
   *
   * @return an SvcStatus object.
   * @see se.su.it.svc.commons.SvcStatus
   */
  public SvcStatus getStatus() {
    Resource me       = ApplicationContextProvider.getApplicationContext().getResource("WEB-INF/classes/version.properties")
    Resource server   = ApplicationContextProvider.getApplicationContext().getResource("version.properties")
    Properties versionProps = new Properties();
    SvcStatus svcs    = new SvcStatus()
    try {
      versionProps.load(me.inputStream)
      svcs.name = versionProps.getProperty("project.name");
      svcs.version = versionProps.getProperty("project.version");
      svcs.buildtime = versionProps.getProperty("project.builddate");
    } catch (Exception e) {
      System.out.println("Warning could not load version.properties. " + e.getMessage())
    }
    try {
      versionProps.load(server.inputStream)
      svcs.sname = versionProps.getProperty("project.name");
      svcs.sversion = versionProps.getProperty("project.version");
      svcs.sbuildtime = versionProps.getProperty("project.builddate");
    } catch (Exception e) {
      System.out.println("Warning could not load server version.properties. " + e.getMessage())
    }

    return svcs
  }
}
