package se.su.it.svc

import javax.jws.WebService
import javax.jws.WebParam
import se.su.it.svc.annotations.*
import se.su.it.svc.domains.*
import se.su.it.svc.manager.ApplicationContextProvider
import se.su.it.svc.ldap.SuPerson
import org.springframework.core.io.Resource
import se.su.it.svc.commons.SvcStatus

@WebService
public class Status {
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
