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

package uk.gov.tariffclassificationfrontend.utils

import java.time.ZonedDateTime

import uk.gov.hmrc.tariffclassificationfrontend.models._

object Cases {

  val eoriDetailsExample = EORIDetails("eori", "trader-business-name", "line1", "line2", "line3", "postcode", "country")
  val eoriAgentDetailsExample = AgentDetails(EORIDetails("eori", "agent-business-name", "line1", "line2", "line3", "postcode", "country"), Attachment(false, true, "http://mockurl", "pdf-type", ZonedDateTime.now()))
  val contactExample = Contact("name", "email", Some("phone"))
  val btiApplicationExample = BTIApplication(eoriDetailsExample, contactExample, Some(eoriAgentDetailsExample), false, "Laptop", "Personal Computer", None, None, None, None, None, None, false, false)
  val decision = Decision("AD12324FR", ZonedDateTime.now(), ZonedDateTime.now().plusYears(2), "justification", "good description", Seq("k1", "k2"), None, None, Some("denomination"), None)
  val liabilityApplicationExample = LiabilityOrder(eoriDetailsExample, contactExample, "status", "port", "entry number", ZonedDateTime.now())
  val btiCaseExample = Case("1", CaseStatus.OPEN, ZonedDateTime.now(), 0, None, None, None, None, btiApplicationExample, Some(decision), Seq())
  val liabilityCaseExample = Case("1", CaseStatus.OPEN, ZonedDateTime.now(), 0, None, None, None, None, liabilityApplicationExample, None, Seq())

  def createAttachment(url: String): Attachment = {
    Attachment(
      application = true,
      public = true,
      url = url,
      mimeType = "",
      timestamp = ZonedDateTime.now()
    )
  }

}
