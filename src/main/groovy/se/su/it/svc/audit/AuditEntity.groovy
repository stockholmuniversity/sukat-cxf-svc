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
