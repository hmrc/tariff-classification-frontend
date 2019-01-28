/*
 * Copyright 2019 HM Revenue & Customs
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
import java.util.UUID

import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.CaseStatus
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.response.ScanStatus

object Cases {

  val storedAttachment = StoredAttachment("id",  public = true, None, Some("url"), "name", "type", Some(ScanStatus.READY), ZonedDateTime.now())
  val storedOperatorAttachment = StoredAttachment("id",  public = true, Some(Operator("0", Some("Operator Name"))), Some("url"), "name", "type", Some(ScanStatus.READY), ZonedDateTime.now())
  val letterOfAuthority = StoredAttachment("id", public = true, None, Some("url"), "letterOfAuthority", "pdf", Some(ScanStatus.READY), ZonedDateTime.now())
  val eoriDetailsExample = EORIDetails("eori", "trader-business-name", "line1", "line2", "line3", "postcode", "country")
  val eoriAgentDetailsExample = AgentDetails(EORIDetails("eori", "agent-business-name", "line1", "line2", "line3", "postcode", "country"), Some(Attachment(UUID.randomUUID().toString, true, None, ZonedDateTime.now())))
  val contactExample = Contact("name", "email", Some("phone"))
  val btiApplicationExample = BTIApplication(eoriDetailsExample, contactExample, Some(eoriAgentDetailsExample), false, "Laptop", "Personal Computer", None, None, None, None, None, None, false, false)
  val decision = Decision("AD12324FR", ZonedDateTime.now(), ZonedDateTime.now().plusYears(2), "justification", "good description", Seq("k1", "k2"), None, None, Some("denomination"), None)
  val liabilityApplicationExample = LiabilityOrder(eoriDetailsExample, contactExample, "status", "port", "entry number", ZonedDateTime.now())
  val btiCaseExample = Case("1", CaseStatus.OPEN, ZonedDateTime.now(), 0, None, None, None, None, btiApplicationExample, Some(decision), Seq())
  val liabilityCaseExample = Case("1", CaseStatus.OPEN, ZonedDateTime.now(), 0, None, None, None, None, liabilityApplicationExample, None, Seq())

  def attachment(id: String = UUID.randomUUID().toString): Attachment = {
    Attachment(
      id = id,
      public = true,
      operator = Some(Operator("0", Some("operatorName"))),
      timestamp = ZonedDateTime.now()
    )
  }

  def aCase(withModifier: (Case => Case)*): Case = {
    withModifier.foldLeft(btiCaseExample)((current: Case, modifier) => modifier.apply(current))
  }

  def withOptionalApplicationFields(confidentialInformation: Option[String] = None,
                                    otherInformation: Option[String] = None,
                                    reissuedBTIReference: Option[String] = None,
                                    relatedBTIReference: Option[String] = None,
                                    knownLegalProceedings: Option[String] = None,
                                    envisagedCommodityCode: Option[String] = None): Case => Case = {
    c =>
      c.copy(
        application = c.application.asBTI.copy(
          confidentialInformation = confidentialInformation,
          otherInformation = otherInformation,
          reissuedBTIReference = reissuedBTIReference,
          relatedBTIReference = relatedBTIReference,
          knownLegalProceedings = knownLegalProceedings,
          envisagedCommodityCode = envisagedCommodityCode
        )
      )
  }

  def withReference(ref: String): Case => Case = {
    _.copy(reference = ref)
  }

  def withStatus(status: CaseStatus): Case => Case = {
    _.copy(status = status)
  }

  def withoutAgent(): Case => Case = {
    c => c.copy(application = c.application.asBTI.copy(agent = None))
  }

  def withAgent(eori: String = "agent-eori",
                businessName: String = "agent-business",
                addressLine1: String = "agent-address1",
                addressLine2: String = "agent-address2",
                addressLine3: String = "agent-address3",
                postcode: String = "agent-postcode",
                country: String = "agent-country",
                letter: Option[Attachment] = None): Case => Case = {
    c =>
      val eoriDetails = EORIDetails(eori, businessName, addressLine1, addressLine2, addressLine3, postcode, country)
      val agentDetails = AgentDetails(eoriDetails, letter)
      c.copy(application = c.application.asBTI.copy(agent = Some(agentDetails)))
  }

  def withAttachment(attachment: Attachment): Case => Case = {
    c => c.copy(attachments = c.attachments :+ attachment)
  }

  def withContact(contact: Contact): Case => Case = {
    c =>c.copy(application = c.application.asBTI.copy(contact = contact))
  }

  def withoutAttachments(): Case => Case = {
    _.copy(attachments = Seq.empty)
  }

  def withoutDecision(): Case => Case = {
    _.copy(decision = None)
  }

}
