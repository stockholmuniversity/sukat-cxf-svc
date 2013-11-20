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

import org.apache.log4j.LogManager
import se.su.it.svc.server.annotations.AuthzRole
import se.su.it.svc.util.WebServiceAdminUtils

import javax.jws.WebParam
import javax.jws.WebService
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

@WebService
@AuthzRole(role = "sukat-account-admin")
public class WebServiceAdminImpl implements WebServiceAdmin{
  private static final MappedByteBuffer mem = new RandomAccessFile("/tmp/cxf-server-tmp.txt", "rw").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 1);

  void setRootLogLevel(@WebParam(name = "level") String level) {
    LogManager.getRootLogger().setLevel(WebServiceAdminUtils.getLogLevelFromString(level));
    mem.put(0, (byte)WebServiceAdminUtils.getFunctionByteFromLoggerAndString("root", level));
  }

  void setApplicationLogLevel(@WebParam(name = "level") String level) {
    LogManager.getLogger("se.su.it.svc").setLevel(WebServiceAdminUtils.getLogLevelFromString(level))
    mem.put(0, (byte)WebServiceAdminUtils.getFunctionByteFromLoggerAndString("app", level));
  }

  void setContainerLogLevel(@WebParam(name = "level") String level) {
    LogManager.getLogger("org.eclipse.jetty").setLevel(WebServiceAdminUtils.getLogLevelFromString(level))
    mem.put(0, (byte)WebServiceAdminUtils.getFunctionByteFromLoggerAndString("jetty", level));
  }

  void setCxfLogLevel(@WebParam(name = "level") String level) {
    LogManager.getLogger("org.apache.cxf").setLevel(WebServiceAdminUtils.getLogLevelFromString(level))
    mem.put(0, (byte)WebServiceAdminUtils.getFunctionByteFromLoggerAndString("cxf", level));
  }

  void setSpringLogLevel(@WebParam(name = "level") String level) {
    LogManager.getLogger("org.springframework").setLevel(WebServiceAdminUtils.getLogLevelFromString(level))
    mem.put(0, (byte)WebServiceAdminUtils.getFunctionByteFromLoggerAndString("spring", level));
  }
}
