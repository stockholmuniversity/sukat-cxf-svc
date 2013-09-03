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

import se.su.it.svc.annotations.SuCxfSvcSpocpRole
import se.su.it.svc.audit.AuditAspectMethodDetails
import se.su.it.svc.commons.SvcAudit
import se.su.it.svc.commons.SvcSuPersonVO

@SuCxfSvcSpocpRole(role = "sukat-account-admin")
public interface AccountService {
  void updatePrimaryAffiliation(String uid, String affiliation, SvcAudit audit)
  @AuditAspectMethodDetails(details = "setPassword")
  String resetPassword(String uid, SvcAudit audit)
  void updateSuPerson(String uid,SvcSuPersonVO person, SvcAudit audit)
  String createSuPerson(String uid, String domain, String nin, String givenName, String sn, SvcSuPersonVO person, boolean fullAccount, SvcAudit audit)
  void terminateSuPerson(String uid, SvcAudit audit)
  String getMailRoutingAddress(String uid, SvcAudit audit)
  void setMailRoutingAddress(String uid, String mail, SvcAudit audit)
  SvcSuPersonVO findSuPersonByNorEduPersonNIN(String norEduPersonNIN, SvcAudit audit)
  SvcSuPersonVO findSuPersonBySocialSecurityNumber(String socialSecurityNumber, SvcAudit audit)
  SvcSuPersonVO findSuPersonByUid(String uid, SvcAudit audit)
}
