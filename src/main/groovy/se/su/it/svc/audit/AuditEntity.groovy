package se.su.it.svc.audit

import groovy.transform.ToString

@ToString(includeNames=true, excludes="raw_args,raw_return")
public class AuditEntity {
  String created
  String ip_address
  String uid
  String client
  String operation
  String text_args
  String raw_args
  String text_return
  String raw_return
  String state
  List<String> methodDetails

  private AuditEntity() {}

  public static getInstance(String created,
                            String ip_address,
                            String uid,
                            String client,
                            String operation,
                            String text_args,
                            String raw_args,
                            String text_return,
                            String raw_return,
                            String state,
                            List<String> methodDetails) {

    AuditEntity auditEntity = new AuditEntity()
    auditEntity.created       = created
    auditEntity.ip_address    = ip_address
    auditEntity.uid           = uid
    auditEntity.client        = client
    auditEntity.operation     = operation
    auditEntity.text_args     = text_args
    auditEntity.raw_args      = raw_args
    auditEntity.text_return   = text_return
    auditEntity.raw_return    = raw_return
    auditEntity.state         = state
    auditEntity.methodDetails = methodDetails
    return auditEntity
  }
}
