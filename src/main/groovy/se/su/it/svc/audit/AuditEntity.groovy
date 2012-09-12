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
  List<String> MethodDetails

  public String toString() {
    String ret = "Created: " + this.Created + "\r\n"
    ret += "Ip_address: " + this.Ip_address + "\r\n"
    ret += "Uid: " +  this.Uid + "\r\n"
    ret += "Client: " + this.Client + "\r\n"
    ret += "Operation: " + this.Operation + "\r\n"
    ret += "Text_args: " + this.Text_args + "\r\n"
    ret += "Raw_args: " + this.Raw_args + "\r\n"
    ret += "Text_return: " + this.Text_return + "\r\n"
    ret += "Raw_return: " + this.Raw_return + "\r\n"
    ret += "State: " + this.State + "\r\n"

    this.MethodDetails?.each {entry -> ret += "This method will also invoke: " + entry + "\r\n"}
    return ret
  }
}
