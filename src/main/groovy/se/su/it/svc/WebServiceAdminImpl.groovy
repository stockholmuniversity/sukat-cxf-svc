package se.su.it.svc

import javax.jws.WebService
import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.log4j.LogManager
import se.su.it.svc.util.WebServiceAdminUtils
import javax.jws.WebParam
import se.su.it.svc.commons.SvcAudit
import java.nio.channels.FileChannel
import java.nio.MappedByteBuffer

@WebService
public class WebServiceAdminImpl implements WebServiceAdmin{
  private static final Logger logger = Logger.getLogger(WebServiceAdminImpl.class)
  private static final MappedByteBuffer mem = new RandomAccessFile("/tmp/cxf-server-tmp.txt", "rw").getChannel().map(FileChannel.MapMode.READ_WRITE, 0, 1);

  void setRootLogLevel(@WebParam(name = "level") String level, @WebParam(name = "audit") SvcAudit audit) {
    LogManager.getRootLogger().setLevel(WebServiceAdminUtils.getLogLevelFromString(level));
    mem.put(0, (byte)WebServiceAdminUtils.getFunctionByteFromLoggerAndString("root", level));
  }

  void setApplicationLogLevel(@WebParam(name = "level") String level, @WebParam(name = "audit") SvcAudit audit) {
    LogManager.getLogger("se.su.it.svc").setLevel(WebServiceAdminUtils.getLogLevelFromString(level))
    mem.put(0, (byte)WebServiceAdminUtils.getFunctionByteFromLoggerAndString("app", level));
  }

  void setContainerLogLevel(@WebParam(name = "level") String level, @WebParam(name = "audit") SvcAudit audit) {
    LogManager.getLogger("org.eclipse.jetty").setLevel(WebServiceAdminUtils.getLogLevelFromString(level))
    mem.put(0, (byte)WebServiceAdminUtils.getFunctionByteFromLoggerAndString("jetty", level));
  }

  void setCxfLogLevel(@WebParam(name = "level") String level, @WebParam(name = "audit") SvcAudit audit) {
    LogManager.getLogger("org.apache.cxf").setLevel(WebServiceAdminUtils.getLogLevelFromString(level))
    mem.put(0, (byte)WebServiceAdminUtils.getFunctionByteFromLoggerAndString("cxf", level));
  }

  void setSpringLogLevel(@WebParam(name = "level") String level, @WebParam(name = "audit") SvcAudit audit) {
    LogManager.getLogger("org.springframework").setLevel(WebServiceAdminUtils.getLogLevelFromString(level))
    mem.put(0, (byte)WebServiceAdminUtils.getFunctionByteFromLoggerAndString("spring", level));
  }
}
