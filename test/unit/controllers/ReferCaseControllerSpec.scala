/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import connector.FakeDataCacheConnector
import models._
import models.request.FileStoreInitiateRequest
import models.response._
import models.ReferralReason.ReferralReason
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.{MimeTypes, Status}
import play.api.test.Helpers._
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import utils.JsonFormatters._
import views.html.{confirm_refer_case, refer_case_email, refer_case_reason}

import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

class ReferCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {
  private val casesService = mock[CasesService]
  private val fileService  = mock[FileStoreService]
  private val operator     = Operator(id = "id")

  private val referCaseReason  = injector.instanceOf[refer_case_reason]
  private val referCaseEmail   = injector.instanceOf[refer_case_email]
  private val confirmReferCase = injector.instanceOf[confirm_refer_case]

  private val caseWithStatusNEW      = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseWithStatusOPEN     = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val caseWithStatusREFERRED = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.REFERRED)

  private val initiateResponse = FileStoreInitiateResponse(
    id              = "id",
    upscanReference = "ref",
    uploadRequest = UpscanFormTemplate(
      "http://localhost:20001/upscan/upload",
      Map("key" -> "value")
    )
  )

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService, fileService)
    await(FakeDataCacheConnector.clear())
  }

  private def controller(requestedCase: Case) =
    new ReferCaseController(
      new SuccessfulRequestActions(playBodyParsers, operator, c = requestedCase),
      casesService,
      fileService,
      FakeDataCacheConnector,
      mcc,
      referCaseReason,
      referCaseEmail,
      confirmReferCase,
      realAppConfig
    )

  private def controller(requestCase: Case, permission: Set[Permission]) =
    new ReferCaseController(
      new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService,
      fileService,
      FakeDataCacheConnector,
      mcc,
      referCaseReason,
      referCaseEmail,
      confirmReferCase,
      realAppConfig
    )

  "GET refer case reason" should {

    "return OK and HTML content type" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.REFER_CASE))
          .getReferCaseReason(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result) should include(
        messages("change_case_status.referred.reason.heading", caseWithStatusOPEN.application.goodsName)
      )
    }

    "redirect to unauthorised when the user does not have right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set.empty)
          .getReferCaseReason(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }

  }

  "POST refer case reason" should {
    "redirect to refer case email page when filled correctly" in {
      val result = await(
        controller(caseWithStatusOPEN).postReferCaseReason(caseWithStatusOPEN.reference)(
          newFakePOSTRequestWithCSRF(
            Map(
              "referredTo" -> "Applicant",
              "reasons[0]" -> ReferralReason.REQUEST_SAMPLE.toString,
              "note"       -> "some-note"
            )
          )
        )
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        routes.ReferCaseController.getReferCaseEmail(caseWithStatusOPEN.reference).path
      )
    }

    "display error page when referent is missing" in {
      val result = await(
        controller(caseWithStatusOPEN).postReferCaseReason(caseWithStatusOPEN.reference)(
          newFakePOSTRequestWithCSRF(Map("note" -> "some-note"))
        )
      )

      status(result)        shouldBe Status.BAD_REQUEST
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messages("error.empty.refer.to"))
    }

    "display error page when referred to applicant and no reason is selected" in {
      val result = await(
        controller(caseWithStatusOPEN).postReferCaseReason(caseWithStatusOPEN.reference)(
          newFakePOSTRequestWithCSRF(Map("referredTo" -> "Applicant", "note" -> "some-note"))
        )
      )

      status(result)        shouldBe Status.BAD_REQUEST
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("Select why you are referring this case")
    }

    "display error page when referred to Other and no details of who is referred to are provided" in {
      val result = await(
        controller(caseWithStatusOPEN).postReferCaseReason(caseWithStatusOPEN.reference)(
          newFakePOSTRequestWithCSRF(Map("referredTo" -> "Other", "note" -> "some-note"))
        )
      )

      status(result)        shouldBe Status.BAD_REQUEST
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("Enter who you are referring this case to")
    }

    "display error page when note is missing" in {
      val result = await(
        controller(caseWithStatusOPEN).postReferCaseReason(caseWithStatusOPEN.reference)(
          newFakePOSTRequestWithCSRF(Map("referredTo" -> "LAB"))
        )
      )

      status(result)        shouldBe Status.BAD_REQUEST
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messages("error.empty.refer.note"))
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val result = await(
        controller(caseWithStatusNEW, Set.empty)
          .postReferCaseReason(caseWithStatusNEW.reference)(
            newFakePOSTRequestWithCSRF(
              Map(
                "referredTo" -> "APPLICANT",
                "reasons[0]" -> ReferralReason.REQUEST_SAMPLE.toString,
                "note"       -> "some-note"
              )
            )
          )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET refer case email" should {

    "return OK and HTML content type" in {
      given(fileService.initiate(any[FileStoreInitiateRequest])(any[HeaderCarrier])) willReturn successful(
        initiateResponse
      )

      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.REFER_CASE))
          .getReferCaseEmail(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result) should include(
        messages("change_case_status.referred.email.heading", caseWithStatusOPEN.application.goodsName)
      )
    }

    "redirect to unauthorised when the user does not have right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set.empty)
          .getReferCaseEmail(caseWithStatusOPEN.reference)(newFakeGETRequestWithCSRF())
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET refer case" should {

    "redirect to confirmation page" in {
      val cacheKey = s"refer_case-${caseWithStatusOPEN.reference}"
      val referral = CaseReferral("APPLICANT", List(ReferralReason.REQUEST_SAMPLE), "some-note", None)
      val cacheMap = UserAnswers(cacheKey).set("referral", referral).cacheMap
      await(FakeDataCacheConnector.save(cacheMap))

      given(
        casesService.referCase(
          refEq(caseWithStatusOPEN),
          refEq("APPLICANT"),
          refEq(Seq(ReferralReason.REQUEST_SAMPLE)),
          any[Attachment],
          any[String],
          any[Operator]
        )(any[HeaderCarrier])
      ) willReturn successful(caseWithStatusREFERRED)

      val result = await(
        controller(caseWithStatusOPEN).referCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        routes.ReferCaseController.confirmReferCase(caseWithStatusREFERRED.reference).path
      )
    }

    "ignore reasons when referred to is set to Lab Analyst" in {
      val cacheKey = s"refer_case-${caseWithStatusOPEN.reference}"
      val referral = CaseReferral(
        "Lab Analyst",
        List(ReferralReason.REQUEST_SAMPLE, ReferralReason.REQUEST_MORE_INFO),
        "some-note",
        None
      )
      val cacheMap = UserAnswers(cacheKey).set("referral", referral).cacheMap
      await(FakeDataCacheConnector.save(cacheMap))

      val captor = ArgumentCaptor.forClass(classOf[Seq[ReferralReason]])

      given(
        casesService.referCase(
          refEq(caseWithStatusOPEN),
          any[String],
          captor.capture(),
          any[Attachment],
          any[String],
          any[Operator]
        )(any[HeaderCarrier])
      ) willReturn successful(caseWithStatusREFERRED)

      val result = await(
        controller(caseWithStatusOPEN).referCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF()
        )
      )

      captor.getValue shouldBe Seq.empty
      status(result)  shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(
        routes.ReferCaseController.confirmReferCase(caseWithStatusREFERRED.reference).path
      )
    }

    "redirect to unauthorised when the user does not have any saved answers" in {
      val result = await(
        controller(caseWithStatusOPEN).referCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }

    "redirect to unauthorised when the user does not have the right permissions" in {
      val cacheKey = s"refer_case-${caseWithStatusOPEN.reference}"
      val referral = CaseReferral("APPLICANT", List(ReferralReason.REQUEST_SAMPLE), "some-note", None)
      val cacheMap = UserAnswers(cacheKey).set("referral", referral).cacheMap
      await(FakeDataCacheConnector.save(cacheMap))

      val result = await(
        controller(caseWithStatusOPEN, Set.empty).referCase(caseWithStatusOPEN.reference, "id")(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().path)
    }
  }

  "GET confirm refer case" should {
    "return OK and HTML content type" in {
      val result = await(
        controller(caseWithStatusREFERRED).confirmReferCase(caseWithStatusREFERRED.reference)(
          newFakeGETRequestWithCSRF()
        )
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result) should include(
        messages("case.referred.confirm_referred", caseWithStatusREFERRED.application.goodsName)
      )
    }
  }
}
