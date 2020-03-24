/*
 * Copyright 2020 HM Revenue & Customs
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

package utils

import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.util.UUID

import models.CaseStatus.CaseStatus
import models.ImportExport.ImportExport
import models.LiabilityStatus.LiabilityStatus
import models.SampleReturn.SampleReturn
import models.SampleStatus.SampleStatus
import models._
import models.response.ScanStatus

object Cases {

  val storedAttachment = StoredAttachment("id", public = true, None, Some("url"), "name", "type", Some(ScanStatus.READY), Instant.now())
  val storedOperatorAttachment = StoredAttachment("id", public = true, Some(Operator("0", Some("Operator Name"))), Some("url"), "name", "type", Some(ScanStatus.READY), Instant.now())
  val letterOfAuthority = StoredAttachment("id", public = true, None, Some("url"), "letterOfAuthority", "pdf", Some(ScanStatus.READY), Instant.now())
  val eoriDetailsExample = EORIDetails("eori", "trader-business-name", "line1", "line2", "line3", "postcode", "country")
  val eoriAgentDetailsExample = AgentDetails(EORIDetails("eori", "agent-business-name", "line1", "line2", "line3", "postcode", "country"), Some(Attachment("letter-id", public = true, None, Instant.now())))
  val contactExample = Contact("name", "email", Some("phone"))
  val btiApplicationExample = BTIApplication(eoriDetailsExample, contactExample, Some(eoriAgentDetailsExample), offline = false, "Laptop", "Personal Computer", None, None, None, None, None, None, None, sampleToBeProvided = false, sampleToBeReturned = false)
  val simpleBtiApplicationExample = BTIApplication(eoriDetailsExample, contactExample, None, offline = false, "Laptop", "Personal Computer", None, None, None, None, None, None, None, sampleToBeProvided = false, sampleToBeReturned = false)
  val decision = Decision("040900", Some(Instant.now()), Some(Instant.now().plusSeconds(2*3600*24*365)), "justification", "good description", None, None, Some("denomination"), Seq.empty)
  val incompleteDecision = Decision("", Some(Instant.now()), Some(Instant.now().plusSeconds(2*3600*24*365)), "justification", "", None, None, Some("denomination"), Seq.empty)
  val decisionWithExclusion = decision.copy(methodExclusion = Some("Excludes everything ever"))
  val liabilityApplicationExample = LiabilityOrder(contactExample, LiabilityStatus.NON_LIVE, "trader-business-name", Some("good-name"), Some(Instant.now()), Some("entry number"),Some("trader-1234567"),Some("officer-1234567"))
  val liabilityLiveApplicationExample = LiabilityOrder(contactExample, LiabilityStatus.LIVE, "trader-business-name", Some("good-name"), Some(Instant.now()), Some("entry number"))
  val btiCaseExample = Case("1", CaseStatus.OPEN, Instant.now(), 0, None, None, None, btiApplicationExample, Some(decision), Seq())
  val btiCaseWithIncompleteDecision = Case("1", CaseStatus.OPEN, Instant.now(), 0, None, None, None, btiApplicationExample, Some(incompleteDecision), Seq())
  val simpleCaseExample = Case("1", CaseStatus.OPEN, Instant.now(), 0, None, None, None, simpleBtiApplicationExample, None, Seq())
  val liabilityCaseExample = Case("1", CaseStatus.OPEN, Instant.now(), 0, None, None, None, liabilityApplicationExample, None, Seq())
  val liabilityCaseWithDecisionExample = Case("1", CaseStatus.OPEN, Instant.now(), 0, None, Some(Operator("0", Some("Kevin"))), None, liabilityApplicationExample, Some(decisionWithExclusion), Seq())
  val liabilityLiveCaseExample = Case("1", CaseStatus.OPEN, Instant.now(), 0, None, None, None, liabilityLiveApplicationExample, None, Seq())
  val caseQueueExample = Case("1", CaseStatus.OPEN, Instant.now(), 0, None, None, Some("1"), btiApplicationExample, Some(decision), Seq())
  val caseAssignedExample = Case("1", CaseStatus.OPEN, Instant.now(), 0, None, Some(Operator("1", Some("Test User"))), Some("1"), btiApplicationExample, Some(decision), Seq())
  val expiredRuling = decision.copy(
    effectiveStartDate = Some(Instant.now().plus(-20, DAYS)),
    effectiveEndDate = Some(Instant.now().plus(-10, DAYS))
  )
  val btiCaseWithExpiredRuling = btiCaseExample.copy(status = CaseStatus.COMPLETED, decision = Some(expiredRuling))
  val liabilityCaseWithExpiredRuling = liabilityCaseExample.copy(status = CaseStatus.COMPLETED, decision = Some(expiredRuling))
  val operator = Operator(id = "0", name =Some("liability op name"), permissions = Set(Permission.RELEASE_CASE))

  def attachment(id: String = UUID.randomUUID().toString): Attachment = {
    Attachment(
      id = id,
      public = true,
      operator = Some(Operator("0", Some("operatorName"))),
      timestamp = Instant.now()
    )
  }

  def aCase(withModifier: (Case => Case)*): Case = {
    withModifier.foldLeft(btiCaseExample)((current: Case, modifier) => modifier.apply(current))
  }

  def withBTIApplication: Case => Case = {
    _.copy(application = btiApplicationExample)
  }

  def withLiabilityApplication(contact: Contact = Contact("name", "email@email.com", Some("1234")),
                               status: LiabilityStatus = LiabilityStatus.NON_LIVE,
                               traderName: String = "trader",
                               goodName: Option[String] = Some("Goods Name"),
                               entryDate: Option[Instant] = Some(Instant.EPOCH),
                               entryNumber: Option[String] = Some("1234567"),
                               traderCommodityCode: Option[String] = Some("0100000000"),
                               officerCommodityCode: Option[String] = Some("0200000000")): Case => Case = {
    _.copy(application = liabilityApplicationExample.copy(
      contact = contact,
      status = status,
      traderName = traderName,
      goodName = goodName,
      entryDate = entryDate,
      entryNumber = entryNumber,
      traderCommodityCode = traderCommodityCode,
      officerCommodityCode = officerCommodityCode
    ))
  }

  def withIncompleteLiabilityApplication(contact: Contact = Contact("name", "email@email.com", Some("1234")),
                               status: LiabilityStatus = LiabilityStatus.NON_LIVE,
                               traderName: String = "trader",
                               goodName: Option[String] = Some("Goods Name"),
                               entryDate: Option[Instant] = Some(Instant.EPOCH),
                               entryNumber: Option[String] = Some("1234567"),
                               traderCommodityCode: Option[String] = Some("0100000000"),
                               officerCommodityCode: Option[String] = Some("0200000000")): Case => Case = {
    _.copy(application = liabilityApplicationExample.copy(
      contact = contact,
      status = status,
      traderName = traderName,
      goodName = goodName,
      entryDate = entryDate,
      entryNumber = entryNumber,
      traderCommodityCode = traderCommodityCode,
      officerCommodityCode = officerCommodityCode
    ))
  }

  def withSampleStatus(sampleStatus : Option[SampleStatus]): Case => Case = { c =>
    c.copy(sample = c.sample.copy(status = sampleStatus))
  }

  def withSample(sample : Sample): Case => Case = { c =>
    c.copy(sample = sample)
  }

  def withSampleRequested(operator: Option[Operator], returnStatus : Option[SampleReturn]): Case => Case = { c =>
    c.copy(sample = c.sample.copy(requestedBy = operator, returnStatus = returnStatus))
  }

  def withAssignee(operator: Option[Operator]): Case => Case = {
    _.copy(assignee = operator)
  }

  def withoutAssignee(): Case => Case = {
    _.copy(assignee = None)
  }

  def withDaysElapsed(elapsed: Int): Case => Case = {
    _.copy(daysElapsed = elapsed)
  }

  def withQueue(queue: String): Case => Case = {
    _.copy(queueId = Some(queue))
  }

  def withoutQueue(): Case => Case = {
    _.copy(queueId = None)
  }

  def withBTIDetails(offline: Boolean = false,
                     goodName: String = "good name",
                     goodDescription: String = "good description",
                     confidentialInformation: Option[String] = None,
                     otherInformation: Option[String] = None,
                     reissuedBTIReference: Option[String] = None,
                     relatedBTIReference: Option[String] = None,
                     knownLegalProceedings: Option[String] = None,
                     envisagedCommodityCode: Option[String] = None,
                     importOrExport: Option[ImportExport] = None,
                     sampleToBeProvided: Boolean = false,
                     sampleToBeReturned: Boolean = false): Case => Case = { c =>
    c.copy(application = c.application.asBTI.copy(
      offline = offline,
      goodName = goodName,
      goodDescription = goodDescription,
      confidentialInformation = confidentialInformation,
      otherInformation = otherInformation,
      reissuedBTIReference = reissuedBTIReference,
      relatedBTIReference = relatedBTIReference,
      knownLegalProceedings = knownLegalProceedings,
      envisagedCommodityCode = envisagedCommodityCode,
      importOrExport = importOrExport,
      sampleToBeProvided = sampleToBeProvided,
      sampleToBeReturned = sampleToBeReturned
    ))
  }

  def withHolder(eori: String = "eori",
                 businessName: String = "business name",
                 addressLine1: String = "address line 1",
                 addressLine2: String = "address line 2",
                 addressLine3: String = "address line 3",
                 postcode: String = "postcode",
                 country: String = "country"): Case => Case = { c =>
    c.copy(application = c.application.asBTI.copy(holder = EORIDetails(
      eori,
      businessName,
      addressLine1,
      addressLine2,
      addressLine3,
      postcode,
      country
    )))
  }

  def withOptionalApplicationFields(confidentialInformation: Option[String] = None,
                                    otherInformation: Option[String] = None,
                                    reissuedBTIReference: Option[String] = None,
                                    relatedBTIReference: Option[String] = None,
                                    knownLegalProceedings: Option[String] = None,
                                    envisagedCommodityCode: Option[String] = None,
                                    importOrExport: Option[ImportExport] = None): Case => Case = { c =>
    c.copy(
      application = c.application.asBTI.copy(
        confidentialInformation = confidentialInformation,
        otherInformation = otherInformation,
        reissuedBTIReference = reissuedBTIReference,
        relatedBTIReference = relatedBTIReference,
        knownLegalProceedings = knownLegalProceedings,
        envisagedCommodityCode = envisagedCommodityCode,
        importOrExport = importOrExport
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
                letter: Option[Attachment] = None): Case => Case = { c =>
    val eoriDetails = EORIDetails(eori, businessName, addressLine1, addressLine2, addressLine3, postcode, country)
    val agentDetails = AgentDetails(eoriDetails, letter)
    c.copy(application = c.application.asBTI.copy(agent = Some(agentDetails)))
  }

  def withAttachment(attachment: Attachment): Case => Case = {
    c => c.copy(attachments = c.attachments :+ attachment)
  }

  def withContact(contact: Contact): Case => Case = {
    c => c.copy(application = c.application.asBTI.copy(contact = contact))
  }

  def withoutAttachments(): Case => Case = {
    _.copy(attachments = Seq.empty)
  }

  def withoutDecision(): Case => Case = {
    _.copy(decision = None)
  }

  def withDecision(bindingCommodityCode: String = "decision-commodity-code",
                   effectiveStartDate: Option[Instant] = Some(Instant.now()),
                   effectiveEndDate: Option[Instant] = Some(Instant.now().plus(30, DAYS)),
                   justification: String = "decision-justification",
                   goodsDescription: String = "decision-goods-description",
                   methodSearch: Option[String] = Some("search"),
                   methodExclusion: Option[String] = Some("exclusion"),
                   methodCommercialDenomination: Option[String] = None,
                   appeal: Seq[Appeal] = Seq.empty,
                   cancellation: Option[Cancellation] = None,
                   explanation: Option[String] = Some("explanation")
                  ): Case => Case = {
    _.copy(decision = Some(
      Decision(
        bindingCommodityCode,
        effectiveStartDate,
        effectiveEndDate,
        justification,
        goodsDescription,
        methodSearch,
        methodExclusion,
        methodCommercialDenomination,
        appeal,
        cancellation,
        explanation
      )))
  }

  def withIncompleteDecision(bindingCommodityCode: String = "decision-commodity-code",
                   effectiveStartDate: Option[Instant] = Some(Instant.now()),
                   effectiveEndDate: Option[Instant] = Some(Instant.now().plus(30, DAYS)),
                   justification: String = "decision-justification",
                   goodsDescription: String = "",
                   methodSearch: Option[String] = Some("search"),
                   methodExclusion: Option[String] = Some("exclusion"),
                   methodCommercialDenomination: Option[String] = None,
                   appeal: Seq[Appeal] = Seq.empty,
                   cancellation: Option[Cancellation] = None,
                   explanation: Option[String] = Some("explanation")
                  ): Case => Case = {
    _.copy(decision = Some(
      Decision(
        bindingCommodityCode,
        effectiveStartDate,
        effectiveEndDate,
        justification,
        goodsDescription,
        methodSearch,
        methodExclusion,
        methodCommercialDenomination,
        appeal,
        cancellation,
        explanation
      )))
  }

  def withCreatedDate(date: Instant): Case => Case = {
    _.copy(createdDate = date)
  }

}
