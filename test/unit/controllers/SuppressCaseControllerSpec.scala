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

class SuppressCaseControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val casesService = mock[CasesService]
  private val operator     = mock[Operator]

  private val caseWithStatusNEW  = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.NEW)
  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)
  private val caseWithStatusSUPRRESSED =
    Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.SUPPRESSED)

  private def controller(requestCase: Case) = new SuppressCaseController(
    new SuccessfulRequestActions(playBodyParsers, operator, c = requestCase),
    casesService,
    mcc,
    realAppConfig
  )

  private def controller(requestCase: Case, permission: Set[Permission]) = new SuppressCaseController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
    casesService,
    mcc,
    realAppConfig
  )

  override def afterEach(): Unit = {
    super.afterEach()
    reset(casesService)
  }

  "Suppress Case" should {

    "return OK and HTML content type" in {
      val result: Result =
        await(controller(caseWithStatusNEW).getSuppressCase("reference")(newFakeGETRequestWithCSRF(app)))

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("Change case status to: Suppressed")
    }

    "return OK when user has right permissions" in {
      val result: Result = await(
        controller(caseWithStatusNEW, Set(Permission.SUPPRESS_CASE))
          .getSuppressCase("reference")(newFakeGETRequestWithCSRF(app))
      )

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(
        controller(caseWithStatusNEW, Set.empty).getSuppressCase("reference")(newFakeGETRequestWithCSRF(app))
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

  "Confirm Suppress a Case" should {

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
        casesService.suppressCase(refEq(caseWithStatusNEW), any[FileUpload], refEq("some-note"), any[Operator])(
          any[HeaderCarrier]
        )
      ).thenReturn(successful(caseWithStatusSUPRRESSED))

      val result: Result =
        await(
          controller(caseWithStatusNEW).postSuppressCase("reference")(
            newFakePOSTRequestWithCSRF(app).withBody(aMultipartFileWithParams("text/plain", "note" -> Seq("some-note")))
          )
        )

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/manage-tariff-classifications/cases/reference/suppress/confirmation")
    }

    "return to form on missing file" in {
      val result: Result =
        await(
          controller(caseWithStatusNEW).postSuppressCase("reference")(
            newFakePOSTRequestWithCSRF(app).withBody(anEmptyMultipartFileWithParams())
          )
        )

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Change case status to: Suppressed")
    }

    "return to form on missing form field" in {
      val result: Result =
        await(
          controller(caseWithStatusNEW).postSuppressCase("reference")(
            newFakePOSTRequestWithCSRF(app).withBody(aMultipartFileWithParams("text/plain"))
          )
        )

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Change case status to: Suppressed")
    }

    "return to form on invalid file type" in {
      val result: Result =
        await(
          controller(caseWithStatusNEW).postSuppressCase("reference")(
            newFakePOSTRequestWithCSRF(app).withBody(aMultipartFileWithParams("audio/mpeg", "note" -> Seq("some-note")))
          )
        )

      status(result) shouldBe Status.OK
      bodyOf(result) should include("Change case status to: Suppressed")
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(
        controller(caseWithStatusNEW, Set.empty)
          .postSuppressCase("reference")(
            newFakePOSTRequestWithCSRF(app).withBody(aMultipartFileWithParams("text/plain"))
          )
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

  }

  "View Confirm page for a suppressed case" should {

    "return OK and HTML content type" in {
      val result: Result = await(
        controller(caseWithStatusSUPRRESSED).confirmSuppressCase("reference")(
          newFakePOSTRequestWithCSRF(app)
            .withFormUrlEncodedBody("state" -> "true")
        )
      )

      status(result)        shouldBe Status.OK
      contentTypeOf(result) shouldBe Some(MimeTypes.HTML)
      charsetOf(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("This case has been suppressed")
    }

    "redirect to a default page on validation error" in {
      val result: Result =
        await(controller(caseWithStatusOPEN).confirmSuppressCase("reference")(newFakePOSTRequestWithCSRF(app)))

      status(result)        shouldBe Status.SEE_OTHER
      contentTypeOf(result) shouldBe None
      charsetOf(result)     shouldBe None
      locationOf(result)    shouldBe Some("/manage-tariff-classifications/cases/reference")
    }
  }

}
