/*
 * Copyright 2025 HM Revenue & Customs
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

import base.SpecBase
import utils.JsonFormatters.*
import models.*
import models.ApplicationType.{ATAR, LIABILITY}
import reporting.*
import models.request.NewEventRequest
import models.response.{FileMetadata, ScanStatus}
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.time.Instant

class JsonFormattersSpec extends SpecBase {
  "InstantRange" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = new InstantRange(
          Instant.parse("2020-01-01T09:00:00.00Z"),
          Instant.parse("2021-01-01T09:00:00.00Z")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[InstantRange]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.validate[InstantRange] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("min" -> "ad", "max" -> 23)
        json.validate[InstantRange] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[InstantRange] shouldBe a[JsError]
      }
    }
  }
  "RepaymentClaim" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = RepaymentClaim(
          dvrNumber = Some("dff"),
          dateForRepayment = Some(Instant.parse("2021-01-01T09:00:00.00Z"))
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[RepaymentClaim]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original     = new RepaymentClaim
        val json         = Json.toJson(original)
        val deserialized = json.as[RepaymentClaim]

        deserialized shouldBe original
      }
    }
    "serialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[RepaymentClaim] shouldBe JsSuccess(RepaymentClaim())
      }
    }
    "fail to deserialize" when {
      "there is type mismatch" in {
        val json = Json.obj("dvrNumber" -> 1, "dateForRepayment" -> 23)
        json.validate[RepaymentClaim] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[RepaymentClaim] shouldBe a[JsError]
      }
    }
  }
  "Address" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = Address(
          buildingAndStreet = "asd",
          townOrCity = "asfdc",
          county = Some("dsf"),
          postCode = Some("sdf")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Address]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original     = new Address("", "", None, None)
        val json         = Json.toJson(original)
        val deserialized = json.as[Address]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[Address] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("buildingAndStreet" -> 3, "townOrCity" -> true, "county" -> 32, "postCode" -> 32)
        json.validate[Address] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[Address] shouldBe a[JsError]
      }
    }
  }
  "TraderContactDetails" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = TraderContactDetails(
          email = Some("fcsd"),
          phone = Some("sdf"),
          address = Some(
            Address(
              buildingAndStreet = "asd",
              townOrCity = "asfdc",
              county = Some("dsf"),
              postCode = Some("sdf")
            )
          )
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[TraderContactDetails]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original     = new TraderContactDetails(None, None, None)
        val json         = Json.toJson(original)
        val deserialized = json.as[TraderContactDetails]

        deserialized shouldBe original
      }
    }
    "deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.as[TraderContactDetails] shouldBe TraderContactDetails(None, None, None)
      }
    }
    "fail to deserialize" when {
      "there is type mismatch" in {
        val json = Json.obj("email" -> true, "phone" -> 2, "address" -> "dsf")
        json.validate[TraderContactDetails] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[TraderContactDetails] shouldBe a[JsError]
      }
    }
  }
  "Operator" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = Operator(
          id = "fsdf",
          name = Some("efdd"),
          email = Some("sfed"),
          role = Role.READ_ONLY,
          memberOfTeams = Seq("ads", "saf"),
          managerOfTeams = Seq("ads", "saf"),
          permissions = Set.empty,
          deleted = true
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Operator]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original     = new Operator("dsacd")
        val json         = Json.toJson(original)
        val deserialized = json.as[Operator]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[InstantRange] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj(
          "email"          -> true,
          "id"             -> 32,
          "name"           -> 34,
          "email"          -> 23,
          "role"           -> "dfs",
          "memberOfTeams"  -> 3,
          "managerOfTeams" -> "sdf",
          "permissions"    -> false,
          "deleted"        -> 2
        )
        json.validate[InstantRange] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[InstantRange] shouldBe a[JsError]
      }
    }
  }
  "NewUserRequest" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original     = new NewUserRequest(Operator("sdf"))
        val json         = Json.toJson(original)
        val deserialized = json.as[NewUserRequest]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[NewUserRequest] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("operator" -> 3)
        json.validate[NewUserRequest] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("a" -> "b", "c" -> 1)
        json.validate[NewUserRequest] shouldBe a[JsError]
      }
    }
  }
  "Attachment" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = new Attachment(
          id = "asd",
          public = true,
          operator = Some(Operator("asd")),
          timestamp = Instant.parse("2020-01-01T09:00:00.00Z"),
          description = Some("fsd"),
          shouldPublishToRulings = true
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Attachment]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = new Attachment(
          id = "asd",
          operator = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Attachment]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.validate[Attachment] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("id" -> 23, "operator" -> 432)
        json.validate[Attachment] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("a" -> "b", "c" -> 2)
        json.validate[Attachment] shouldBe a[JsError]
      }
    }
  }
  "Appeal" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = new Appeal(
          id = "asd",
          status = AppealStatus.ALLOWED,
          `type` = AppealType.APPEAL_TIER_1
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Appeal]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.validate[Appeal] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("id" -> 32, "status" -> "AppealStatus.ALLOWED", "type" -> "AppealType.APPEAL_TIER_1")
        json.validate[Appeal] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("a" -> "b", "c" -> 23)
        json.validate[Appeal] shouldBe a[JsError]
      }
    }
  }
  "Cancellation" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = new Cancellation(
          reason = CancelReason.OTHER,
          applicationForExtendedUse = true
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Cancellation]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.validate[Cancellation] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("reason" -> "ad", "applicationForExtendedUse" -> 23)
        json.validate[Cancellation] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[Cancellation] shouldBe a[JsError]
      }
    }
  }
  "Contact" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = new Contact(
          name = "wds",
          email = "sdaf",
          phone = Some("sdf")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Contact]

        deserialized shouldBe original
      }
      "phone is none" in {
        val original = new Contact(
          name = "wds",
          email = "sdaf",
          phone = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Contact]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.validate[Contact] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("name" -> 32, "email" -> 324)
        json.validate[Contact] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[Contact] shouldBe a[JsError]
      }
    }
  }
  "EORIDetails" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = new EORIDetails(
          eori = "sfd",
          businessName = "sdf",
          addressLine1 = "sdf",
          addressLine2 = "sdfd",
          addressLine3 = "dsf",
          postcode = "sdf",
          country = "sdf"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[EORIDetails]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.validate[EORIDetails] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("min" -> "ad", "max" -> 23)
        json.validate[EORIDetails] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[EORIDetails] shouldBe a[JsError]
      }
    }
  }
  "Decision" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = new Decision(
          bindingCommodityCode = "dsff",
          effectiveStartDate = Some(Instant.parse("2020-01-01T09:00:00.00Z")),
          effectiveEndDate = Some(Instant.parse("2020-01-01T09:00:00.00Z")),
          justification = "fds",
          goodsDescription = "sdf",
          methodSearch = Some("sdf"),
          methodExclusion = Some("sdf"),
          methodCommercialDenomination = Some("sdf"),
          appeal = Seq(Appeal("dsaf", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_2)),
          cancellation = Some(Cancellation(CancelReason.OTHER)),
          explanation = Some("wedw"),
          decisionPdf = Some(Attachment("asdd", true, None, Instant.parse("2020-01-01T09:00:00.00Z"))),
          letterPdf = Some(Attachment("asdd", true, None, Instant.parse("2020-01-01T09:00:00.00Z")))
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Decision]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = new Decision(
          bindingCommodityCode = "dsff",
          effectiveStartDate = None,
          effectiveEndDate = None,
          justification = "fds",
          goodsDescription = "sdf",
          methodSearch = None,
          methodExclusion = None,
          methodCommercialDenomination = None,
          appeal = Seq.empty,
          cancellation = None,
          decisionPdf = None,
          letterPdf = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Decision]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.validate[Decision] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("bindingCommodityCode" -> true, "justification" -> 23, "goodsDescription" -> 32)
        json.validate[Decision] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[Decision] shouldBe a[JsError]
      }
    }
  }
  "Sample" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = new Sample(
          status = Some(SampleStatus(3)),
          requestedBy = Some(Operator("asd")),
          returnStatus = Some(SampleReturn.NO),
          whoIsSending = Some(SampleSend.AGENT)
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Sample]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = new Sample(
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Sample]

        deserialized shouldBe original
      }
    }
    "serialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.as[Sample] shouldBe Sample()
      }
    }
    "fail to deserialize" when {
      "there is type mismatch" in {
        val json = Json.obj("status" -> 23, "requestedBy" -> 321, "returnStatus" -> 42, "whoIsSending" -> 34)
        json.validate[Sample] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[Sample] shouldBe a[JsError]
      }
    }
  }
  "AgentDetails" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = AgentDetails(
          eoriDetails = EORIDetails(
            eori = "sdf",
            businessName = "fdds",
            addressLine1 = "sdfd",
            addressLine2 = "sdfsd",
            addressLine3 = "dsfds",
            postcode = "dfds",
            country = "sdf"
          ),
          letterOfAuthorisation = Some(Attachment(id = "sda", operator = None))
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[AgentDetails]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = AgentDetails(
          eoriDetails = EORIDetails(
            eori = "sdf",
            businessName = "fdds",
            addressLine1 = "sdfd",
            addressLine2 = "sdfsd",
            addressLine3 = "dsfds",
            postcode = "dfds",
            country = "sdf"
          ),
          letterOfAuthorisation = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[AgentDetails]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[AgentDetails] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("eoriDetails" -> 1, "letterOfAuthorisation" -> 23)
        json.validate[AgentDetails] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[AgentDetails] shouldBe a[JsError]
      }
    }
  }
  "Message" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = new Message(
          name = "dfsdf",
          date = Instant.parse("2021-01-01T09:00:00.00Z"),
          message = "ddfg"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Message]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.validate[Message] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("name" -> 3, "date" -> "ad", "message" -> 23)
        json.validate[Message] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[Message] shouldBe a[JsError]
      }
    }
  }
  "Keyword" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = Keyword(
          name = "sdf",
          approved = true
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Keyword]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[Keyword] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("name" -> 1, "approved" -> 23)
        json.validate[Keyword] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[Keyword] shouldBe a[JsError]
      }
    }
  }
  "LiabilityOrder" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = LiabilityOrder(
          contact = Contact(name = "efwef", email = "wefd"),
          status = LiabilityStatus.LIVE,
          traderName = "afd",
          goodName = Some("dfsd"),
          entryDate = Option(Instant.parse("2020-01-01T09:00:00.00Z")),
          entryNumber = Some("fds"),
          traderCommodityCode = Some("sdfsd"),
          officerCommodityCode = Some("sdf"),
          btiReference = Some("sdf"),
          repaymentClaim = Some(RepaymentClaim()),
          dateOfReceipt = Option(Instant.parse("2020-01-01T09:00:00.00Z")),
          traderContactDetails = Some(TraderContactDetails(email = Some("sdf"), phone = Some("dsfds"), address = None)),
          agentName = Some("dfs"),
          port = Some("sdf")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[LiabilityOrder]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = new LiabilityOrder(
          contact = Contact(name = "efwef", email = "wefd"),
          status = LiabilityStatus.LIVE,
          traderName = "afd"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[LiabilityOrder]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[LiabilityOrder] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj("contact" -> 1, "status" -> 23, "tradreName" -> 23)
        json.validate[LiabilityOrder] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[LiabilityOrder] shouldBe a[JsError]
      }
    }
  }
  "CorrespondenceApplication" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = CorrespondenceApplication(
          correspondenceStarter = Some("fsd"),
          agentName = Some("dsfs"),
          address = Address("s", "ewfs", Some("sdsad"), Some("DSF")),
          contact = Contact(name = "efwef", email = "wefd"),
          fax = Some("sdC"),
          summary = "sdfcx",
          detailedDescription = "sdfsd",
          relatedBTIReference = Some("sdf"),
          relatedBTIReferences = List("ddsd", "asd"),
          sampleToBeProvided = true,
          sampleToBeReturned = true,
          messagesLogged =
            List(Message(name = "sds", date = Instant.parse("2020-01-01T09:00:00.00Z"), message = "sdasd"))
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CorrespondenceApplication]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = new CorrespondenceApplication(
          correspondenceStarter = None,
          agentName = None,
          address = Address("s", "ewfs", None, None),
          contact = Contact(name = "efwef", email = "wefd"),
          fax = None,
          summary = "sdfcx",
          detailedDescription = "sdfsd",
          relatedBTIReference = None,
          relatedBTIReferences = List.empty,
          sampleToBeProvided = true,
          sampleToBeReturned = true,
          messagesLogged = List.empty
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CorrespondenceApplication]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[CorrespondenceApplication] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj(
          "address"             -> 1,
          "contact"             -> 23,
          "summary"             -> 23,
          "detailedDescription" -> 23,
          "sampleToBeProvided"  -> 23,
          "sampleToBeReturned"  -> 23
        )
        json.validate[CorrespondenceApplication] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CorrespondenceApplication] shouldBe a[JsError]
      }
    }
  }
  "MiscApplication" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = MiscApplication(
          contact = Contact(name = "efwef", email = "wefd"),
          name = "sdf",
          contactName = Some("adcs"),
          caseType = MiscCaseType.OTHER,
          detailedDescription = Some("sdc"),
          sampleToBeProvided = true,
          sampleToBeReturned = true,
          messagesLogged =
            List(Message(name = "sds", date = Instant.parse("2020-01-01T09:00:00.00Z"), message = "sdasd"))
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[MiscApplication]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = new MiscApplication(
          contact = Contact(name = "efwef", email = "wefd"),
          name = "sdf",
          contactName = None,
          caseType = MiscCaseType.OTHER,
          detailedDescription = None,
          sampleToBeProvided = false,
          sampleToBeReturned = false,
          messagesLogged = List.empty
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[MiscApplication]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[MiscApplication] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj(
          "contact"             -> 1,
          "name"                -> 23,
          "caseType"            -> 23,
          "detailedDescription" -> 23,
          "sampleToBeProvided"  -> 23,
          "sampleToBeReturned"  -> 23
        )
        json.validate[MiscApplication] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[MiscApplication] shouldBe a[JsError]
      }
    }
  }
  "BTIApplication" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = BTIApplication(
          holder = EORIDetails(
            eori = "sfd",
            businessName = "sdf",
            addressLine1 = "sdf",
            addressLine2 = "sdfd",
            addressLine3 = "dsf",
            postcode = "sdf",
            country = "sdf"
          ),
          contact = Contact(name = "efwef", email = "wefd"),
          agent = Some(
            AgentDetails(
              eoriDetails = EORIDetails(
                eori = "sdf",
                businessName = "fdds",
                addressLine1 = "sdfd",
                addressLine2 = "sdfsd",
                addressLine3 = "dsfds",
                postcode = "dfds",
                country = "sdf"
              ),
              letterOfAuthorisation = Some(Attachment(id = "sda", operator = None))
            )
          ),
          offline = true,
          goodName = "sdd",
          goodDescription = "sdfsd",
          confidentialInformation = Some("sdf"),
          otherInformation = Some("fsdf"),
          reissuedBTIReference = Some("sfd"),
          relatedBTIReference = Some("sdds"),
          relatedBTIReferences = List("sfd", "sdf"),
          knownLegalProceedings = Some("sdf"),
          envisagedCommodityCode = Some("sdf"),
          sampleToBeProvided = true,
          sampleToBeReturned = true,
          applicationPdf = Some(
            Attachment(
              id = "asd",
              public = true,
              operator = Some(Operator("asd")),
              timestamp = Instant.parse("2020-01-01T09:00:00.00Z"),
              description = Some("fsd"),
              shouldPublishToRulings = true
            )
          )
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[BTIApplication]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = new BTIApplication(
          holder = EORIDetails(
            eori = "sfd",
            businessName = "sdf",
            addressLine1 = "sdf",
            addressLine2 = "sdfd",
            addressLine3 = "dsf",
            postcode = "sdf",
            country = "sdf"
          ),
          contact = Contact(name = "efwef", email = "wefd"),
          offline = true,
          goodName = "sdd",
          goodDescription = "sdfsd",
          confidentialInformation = None,
          otherInformation = None,
          reissuedBTIReference = None,
          knownLegalProceedings = None,
          envisagedCommodityCode = None,
          sampleToBeProvided = true,
          sampleToBeReturned = true,
          applicationPdf = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[BTIApplication]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[BTIApplication] shouldBe a[JsError]
      }
      "there is type mismatch" in {
        val json = Json.obj(
          "holder"              -> 23,
          "contact"             -> 1,
          "name"                -> 23,
          "caseType"            -> 23,
          "detailedDescription" -> 23,
          "sampleToBeProvided"  -> 23,
          "sampleToBeReturned"  -> 23
        )
        json.validate[BTIApplication] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[BTIApplication] shouldBe a[JsError]
      }
    }
  }
  "Case" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = Case(
          reference = "asd",
          status = CaseStatus.REFERRED,
          createdDate = Instant.parse("2020-01-01T09:00:00.00Z"),
          daysElapsed = 2,
          caseBoardsFileNumber = Some("sdf"),
          assignee = Some(Operator("dsacd")),
          queueId = Some("dfss"),
          application = BTIApplication(
            holder = EORIDetails(
              eori = "sfd",
              businessName = "sdf",
              addressLine1 = "sdf",
              addressLine2 = "sdfd",
              addressLine3 = "dsf",
              postcode = "sdf",
              country = "sdf"
            ),
            contact = Contact(name = "efwef", email = "wefd"),
            offline = true,
            goodName = "sdd",
            goodDescription = "sdfsd",
            confidentialInformation = None,
            otherInformation = None,
            reissuedBTIReference = None,
            knownLegalProceedings = None,
            envisagedCommodityCode = None,
            sampleToBeProvided = true,
            sampleToBeReturned = true,
            applicationPdf = None
          ),
          decision = Some(
            Decision(
              bindingCommodityCode = "dsff",
              effectiveStartDate = None,
              effectiveEndDate = None,
              justification = "fds",
              goodsDescription = "sdf",
              methodSearch = None,
              methodExclusion = None,
              methodCommercialDenomination = None,
              appeal = Seq.empty,
              cancellation = None,
              decisionPdf = None,
              letterPdf = None
            )
          ),
          attachments = Seq(
            Attachment(
              id = "asd",
              operator = None
            )
          ),
          keywords = Set("dfs"),
          sample = Sample(),
          dateOfExtract = Some(Instant.parse("2020-01-01T09:00:00.00Z")),
          migratedDaysElapsed = Some(4),
          referredDaysElapsed = 2
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Case]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = Case(
          reference = "asd",
          status = CaseStatus.REFERRED,
          createdDate = Instant.parse("2020-01-01T09:00:00.00Z"),
          daysElapsed = 2,
          caseBoardsFileNumber = None,
          assignee = None,
          queueId = None,
          application = BTIApplication(
            holder = EORIDetails(
              eori = "sfd",
              businessName = "sdf",
              addressLine1 = "sdf",
              addressLine2 = "sdfd",
              addressLine3 = "dsf",
              postcode = "sdf",
              country = "sdf"
            ),
            contact = Contact(name = "efwef", email = "wefd"),
            offline = true,
            goodName = "sdd",
            goodDescription = "sdfsd",
            confidentialInformation = None,
            otherInformation = None,
            reissuedBTIReference = None,
            knownLegalProceedings = None,
            envisagedCommodityCode = None,
            sampleToBeProvided = true,
            sampleToBeReturned = true,
            applicationPdf = None
          ),
          decision = None,
          attachments = Seq.empty,
          keywords = Set.empty,
          sample = Sample(),
          dateOfExtract = None,
          migratedDaysElapsed = None,
          referredDaysElapsed = 2
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Case]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[Case] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[Case] shouldBe a[JsError]
      }
    }
  }
  "NewCaseRequest" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = NewCaseRequest(
          application = MiscApplication(
            contact = Contact(name = "efwef", email = "wefd"),
            name = "sdf",
            contactName = Some("adcs"),
            caseType = MiscCaseType.OTHER,
            detailedDescription = Some("sdc"),
            sampleToBeProvided = true,
            sampleToBeReturned = true,
            messagesLogged =
              List(Message(name = "sds", date = Instant.parse("2020-01-01T09:00:00.00Z"), message = "sdasd"))
          ),
          attachments = Seq(
            Attachment(
              id = "asd",
              operator = None
            )
          )
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[NewCaseRequest]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = NewCaseRequest(
          application = MiscApplication(
            contact = Contact(name = "efwef", email = "wefd"),
            name = "sdf",
            contactName = Some("adcs"),
            caseType = MiscCaseType.OTHER,
            detailedDescription = Some("sdc"),
            sampleToBeProvided = true,
            sampleToBeReturned = true,
            messagesLogged =
              List(Message(name = "sds", date = Instant.parse("2020-01-01T09:00:00.00Z"), message = "sdasd"))
          ),
          attachments = Seq.empty
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[NewCaseRequest]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[NewCaseRequest] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[NewCaseRequest] shouldBe a[JsError]
      }
    }
  }
  "NewKeywordRequest" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = NewKeywordRequest(
          keyword = Keyword("sdf")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[NewKeywordRequest]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[NewKeywordRequest] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[NewKeywordRequest] shouldBe a[JsError]
      }
    }
  }
  "CaseStatusChange" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = CaseStatusChange(
          from = CaseStatus.REFERRED,
          to = CaseStatus.CANCELLED,
          comment = Some("asd"),
          attachmentId = Some("sdf")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CaseStatusChange]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = CaseStatusChange(
          from = CaseStatus.REFERRED,
          to = CaseStatus.CANCELLED,
          comment = None,
          attachmentId = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CaseStatusChange]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[CaseStatusChange] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CaseStatusChange] shouldBe a[JsError]
      }
    }
  }
  "CancellationCaseStatusChange" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = CancellationCaseStatusChange(
          from = CaseStatus.REFERRED,
          comment = Some("asd"),
          attachmentId = Some("sdf"),
          reason = CancelReason.OTHER
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CancellationCaseStatusChange]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = CancellationCaseStatusChange(
          from = CaseStatus.REFERRED,
          reason = CancelReason.OTHER,
          comment = None,
          attachmentId = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CancellationCaseStatusChange]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[CancellationCaseStatusChange] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CancellationCaseStatusChange] shouldBe a[JsError]
      }
    }
  }
  "ReferralCaseStatusChange" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = ReferralCaseStatusChange(
          from = CaseStatus.REFERRED,
          comment = Some("asd"),
          attachmentId = Some("sdf"),
          referredTo = "dsdf",
          reason = Seq(ReferralReason.REQUEST_SAMPLE)
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[ReferralCaseStatusChange]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = ReferralCaseStatusChange(
          from = CaseStatus.REFERRED,
          comment = None,
          attachmentId = None,
          referredTo = "dsdf",
          reason = Seq.empty
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[ReferralCaseStatusChange]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[ReferralCaseStatusChange] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[ReferralCaseStatusChange] shouldBe a[JsError]
      }
    }
  }
  "RejectCaseStatusChange" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = RejectCaseStatusChange(
          from = CaseStatus.REFERRED,
          comment = Some("asd"),
          attachmentId = Some("sdf"),
          to = CaseStatus.COMPLETED,
          reason = RejectReason.DUPLICATE_APPLICATION
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[RejectCaseStatusChange]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = RejectCaseStatusChange(
          from = CaseStatus.REFERRED,
          comment = None,
          attachmentId = None,
          to = CaseStatus.COMPLETED,
          reason = RejectReason.DUPLICATE_APPLICATION
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[RejectCaseStatusChange]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[RejectCaseStatusChange] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[RejectCaseStatusChange] shouldBe a[JsError]
      }
    }
  }
  "CompletedCaseStatusChange" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = CompletedCaseStatusChange(
          from = CaseStatus.REFERRED,
          comment = Some("asd"),
          email = Some("sdfs")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CompletedCaseStatusChange]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = CompletedCaseStatusChange(
          from = CaseStatus.REFERRED,
          comment = None,
          email = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CompletedCaseStatusChange]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[CompletedCaseStatusChange] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CompletedCaseStatusChange] shouldBe a[JsError]
      }
    }
  }
  "AppealStatusChange" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = AppealStatusChange(
          from = AppealStatus.ALLOWED,
          to = AppealStatus.DISMISSED,
          comment = Some("asd"),
          appealType = AppealType.ADR
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[AppealStatusChange]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = AppealStatusChange(
          from = AppealStatus.ALLOWED,
          to = AppealStatus.DISMISSED,
          comment = None,
          appealType = AppealType.ADR
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[AppealStatusChange]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[AppealStatusChange] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[AppealStatusChange] shouldBe a[JsError]
      }
    }
  }
  "SampleStatusChange" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = SampleStatusChange(
          from = Some(SampleStatus.NONE),
          to = Some(SampleStatus.STORAGE),
          comment = Some("asd")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[SampleStatusChange]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = SampleStatusChange(
          from = None,
          to = None,
          comment = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[SampleStatusChange]

        deserialized shouldBe original
      }
    }
    "serialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.as[SampleStatusChange] shouldBe SampleStatusChange(None, None, None)
      }
    }
    "fail to deserialize" when {
      "json is type mismatch" in {
        val json = Json.obj("from" -> 24, "to" -> 32, "comment" -> 23)
        json.validate[SampleStatusChange] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[SampleStatusChange] shouldBe a[JsError]
      }
    }
  }
  "SampleReturnChange" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = SampleReturnChange(
          from = Some(SampleReturn.TO_BE_CONFIRMED),
          to = Some(SampleReturn.NO),
          comment = Some("asd")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[SampleReturnChange]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = SampleReturnChange(
          from = None,
          to = None,
          comment = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[SampleReturnChange]

        deserialized shouldBe original
      }
    }
    "deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.as[SampleReturnChange] shouldBe SampleReturnChange(
          from = None,
          to = None,
          comment = None
        )
      }
    }
    "fail to deserialize" when {
      "there is type mismatch" in {
        val json = Json.obj("from" -> 3, "to" -> 2, "comment" -> 4)
        json.validate[SampleReturnChange] shouldBe a[JsError]

      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[SampleReturnChange] shouldBe a[JsError]
      }
    }
  }
  "SampleSendChange" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = SampleSendChange(
          from = Some(SampleSend.TRADER),
          to = Some(SampleSend.AGENT),
          comment = Some("asd")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[SampleSendChange]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = SampleSendChange(
          from = None,
          to = None,
          comment = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[SampleSendChange]

        deserialized shouldBe original
      }
    }
    "deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.as[SampleSendChange] shouldBe SampleSendChange(
          from = None,
          to = None,
          comment = None
        )
      }
    }
    "fail to deserialize" when {
      "there is type mismatch" in {
        val json = Json.obj("from" -> 3, "to" -> 2, "comment" -> 4)
        json.validate[SampleSendChange] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[SampleSendChange] shouldBe a[JsError]
      }
    }
  }
  "AppealAdded" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = AppealAdded(
          appealType = AppealType.APPEAL_TIER_2,
          appealStatus = AppealStatus.ALLOWED,
          comment = Some("asd")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[AppealAdded]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = AppealAdded(
          appealType = AppealType.APPEAL_TIER_2,
          appealStatus = AppealStatus.ALLOWED,
          comment = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[AppealAdded]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[AppealAdded] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[AppealAdded] shouldBe a[JsError]
      }
    }
  }
  "ExtendedUseStatusChange" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = ExtendedUseStatusChange(
          from = true,
          to = true,
          comment = Some("asd")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[ExtendedUseStatusChange]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = ExtendedUseStatusChange(
          false,
          false,
          None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[ExtendedUseStatusChange]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[ExtendedUseStatusChange] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[ExtendedUseStatusChange] shouldBe a[JsError]
      }
    }
  }
  "AssignmentChange" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = AssignmentChange(
          from = Some(Operator("sad")),
          to = Some(Operator("sd")),
          comment = Some("asd")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[AssignmentChange]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = AssignmentChange(
          None,
          None,
          None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[AssignmentChange]

        deserialized shouldBe original
      }
    }
    "deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.as[AssignmentChange] shouldBe AssignmentChange(
          None,
          None,
          None
        )
      }
    }
    "fail to deserialize" when {
      "there is type mismatch" in {
        val json = Json.obj("from" -> 3, "to" -> 2, "comment" -> 4)
        json.validate[AssignmentChange] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[AssignmentChange] shouldBe a[JsError]
      }
    }
  }
  "QueueChange" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = QueueChange(
          from = Some("asd"),
          to = Some("edfd"),
          comment = Some("asd")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[QueueChange]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = QueueChange(
          None,
          None,
          None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[QueueChange]

        deserialized shouldBe original
      }
    }
    "deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.as[QueueChange] shouldBe QueueChange(
          None,
          None,
          None
        )
      }
    }
    "fail to deserialize" when {
      "there is type mismatch" in {
        val json = Json.obj("from" -> 3, "to" -> 2, "comment" -> 4)
        json.validate[QueueChange] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[QueueChange] shouldBe a[JsError]
      }
    }
  }
  "CaseCreated" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = CaseCreated(
          comment = "asd"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CaseCreated]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[CaseCreated] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CaseCreated] shouldBe a[JsError]
      }
    }
  }
  "ExpertAdviceReceived" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = ExpertAdviceReceived(
          comment = "asd"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[ExpertAdviceReceived]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[ExpertAdviceReceived] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[ExpertAdviceReceived] shouldBe a[JsError]
      }
    }
  }
  "Note" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = Note(
          comment = "asd"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Note]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[Note] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[Note] shouldBe a[JsError]
      }
    }
  }
  "FileMetadata" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = FileMetadata(
          id = "sdf",
          fileName = Some("sdf"),
          mimeType = Some("sdfs"),
          url = Some("sfd"),
          scanStatus = Some(ScanStatus.READY)
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[FileMetadata]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = FileMetadata(
          "adfs",
          None,
          None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[FileMetadata]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[FileMetadata] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[FileMetadata] shouldBe a[JsError]
      }
    }
  }
  "Event" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = new Event(
          id = "dsdf",
          details = CaseCreated("dfsd"),
          operator = Operator("dscd"),
          caseReference = "sdf",
          timestamp = Instant.parse("2021-01-01T09:00:00.00Z")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[Event]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.validate[Event] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[Event] shouldBe a[JsError]
      }
    }
  }
  "NewEventRequest" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = NewEventRequest(
          details = CaseCreated("dfsd"),
          operator = Operator("dscd"),
          timestamp = Instant.parse("2021-01-01T09:00:00.00Z")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[NewEventRequest]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "maxFileSize is not present" in {
        val json = Json.obj()
        json.validate[NewEventRequest] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[NewEventRequest] shouldBe a[JsError]
      }
    }
  }
  "CaseHeader" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = CaseHeader(
          reference = "sdf",
          assignee = Some(Operator("ds")),
          team = Some("sdf"),
          goodsName = Some("sdf"),
          caseType = ATAR,
          status = CaseStatus.CANCELLED,
          daysElapsed = 3,
          liabilityStatus = Some(LiabilityStatus.LIVE)
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CaseHeader]

        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = CaseHeader(
          reference = "sdf",
          assignee = None,
          team = None,
          goodsName = None,
          caseType = ATAR,
          status = CaseStatus.CANCELLED,
          daysElapsed = 3,
          liabilityStatus = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CaseHeader]

        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[CaseHeader] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CaseHeader] shouldBe a[JsError]
      }
    }
  }
  "CaseKeyword" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = CaseKeyword(
          Keyword("sdf", true),
          List(
            CaseHeader(
              reference = "sdf",
              assignee = None,
              team = None,
              goodsName = None,
              caseType = ATAR,
              status = CaseStatus.CANCELLED,
              daysElapsed = 3,
              liabilityStatus = None
            )
          )
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CaseKeyword]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[CaseKeyword] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CaseKeyword] shouldBe a[JsError]
      }
    }
  }
  "CaseCompletedEmailParameters" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = CaseCompletedEmailParameters(
          recipientName_line1 = "sdfs",
          reference = "sdf",
          goodsName = "sdf",
          officerName = "Fsadf",
          dateSubmitted = "sadf"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CaseCompletedEmailParameters]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[CaseCompletedEmailParameters] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CaseCompletedEmailParameters] shouldBe a[JsError]
      }
    }
  }
  "CaseCompletedEmail" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = CaseCompletedEmail(
          to = Seq("sdf"),
          parameters = CaseCompletedEmailParameters(
            recipientName_line1 = "sdfs",
            reference = "sdf",
            goodsName = "sdf",
            officerName = "Fsadf",
            dateSubmitted = "sadf"
          )
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CaseCompletedEmail]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[CaseCompletedEmail] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CaseCompletedEmail] shouldBe a[JsError]
      }
    }
  }
  "EmailTemplate" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = EmailTemplate(
          plain = "wefd",
          html = "ndf",
          fromAddress = "dgds",
          subject = "sgf",
          service = "sgfds"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[EmailTemplate]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[EmailTemplate] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[EmailTemplate] shouldBe a[JsError]
      }
    }
  }
  "NumberField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = NumberField(
          fieldName = "sdfds"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[NumberField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[NumberField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[NumberField] shouldBe a[JsError]
      }
    }
  }
  "StatusField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = StatusField(
          fieldName = "sdfds"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[StatusField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[StatusField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[StatusField] shouldBe a[JsError]
      }
    }
  }
  "LiabilityStatusField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = LiabilityStatusField(
          fieldName = "sdfds"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[LiabilityStatusField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[LiabilityStatusField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[LiabilityStatusField] shouldBe a[JsError]
      }
    }
  }
  "CaseTypeField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = CaseTypeField(
          fieldName = "sdfds"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CaseTypeField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[CaseTypeField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CaseTypeField] shouldBe a[JsError]
      }
    }
  }
  "ChapterField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = ChapterField(
          fieldName = "sdfds"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[ChapterField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[ChapterField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[ChapterField] shouldBe a[JsError]
      }
    }
  }
  "DateField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = DateField(
          fieldName = "sdfds"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[DateField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[DateField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[DateField] shouldBe a[JsError]
      }
    }
  }
  "StringField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = StringField(
          fieldName = "sdfds"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[StringField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[StringField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[StringField] shouldBe a[JsError]
      }
    }
  }
  "DaysSinceField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = DaysSinceField(
          fieldName = "sdfds"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[DaysSinceField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[DaysSinceField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[DaysSinceField] shouldBe a[JsError]
      }
    }
  }
  "NumberResultField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = NumberResultField(
          fieldName = "sdfds",
          data = Some(2)
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[NumberResultField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[NumberResultField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[NumberResultField] shouldBe a[JsError]
      }
    }
  }
  "StatusResultField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = StatusResultField(
          fieldName = "sdfds",
          data = Some(PseudoCaseStatus.REFERRED)
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[StatusResultField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[StatusResultField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[StatusResultField] shouldBe a[JsError]
      }
    }
  }
  "LiabilityStatusResultField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = LiabilityStatusResultField(
          fieldName = "sdfds",
          data = Some(LiabilityStatus.LIVE)
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[LiabilityStatusResultField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[LiabilityStatusResultField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[LiabilityStatusResultField] shouldBe a[JsError]
      }
    }
  }
  "CaseTypeResultField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = CaseTypeResultField(
          fieldName = "sdfds",
          data = Some(ApplicationType.ATAR)
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CaseTypeResultField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[CaseTypeResultField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CaseTypeResultField] shouldBe a[JsError]
      }
    }
  }
  "DateResultField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = DateResultField(
          fieldName = "sdfds",
          data = Some(Instant.parse("2021-01-01T09:00:00.00Z"))
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[DateResultField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[DateResultField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[DateResultField] shouldBe a[JsError]
      }
    }
  }
  "StringResultField" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = StringResultField(
          fieldName = "sdfds",
          data = Some("sdf")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[StringResultField]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[StringResultField] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[StringResultField] shouldBe a[JsError]
      }
    }
  }
  "QueueResultGroup" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = QueueResultGroup(
          count = 2,
          team = Some("sd"),
          caseType = LIABILITY
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[QueueResultGroup]
        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = QueueResultGroup(
          count = 2,
          team = None,
          caseType = LIABILITY
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[QueueResultGroup]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[QueueResultGroup] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[QueueResultGroup] shouldBe a[JsError]
      }
    }
  }
  "CaseReferral" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = CaseReferral(
          referredTo = "sdd",
          reasons = List(ReferralReason.REQUEST_SAMPLE),
          note = "sfdf",
          referManually = Some("sdsd")
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CaseReferral]
        deserialized shouldBe original
      }
      "all the values are None" in {
        val original = CaseReferral(
          referredTo = "sdd",
          reasons = List.empty,
          note = "sfdf",
          referManually = None
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CaseReferral]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[CaseReferral] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CaseReferral] shouldBe a[JsError]
      }
    }
  }
  "CaseRejection" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = CaseRejection(
          reason = RejectReason.OTHER,
          note = "sds"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[CaseRejection]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[CaseRejection] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[CaseRejection] shouldBe a[JsError]
      }
    }
  }
  "RulingCancellation" should {
    "handle round-trip serialization/deserialization" when {
      "all the values are present" in {
        val original = RulingCancellation(
          cancelReason = "sad",
          note = "sdcd"
        )
        val json         = Json.toJson(original)
        val deserialized = json.as[RulingCancellation]
        deserialized shouldBe original
      }
    }
    "fail to deserialize" when {
      "json is empty" in {
        val json = Json.obj()
        json.validate[RulingCancellation] shouldBe a[JsError]
      }
      "there is a json array" in {
        val json = Json.arr("min" -> "ad", "max" -> 23)
        json.validate[RulingCancellation] shouldBe a[JsError]
      }
    }
  }
}
