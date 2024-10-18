/*
 * Copyright 2024 HM Revenue & Customs
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

import models.CaseStatus.CaseStatus
import models.LiabilityStatus.LiabilityStatus
import models.SampleReturn.SampleReturn
import models.SampleStatus.SampleStatus
import models._
import models.response.ScanStatus
import viewmodels._

import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.util.UUID

object Cases {

  val fileAttachment: Attachment = Attachment(id = UUID.randomUUID().toString, public = false, None)

  val storedAttachment: StoredAttachment = StoredAttachment(
    "id",
    public = true,
    None,
    Some("url"),
    Some("name"),
    Some("type"),
    Some(ScanStatus.READY),
    Instant.now(),
    Some("test description"),
    shouldPublishToRulings = true
  )
  val storedOperatorAttachment: StoredAttachment = StoredAttachment(
    "id",
    public = true,
    Some(Operator("0", Some("Operator Name"))),
    Some("url"),
    Some("name"),
    Some("type"),
    Some(ScanStatus.READY),
    Instant.now(),
    Some("test description"),
    shouldPublishToRulings = false
  )
  val letterOfAuthority: StoredAttachment = StoredAttachment(
    "id",
    public = true,
    None,
    Some("url"),
    Some("letterOfAuthority"),
    Some("pdf"),
    Some(ScanStatus.READY),
    Instant.now(),
    Some("test description"),
    shouldPublishToRulings = true
  )
  val eoriDetailsExample: EORIDetails =
    EORIDetails("eori", "trader-business-name", "line1", "line2", "line3", "postcode", "country")
  val eoriAgentDetailsExample: AgentDetails = AgentDetails(
    EORIDetails("eori", "agent-business-name", "line1", "line2", "line3", "postcode", "country"),
    Some(Attachment("letter-id", public = true, None, Instant.now()))
  )
  val contactExample: Contact = Contact("name", "email", Some("phone"))
  val btiApplicationExample: BTIApplication = BTIApplication(
    eoriDetailsExample,
    contactExample,
    Some(eoriAgentDetailsExample),
    offline = false,
    "Laptop",
    "Personal Computer",
    None,
    None,
    None,
    None,
    Nil,
    None,
    None,
    sampleToBeProvided = false,
    sampleToBeReturned = false,
    applicationPdf = Some(Attachment("id", public = false, Some(Operator("1", None))))
  )
  val simpleBtiApplicationExample: BTIApplication = BTIApplication(
    eoriDetailsExample,
    contactExample,
    None,
    offline = false,
    "Laptop",
    "Personal Computer",
    None,
    None,
    None,
    None,
    Nil,
    None,
    None,
    sampleToBeProvided = false,
    sampleToBeReturned = false,
    applicationPdf = None
  )
  val decision: Decision = Decision(
    "040900",
    Some(Instant.now()),
    Some(Instant.now().plusSeconds(2 * 3600 * 24 * 365)),
    "justification",
    "good description",
    None,
    None,
    Some("denomination"),
    Seq.empty,
    decisionPdf = Some(Attachment("id", public = false, Some(Operator("1", None))))
  )
  val incompleteDecision: Decision = Decision(
    "",
    Some(Instant.now()),
    Some(Instant.now().plusSeconds(2 * 3600 * 24 * 365)),
    "justification",
    "",
    None,
    None,
    Some("denomination"),
    Seq.empty
  )
  val decisionWithExclusion: Decision = decision.copy(methodExclusion = Some("Excludes everything ever"))
  val liabilityApplicationExample: LiabilityOrder = LiabilityOrder(
    contactExample,
    LiabilityStatus.NON_LIVE,
    "trader-business-name",
    Some("good-name"),
    Some(Instant.now()),
    Some("entry number"),
    Some("trader-1234567"),
    Some("officer-1234567")
  )
  val liabilityLiveApplicationExample: LiabilityOrder = LiabilityOrder(
    contactExample,
    LiabilityStatus.LIVE,
    "trader-business-name",
    Some("good-name"),
    Some(Instant.now()),
    Some("entry number")
  )
  val btiCaseExample: Case = Case(
    "1",
    CaseStatus.OPEN,
    Instant.now(),
    0,
    None,
    None,
    None,
    btiApplicationExample,
    Some(decision),
    Seq(),
    Set.empty,
    Sample(),
    Some(Instant.now()),
    Some(5),
    3
  )
  val btiNewCase: Case = Case(
    "1",
    CaseStatus.NEW,
    Instant.now(),
    0,
    None,
    None,
    None,
    btiApplicationExample,
    Some(decision),
    Seq(),
    Set.empty,
    Sample(),
    Some(Instant.now()),
    Some(5),
    3
  )
  val btiCaseWithIncompleteDecision: Case = Case(
    "1",
    CaseStatus.OPEN,
    Instant.now(),
    0,
    None,
    None,
    None,
    btiApplicationExample,
    Some(incompleteDecision),
    Seq(),
    referredDaysElapsed = 0
  )
  val simpleCaseExample: Case =
    Case(
      "1",
      CaseStatus.OPEN,
      Instant.now(),
      0,
      None,
      None,
      None,
      simpleBtiApplicationExample,
      None,
      Seq(),
      referredDaysElapsed = 0
    )
  val liabilityCaseExample: Case =
    Case(
      "1",
      CaseStatus.OPEN,
      Instant.now(),
      0,
      None,
      None,
      None,
      liabilityApplicationExample,
      None,
      Seq(),
      referredDaysElapsed = 0
    )
  val liabilityCaseWithDecisionExample: Case = Case(
    "1",
    CaseStatus.OPEN,
    Instant.now(),
    0,
    None,
    Some(Operator("0", Some("Kevin"))),
    None,
    liabilityApplicationExample,
    Some(decisionWithExclusion),
    Seq(),
    referredDaysElapsed = 0
  )
  val liabilityLiveCaseExample: Case =
    Case(
      "1",
      CaseStatus.OPEN,
      Instant.now(),
      0,
      None,
      None,
      None,
      liabilityLiveApplicationExample,
      None,
      Seq(),
      referredDaysElapsed = 0
    )
  val caseQueueExample: Case =
    Case(
      "1",
      CaseStatus.OPEN,
      Instant.now(),
      0,
      None,
      None,
      Some("1"),
      btiApplicationExample,
      Some(decision),
      Seq(),
      referredDaysElapsed = 0
    )
  val caseAssignedExample: Case = Case(
    "1",
    CaseStatus.OPEN,
    Instant.now(),
    0,
    None,
    Some(Operator("1", Some("Test User"))),
    Some("1"),
    btiApplicationExample,
    Some(decision),
    Seq(),
    referredDaysElapsed = 0
  )
  val expiredRuling: Decision = decision.copy(
    effectiveStartDate = Some(Instant.now().plus(-20, DAYS)),
    effectiveEndDate = Some(Instant.now().plus(-10, DAYS))
  )
  val btiCaseWithExpiredRuling: Case =
    btiCaseExample.copy(status = CaseStatus.COMPLETED, decision = Some(expiredRuling))
  val liabilityCaseWithExpiredRuling: Case =
    liabilityCaseExample.copy(status = CaseStatus.COMPLETED, decision = Some(expiredRuling))

  val pagedEvent: Paged[Event] = Paged(Seq(Events.event), 1, 1, 1)
  val queues: Seq[Queue]       = Seq(Queue("", "", ""))

  val operatorWithoutPermissions: Operator = Operator(id = "0", name = Some("liability op name"), permissions = Set())
  val operatorWithAddAttachment: Operator =
    Operator(id = "0", name = Some("liability op name"), permissions = Set(Permission.ADD_ATTACHMENT))

  val c592ViewModel: C592ViewModel = C592ViewModel(
    "caseReference",
    "entry number",
    "03 Mar 2020",
    "",
    "No",
    "",
    "good-name",
    TraderContact("trader-business-name", "email", "phone", Address("", "", None, None)),
    "trader-1234567",
    "officer-1234567",
    PortOrComplianceOfficerContact("name", "email", "phone"),
    "",
    "",
    None,
    isRepaymentClaim = false,
    Some("agentName"),
    Some("location name"),
    isMigratedCase = false
  )

  val rulingViewModel: Option[RulingViewModel] = Some(
    RulingViewModel(
      "",
      "",
      "123456",
      "item description",
      "justification",
      "method searches",
      "method exclusions",
      showEditRuling = false,
      "case references"
    )
  )

  val attachmentsTabViewModel: Option[AttachmentsTabViewModel] = Some(
    AttachmentsTabViewModel(Cases.liabilityCaseExample.reference, Nil, None)
  )
  val activityTabViewModel: Option[ActivityViewModel] = Some(
    ActivityViewModel(
      "referenceNumber",
      Some(operatorWithoutPermissions),
      Some("queueId"),
      Instant.now,
      pagedEvent,
      queues,
      "queue Name"
    )
  )
  val operatorWithPermissions: Operator =
    Operator(id = "0", name = Some("liability op name"), permissions = Set(Permission.ADD_NOTE, Permission.VIEW_CASES))
  val activityTabViewModelWithPermissions: ActivityViewModel = ActivityViewModel(
    "referenceNumber",
    Some(operatorWithPermissions),
    Some("queueId"),
    Instant.now,
    pagedEvent,
    queues,
    "queue Name"
  )
  val operatorWithCompleteCasePermission: Operator = Operator(
    id = "0",
    name = Some("liability op name"),
    permissions = Set(Permission.COMPLETE_CASE, Permission.REOPEN_CASE)
  )
  val operatorWithKeywordsPermissions: Operator =
    Operator(id = "0", name = Some("liability op name"), permissions = Set(Permission.KEYWORDS))
  val operatorWithEditLiabilityPermissions: Operator =
    Operator(id = "0", name = Some("liability op name"), permissions = Set(Permission.EDIT_LIABILITY))
  val operatorWithoutCompleteCasePermission: Operator =
    Operator(id = "0", name = Some("liability op name"), permissions = Set(Permission.VIEW_CASES))
  val operatorWithReleaseOrSuppressPermissions: Operator = Operator(
    id = "0",
    name = Some("liability op name"),
    permissions = Set(Permission.RELEASE_CASE, Permission.SUPPRESS_CASE)
  )

  val newLiabilityLiveApplicationExample: LiabilityOrder = LiabilityOrder(
    Contact("contact-name", "contact@email.com", Some("contact-phone")),
    LiabilityStatus.LIVE,
    "trader-business-name",
    Some("good-name"),
    Some(Instant.now()),
    None
  )
  val newLiabilityLiveCaseExample: Case =
    Case(
      "1",
      CaseStatus.NEW,
      Instant.now(),
      0,
      None,
      None,
      None,
      newLiabilityLiveApplicationExample,
      None,
      Seq(),
      referredDaysElapsed = 0
    )

  val liabilityWithCompleteDecision: LiabilityOrder = LiabilityOrder(
    Contact(name = "contact-name", email = "contact@email.com", Some("contact-phone")),
    status = LiabilityStatus.LIVE,
    traderName = "trader-name",
    goodName = Some("good-name"),
    entryDate = Some(Instant.EPOCH),
    entryNumber = Some("entry-no"),
    traderCommodityCode = Some("0200000000"),
    officerCommodityCode = Some("0100000000"),
    btiReference = Some("btiReferenceN"),
    repaymentClaim = Some(RepaymentClaim(dvrNumber = Some(""), dateForRepayment = Some(Instant.EPOCH))),
    dateOfReceipt = Some(Instant.EPOCH),
    traderContactDetails = Some(
      TraderContactDetails(
        email = Some("trader@email.com"),
        phone = Some("2345"),
        address = Some(
          Address(
            buildingAndStreet = "STREET 1",
            townOrCity = "Town",
            county = Some("County"),
            postCode = Some("postcode")
          )
        )
      )
    ),
    agentName = Some("agent"),
    port = Some("port")
  )

  val aCaseWithCompleteDecision: Case = Cases.liabilityCaseExample
    .copy(reference = "123456", caseBoardsFileNumber = Some("SCR/ARD/123"), application = liabilityWithCompleteDecision)

  def attachment(id: String = UUID.randomUUID().toString): Attachment =
    Attachment(
      id = id,
      public = true,
      operator = Some(Operator("0", Some("operatorName"))),
      timestamp = Instant.now(),
      shouldPublishToRulings = true
    )

  def aCase(withModifier: (Case => Case)*): Case =
    withModifier.foldLeft(btiCaseExample)((current: Case, modifier) => modifier.apply(current))

  def aCorrespondenceCase(withModifier: (Case => Case)*): Case =
    withModifier.foldLeft(correspondenceCaseExample)((current: Case, modifier) => modifier.apply(current))

  def aMiscellaneousCase(withModifier: (Case => Case)*): Case =
    withModifier.foldLeft(miscellaneousCaseExample)((current: Case, modifier) => modifier.apply(current))

  def aLiabilityCase(withModifier: (Case => Case)*): Case =
    withModifier.foldLeft(liabilityCaseExample)((current: Case, modifier) => modifier.apply(current))

  def withBTIApplication: Case => Case =
    _.copy(application = btiApplicationExample)

  def withCorrespondenceApplication: Case => Case =
    _.copy(application = corrApplicationExample)

  def withMiscellaneousApplication: Case => Case =
    _.copy(application = miscExample)

  def withLiabilityApplication(
    contact: Contact = Contact("name", "email@email.com", Some("1234")),
    status: LiabilityStatus = LiabilityStatus.NON_LIVE,
    traderName: String = "trader",
    goodName: Option[String] = Some("Goods Name"),
    entryDate: Option[Instant] = Some(Instant.EPOCH),
    entryNumber: Option[String] = Some("1234567"),
    traderCommodityCode: Option[String] = Some("0100000000"),
    officerCommodityCode: Option[String] = Some("0200000000")
  ): Case => Case =
    _.copy(application =
      liabilityApplicationExample.copy(
        contact = contact,
        status = status,
        traderName = traderName,
        goodName = goodName,
        entryDate = entryDate,
        entryNumber = entryNumber,
        traderCommodityCode = traderCommodityCode,
        officerCommodityCode = officerCommodityCode
      )
    )

  def liabilityApplicationWithC592(
    contact: Contact = Contact("name", "email@email.com", Some("1234")),
    status: LiabilityStatus = LiabilityStatus.NON_LIVE,
    traderName: String = "trader",
    goodName: Option[String] = Some("Goods Name"),
    entryDate: Option[Instant] = Some(Instant.now.plus(-20, DAYS)),
    entryNumber: Option[String] = Some("1234567"),
    traderCommodityCode: Option[String] = Some("0100000000"),
    officerCommodityCode: Option[String] = Some("0200000000"),
    traderDetails: Option[TraderContactDetails] = Some(
      TraderContactDetails(
        email = Some("trader@email.com"),
        phone = Some("2345"),
        address = Some(
          Address(
            buildingAndStreet = "STREET 1",
            townOrCity = "Town",
            county = Some("County"),
            postCode = Some("NE10 0HW")
          )
        )
      )
    )
  ): Case => Case =
    _.copy(application =
      liabilityApplicationExample.copy(
        contact = contact,
        status = status,
        traderName = traderName,
        goodName = goodName,
        entryDate = entryDate,
        entryNumber = entryNumber,
        traderCommodityCode = traderCommodityCode,
        officerCommodityCode = officerCommodityCode,
        traderContactDetails = traderDetails
      )
    )

  def withIncompleteLiabilityApplication(
    contact: Contact = Contact("name", "email@email.com", Some("1234")),
    status: LiabilityStatus = LiabilityStatus.NON_LIVE,
    traderName: String = "trader",
    goodName: Option[String] = Some("Goods Name"),
    entryDate: Option[Instant] = Some(Instant.EPOCH),
    entryNumber: Option[String] = Some("1234567"),
    traderCommodityCode: Option[String] = Some("0100000000"),
    officerCommodityCode: Option[String] = Some("0200000000")
  ): Case => Case =
    _.copy(application =
      liabilityApplicationExample.copy(
        contact = contact,
        status = status,
        traderName = traderName,
        goodName = goodName,
        entryDate = entryDate,
        entryNumber = entryNumber,
        traderCommodityCode = traderCommodityCode,
        officerCommodityCode = officerCommodityCode
      )
    )

  def withSampleStatus(sampleStatus: Option[SampleStatus]): Case => Case = { c =>
    c.copy(sample = c.sample.copy(status = sampleStatus))
  }

  def withSample(sample: Sample): Case => Case = { c => c.copy(sample = sample) }

  def withSampleRequested(operator: Option[Operator], returnStatus: Option[SampleReturn]): Case => Case = { c =>
    c.copy(sample = c.sample.copy(requestedBy = operator, returnStatus = returnStatus))
  }

  def withAssignee(operator: Option[Operator]): Case => Case =
    _.copy(assignee = operator)

  def withoutAssignee(): Case => Case =
    _.copy(assignee = None)

  def withDaysElapsed(elapsed: Long): Case => Case =
    _.copy(daysElapsed = elapsed)

  def withQueue(queue: String): Case => Case =
    _.copy(queueId = Some(queue))

  def withoutQueue(): Case => Case =
    _.copy(queueId = None)

  def withBTIDetails(
    offline: Boolean = false,
    goodName: String = "good name",
    goodDescription: String = "good description",
    confidentialInformation: Option[String] = None,
    otherInformation: Option[String] = None,
    reissuedBTIReference: Option[String] = None,
    relatedBTIReference: Option[String] = None,
    knownLegalProceedings: Option[String] = None,
    envisagedCommodityCode: Option[String] = None,
    sampleToBeProvided: Boolean = false,
    sampleToBeReturned: Boolean = false
  ): Case => Case = { c =>
    c.copy(application =
      c.application.asATAR.copy(
        offline = offline,
        goodName = goodName,
        goodDescription = goodDescription,
        confidentialInformation = confidentialInformation,
        otherInformation = otherInformation,
        reissuedBTIReference = reissuedBTIReference,
        relatedBTIReference = relatedBTIReference,
        knownLegalProceedings = knownLegalProceedings,
        envisagedCommodityCode = envisagedCommodityCode,
        sampleToBeProvided = sampleToBeProvided,
        sampleToBeReturned = sampleToBeReturned
      )
    )
  }

  def withHolder(
    eori: String = "eori",
    businessName: String = "business name",
    addressLine1: String = "address line 1",
    addressLine2: String = "address line 2",
    addressLine3: String = "address line 3",
    postcode: String = "postcode",
    country: String = "country"
  ): Case => Case = { c =>
    c.copy(application =
      c.application.asATAR.copy(holder =
        EORIDetails(
          eori,
          businessName,
          addressLine1,
          addressLine2,
          addressLine3,
          postcode,
          country
        )
      )
    )
  }

  def withOptionalApplicationFields(
    confidentialInformation: Option[String] = None,
    otherInformation: Option[String] = None,
    reissuedBTIReference: Option[String] = None,
    relatedBTIReference: Option[String] = None,
    relatedBTIReferences: List[String] = Nil,
    knownLegalProceedings: Option[String] = None,
    envisagedCommodityCode: Option[String] = None
  ): Case => Case = { c =>
    c.copy(
      application = c.application.asATAR.copy(
        confidentialInformation = confidentialInformation,
        otherInformation = otherInformation,
        reissuedBTIReference = reissuedBTIReference,
        relatedBTIReference = relatedBTIReference,
        relatedBTIReferences = relatedBTIReferences,
        knownLegalProceedings = knownLegalProceedings,
        envisagedCommodityCode = envisagedCommodityCode
      )
    )
  }

  def withReference(ref: String): Case => Case =
    _.copy(reference = ref)

  def withStatus(status: CaseStatus): Case => Case =
    _.copy(status = status)

  def withoutAgent(): Case => Case = { c => c.copy(application = c.application.asATAR.copy(agent = None)) }

  def withAgent(
    eori: String = "agent-eori",
    businessName: String = "agent-business",
    addressLine1: String = "agent-address1",
    addressLine2: String = "agent-address2",
    addressLine3: String = "agent-address3",
    postcode: String = "agent-postcode",
    country: String = "agent-country",
    letter: Option[Attachment] = None
  ): Case => Case = { c =>
    val eoriDetails  = EORIDetails(eori, businessName, addressLine1, addressLine2, addressLine3, postcode, country)
    val agentDetails = AgentDetails(eoriDetails, letter)
    c.copy(application = c.application.asATAR.copy(agent = Some(agentDetails)))
  }

  def withAttachment(attachment: Attachment): Case => Case = { c => c.copy(attachments = c.attachments :+ attachment) }

  def withContact(contact: Contact): Case => Case = { c =>
    c.copy(application = c.application.asATAR.copy(contact = contact))
  }

  def withoutAttachments(): Case => Case =
    _.copy(attachments = Seq.empty)

  def withoutDecision(): Case => Case =
    _.copy(decision = None)

  def withDecision(
    bindingCommodityCode: String = "decision-commodity-code",
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
  ): Case => Case =
    _.copy(decision =
      Some(
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
        )
      )
    )

  def withIncompleteDecision(
    bindingCommodityCode: String = "decision-commodity-code",
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
  ): Case => Case =
    _.copy(decision =
      Some(
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
        )
      )
    )

  def withCreatedDate(date: Instant): Case => Case =
    _.copy(createdDate = date)

  val correspondenceExample: CorrespondenceApplication = CorrespondenceApplication(
    None,
    None,
    Address("s", "s", None, None),
    Contact("name", "email"),
    None,
    "Laptop",
    "Personal Computer",
    sampleToBeProvided = false,
    sampleToBeReturned = false
  )

  val corrExampleWithMissingFields: CorrespondenceApplication = CorrespondenceApplication(
    None,
    None,
    Address("s", "s", None, None),
    Contact("name", "email"),
    None,
    "",
    "Personal Computer",
    sampleToBeProvided = false,
    sampleToBeReturned = false
  )

  val miscExample: MiscApplication = MiscApplication(
    Contact("name", "email"),
    "name",
    None,
    MiscCaseType.HARMONISED,
    Some("A detailed description"),
    sampleToBeProvided = false,
    sampleToBeReturned = false
  )

  val miscExampleWithMissingName: MiscApplication = MiscApplication(
    Contact("name", "email"),
    "",
    None,
    MiscCaseType.HARMONISED,
    None,
    sampleToBeProvided = false,
    sampleToBeReturned = false
  )

  val corrApplicationExample: CorrespondenceApplication = CorrespondenceApplication(
    Some("Starter"),
    Some("Agent 007"),
    Address("New building", "Old Town", None, None),
    Contact("a name", "anemail@some.com", None),
    None,
    "A short summary",
    "A detailed desc",
    None,
    sampleToBeProvided = false,
    sampleToBeReturned = false
  )

  val correspondenceCaseExample: Case = Case(
    "1",
    CaseStatus.OPEN,
    Instant.now(),
    0,
    None,
    None,
    None,
    corrApplicationExample,
    None,
    Seq(),
    Set.empty,
    Sample(),
    Some(Instant.now()),
    Some(5),
    referredDaysElapsed = 0
  )

  val miscellaneousCaseExample: Case = Case(
    "1",
    CaseStatus.OPEN,
    Instant.now(),
    0,
    Some("SOC/554/2015/JN"),
    None,
    None,
    miscExample,
    None,
    Seq(),
    Set.empty,
    Sample(),
    Some(Instant.now()),
    Some(5),
    referredDaysElapsed = 0
  )
}
