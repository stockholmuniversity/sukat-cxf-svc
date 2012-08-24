package se.su.it.svc.audit

/**
 * Created with IntelliJ IDEA.
 * User: jqvar
 * Date: 2012-08-24
 * Time: 08:48
 * To change this template use File | Settings | File Templates.
 */
public class AuditEntity {
  String Created
  String Ip_address
  String Uid
  String Client
  String Operation
  String Text_args
  String Raw_args
  String Text_return
  String Raw_return
  String State

  public String toString() {
    return "Created: " + this.Created + "\r\n"
    ae += "Ip_address: " + this.Ip_address + "\r\n"
    ae += "Uid: " +  this.Uid + "\r\n"
    ae += "Client: " + this.Client + "\r\n"
    ae += "Operation: " + this.Operation + "\r\n"
    ae += "Text_args: " + this.Text_args + "\r\n"
    ae += "Raw_args: " + this.Raw_args + "\r\n"
    ae += "Text_return: " + this.Text_return + "\r\n"
    ae += "Raw_return: " + this.Raw_return + "\r\n"
    ae += "State: " + this.State + "\r\n"
  }
}
