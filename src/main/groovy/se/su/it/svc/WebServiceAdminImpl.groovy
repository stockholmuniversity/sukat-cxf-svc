package se.su.it.svc

import javax.jws.WebService
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import se.su.it.svc.util.WebServiceAdminUtils
import javax.jws.WebParam
import se.su.it.svc.commons.SvcAudit

@WebService
public class WebServiceAdminImpl implements WebServiceAdmin{
  private static final Logger logger = Logger.getLogger(WebServiceAdminImpl.class)

  void setRootLogLevel(@WebParam(name = "level") String level, @WebParam(name = "audit") SvcAudit audit) {
    LogManager.getRootLogger().setLevel(WebServiceAdminUtils.getLogLevelFromString(level));
  }

  void setApplicationLogLevel(@WebParam(name = "level") String level, @WebParam(name = "audit") SvcAudit audit) {
    LogManager.getLogger("se.su.it.svc").setLevel(WebServiceAdminUtils.getLogLevelFromString(level))
  }

  void setContainerLogLevel(@WebParam(name = "level") String level, @WebParam(name = "audit") SvcAudit audit) {
    LogManager.getLogger("org.eclipse.jetty").setLevel(WebServiceAdminUtils.getLogLevelFromString(level))
  }

  void setCxfLogLevel(@WebParam(name = "level") String level, @WebParam(name = "audit") SvcAudit audit) {
    LogManager.getLogger("org.apache.cxf").setLevel(WebServiceAdminUtils.getLogLevelFromString(level))
  }

  void setSpringLogLevel(@WebParam(name = "level") String level, @WebParam(name = "audit") SvcAudit audit) {
    LogManager.getLogger("org.springframework").setLevel(WebServiceAdminUtils.getLogLevelFromString(level))
  }
}
