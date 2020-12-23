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

package controllers

import models.{Permission, _}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.{MimeTypes, Status}
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{MultipartFormData, Result}
import play.api.test.Helpers.{redirectLocation, _}
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

class SuspendCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val operator     = mock[Operator]

  private val caseWithStatusNEW  = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val caseWithStatusSUSPENDED =
    Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.SUSPENDED)

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  private def controller(c: Case) =
    new SuspendCaseController(
      new SuccessfulRequestActions(playBodyParsers, operator, c = c),
      casesService,
      mcc,
      realAppConfig
    )

  private def controller(requestCase: Case, permission: Set[Permission]) =
    new SuspendCaseController(
      new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService,
      mcc,
      realAppConfig
    )

  "Suspend Case" should {

    "return OK and HTML content type" in {

      val result: Result =
        await(controller(caseWithStatusOPEN).getSuspendCase("reference")(newFakeGETRequestWithCSRF(app)))

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("Change case status to: Suspended")
    }

    "return OK when user has right permissions" in {
      val result: Result = await(
        controller(caseWithStatusOPEN, Set(Permission.SUSPEND_CASE))
          .getSuspendCase("reference")(newFakeGETRequestWithCSRF(app))
      )

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(
        controller(caseWithStatusNEW, Set.empty)
          .getSuspendCase("reference")(newFakeGETRequestWithCSRF(app))
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Post Confirm Suspend a Case" should {

    def aMultipartFileWithParams(
      contentType: String,
      params: (String, Seq[String])*
    ): MultipartFormData[TemporaryFile] = {
      val file     = SingletonTemporaryFileCreator.create("example-file.txt")
      val filePart = FilePart[TemporaryFile](key = "email", "file.txt", contentType = Some(contentType), ref = file)
      MultipartFormData[TemporaryFile](dataParts = params.toMap, files = Seq(filePart), badParts = Seq.empty)
    }

    def anEmptyMultipartFileWithParams(params: (String, Seq[String])*): MultipartFormData[TemporaryFile] = {
      val file     = SingletonTemporaryFileCreator.create("example-file.txt")
      val filePart = FilePart[TemporaryFile](key = "email", "", contentType = Some("text/plain"), ref = file)
      MultipartFormData[TemporaryFile](dataParts = params.toMap, files = Seq(filePart), badParts = Seq.empty)
    }

    "redirect to confirmation" in {
      when(
        casesService.suspendCase(refEq(caseWithStatusOPEN), any[FileUpload], refEq("some-note"), any[Operator])(
          any[HeaderCarrier]
        )
      ).thenReturn(successful(caseWithStatusSUSPENDED))

      val result: Result =
        await(
          controller(caseWithStatusOPEN).postSuspendCase("reference")(
            newFakePOSTRequestWithCSRF(app).withBody(aMultipartFileWithParams("text/plain", "note" -> Seq("some-note")))
          )
        )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/suspend/confirmation")
    }

    "return to form on missing file" in {
      val result: Result =
        await(
          controller(caseWithStatusOPEN).postSuspendCase("reference")(
            newFakePOSTRequestWithCSRF(app).withBody(anEmptyMultipartFileWithParams())
          )
        )

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Change case status to: Suspended")
    }

    "return to form on missing form field" in {
      val result: Result =
        await(
          controller(caseWithStatusOPEN).postSuspendCase("reference")(
            newFakePOSTRequestWithCSRF(app).withBody(aMultipartFileWithParams("text/plain"))
          )
        )

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Change case status to: Suspended")
    }

    "return to form on invalid file type" in {
      val result: Result =
        await(
          controller(caseWithStatusOPEN).postSuspendCase("reference")(
            newFakePOSTRequestWithCSRF(app).withBody(aMultipartFileWithParams("audio/mpeg", "note" -> Seq("some-note")))
          )
        )

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Change case status to: Suspended")
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(
        controller(caseWithStatusOPEN, Set.empty)
          .postSuspendCase("reference")(
            newFakePOSTRequestWithCSRF(app).withBody(aMultipartFileWithParams("text/plain"))
          )
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "View Confirm page for a suspended case" should {

    "return OK and HTML content type" in {
      val result: Result = await(
        controller(caseWithStatusSUSPENDED).confirmSuspendCase("reference")(
          newFakePOSTRequestWithCSRF(app)
            .withFormUrlEncodedBody("state" -> "true")
        )
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("This case has been suspended")
    }

    "redirect to a default page on validaiton error" in {
      val result: Result =
        await(controller(caseWithStatusOPEN).confirmSuspendCase("reference")(newFakePOSTRequestWithCSRF(app)))

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some("/manage-tariff-classifications/cases/reference")
    }
  }

}
