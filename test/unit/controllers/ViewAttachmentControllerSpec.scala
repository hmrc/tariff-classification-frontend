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

import akka.stream.Materializer
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.given
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.mvc.{BodyParsers, MessagesControllerComponents, Result}
import play.api.test.Helpers.{redirectLocation, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import config.AppConfig
import models.response.{FileMetadata, ScanStatus}
import models.{Operator, Permission}
import service.FileStoreService

import scala.concurrent.Future

class ViewAttachmentControllerSpec extends WordSpec with Matchers with UnitSpec
  with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach with ControllerCommons {

  private val messagesControllerComponents = inject[MessagesControllerComponents]
  private val appConfig = inject[AppConfig]
  private val fileService = mock[FileStoreService]
  private val operator = mock[Operator]

  private implicit val mat: Materializer = fakeApplication.materializer
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def afterEach(): Unit = {
    super.afterEach()
    reset(fileService)
  }

  private def controller() = new ViewAttachmentController(
    new SuccessfulRequestActions(inject[BodyParsers.Default], operator), fileService, messagesControllerComponents, appConfig)

  private def controller(permission: Set[Permission]) = new ViewAttachmentController(
    new RequestActionsWithPermissions(inject[BodyParsers.Default], permission, addViewCasePermission = false), fileService, messagesControllerComponents, appConfig)

  private def givenFileMetadata(fileMetadata: Option[FileMetadata]) =
    given(fileService.getFileMetadata(refEq("id"))(any[HeaderCarrier])) willReturn Future.successful(fileMetadata)

  private val fileReady = FileMetadata("id", "file", "type", Some("url"), Some(ScanStatus.READY))
  private val fileFailed = FileMetadata("id", "file", "type", None, Some(ScanStatus.FAILED))
  private val fileProcessing = FileMetadata("id", "file", "type", None, None)


  "View Attachment 'GET" should {

    "return 303 and redirect for safe file found" in {
      givenFileMetadata(Some(fileReady))

      val result = await(controller().get("id")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("url")
    }

    "return 200 for file processing" in {
      givenFileMetadata(Some(fileProcessing))

      val result = await(controller().get("id")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("attachment-processing")
    }

    "return 200 for un-safe file found" in {
      givenFileMetadata(Some(fileFailed))

      val result = await(controller().get("id")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("attachment-scan_failed")
    }

    "return OK when user has right permissions" in {
      givenFileMetadata(Some(fileProcessing))

      val result: Result = await(controller(Set(Permission.VIEW_CASES))
        .get("id")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have right permissions" in {
      val result: Result = await(controller(Set.empty)
        .get("id")(newFakeGETRequestWithCSRF(fakeApplication)))

      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }
}
