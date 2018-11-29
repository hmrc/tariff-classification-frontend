/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.tariffclassificationfrontend.audit

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.tariffclassificationfrontend.audit.AuditPayloadType.CaseReleased
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters.caseFormat

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AuditService @Inject()(auditConnector: DefaultAuditConnector) {

  def auditCaseReleased(c: Case)
                       (implicit hc: HeaderCarrier): Unit = {
/*
    TODO: verify with TxM if we need to include any specific detail about the user logged in.

    Possible example:

    "operatorDetails" : {
      "roles" : [ "hts helpdesk advisor", "other role"] // list of roles operator has
      "pid"   : "abc123"                                // personal identifier operator uses to login to machine
      "name"  : "Operator Shmoperator"                  // name of operator
      "email" : "operator@shmoperator.com"              // email of operator
    }
*/

    sendExplicitAuditEvent(CaseReleased, toJson(c))
  }

  private def sendExplicitAuditEvent(auditEventType: String, auditPayload: JsValue)
                                    (implicit hc : uk.gov.hmrc.http.HeaderCarrier): Unit = {
    auditConnector.sendExplicitAudit(auditType = auditEventType, detail = auditPayload)
  }

}

object AuditPayloadType {

  val CaseReleased = "CaseReleased"
}
