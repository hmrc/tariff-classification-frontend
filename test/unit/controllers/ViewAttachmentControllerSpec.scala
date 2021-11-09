/*
 * Copyright 2021 HM Revenue & Customs
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

import akka.stream.scaladsl.Source
import akka.util.ByteString
import models.response.{FileMetadata, ScanStatus}
import models.{Operator, Permission}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.given
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.mvc.Result
import play.api.test.Helpers.{redirectLocation, _}
import service.FileStoreService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.view_attachment_unavailable

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ViewAttachmentControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val fileService = mock[FileStoreService]
  private val operator    = Operator(id = "id")

  private val reference = Cases.btiNewCase.reference

  private val view_attachment_unavailable = app.injector.instanceOf[view_attachment_unavailable]

  override def afterEach(): Unit = {
    super.afterEach()
    reset(fileService)
  }

  private def controller() =
    new ViewAttachmentController(
      new SuccessfulRequestActions(playBodyParsers, operator),
      fileService,
      mcc,
      view_attachment_unavailable,
      realAppConfig
    )

  private def controller(permission: Set[Permission]) =
    new ViewAttachmentController(
      new RequestActionsWithPermissions(playBodyParsers, permission, addViewCasePermission = false),
      fileService,
      mcc,
      view_attachment_unavailable,
      realAppConfig
    )

  private def givenFileMetadata(fileMetadata: Option[FileMetadata]) =
    given(fileService.getFileMetadata(refEq("id"))(any[HeaderCarrier])) willReturn Future.successful(fileMetadata)

  private def givenFileContent(url: String, fileContent: Array[Byte]) =
    given(fileService.downloadFile(refEq(url))(any[HeaderCarrier])) willReturn Future.successful(Some(Source.single(ByteString(fileContent))))

  private val fileReady      = FileMetadata("id", Some("file"), Some("type"), Some("url"), Some(ScanStatus.READY))
  private val fileFailed     = FileMetadata("id", Some("file"), Some("type"), None, Some(ScanStatus.FAILED))
  private val fileProcessing = FileMetadata("id", Some("file"), Some("type"), None, None)

  "View Attachment 'GET" should {

    "return 200 and file content for safe file found" in {
      givenFileMetadata(Some(fileReady))
      givenFileContent(fileReady.url.get, "CONTENT".getBytes())

      val result = await(controller().get(reference, "id")(newFakeGETRequestWithCSRF(app)))

      status(result)     shouldBe Status.OK
      contentAsBytes(result) shouldBe ByteString("CONTENT".getBytes)
    }

    "return 404 for file processing" in {
      givenFileMetadata(Some(fileProcessing))

      val result = await(controller().get(reference, "id")(newFakeGETRequestWithCSRF(app)))

      status(result)      shouldBe Status.NOT_FOUND
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)      should include("attachment-processing")
    }

    "return 404 for un-safe file found" in {
      givenFileMetadata(Some(fileFailed))

      val result = await(controller().get(reference, "id")(newFakeGETRequestWithCSRF(app)))

      status(result)      shouldBe Status.NOT_FOUND
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)      should include("attachment-scan_failed")
    }

    "return 404 when there is no file metadata" in {
      givenFileMetadata(Some(fileFailed))

      val result: Result = await(
        controller(Set(Permission.VIEW_CASES))
          .get(reference, "id")(newFakeGETRequestWithCSRF(app))
      )

      status(result)      shouldBe Status.NOT_FOUND
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)      should include("attachment-scan_failed")
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(
        controller(Set.empty)
          .get(reference, "id")(newFakeGETRequestWithCSRF(app))
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }
}
