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
    + "Ip_address: " + this.Ip_address + "\r\n"
    + "Uid: " +  this.Uid + "\r\n"
    + "Client: " + this.Client + "\r\n"
    + "Operation: " + this.Operation + "\r\n"
    + "Text_args: " + this.Text_args + "\r\n"
    + "Raw_args: " + this.Raw_args + "\r\n"
    + "Text_return: " + this.Text_return + "\r\n"
    + "Raw_return: " + this.Raw_return + "\r\n"
    + "State: " + this.State + "\r\n"
  }
}
