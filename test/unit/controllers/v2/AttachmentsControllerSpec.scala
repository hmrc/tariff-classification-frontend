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

package controllers.v2

import config.AppConfig
import controllers.routes.CaseController
import controllers.{ControllerBaseSpec, RequestActionsWithPermissions, SuccessfulRequestActions}
import models._
import models.request.AuthenticatedRequest
import models.viewmodels.CaseHeaderViewModel
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{reset, times, verify, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.http.Status
import play.api.i18n.Messages
import play.api.mvc._
import play.api.test.Helpers._
import play.twirl.api.Html
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.partials.liabilities.attachments_details
import views.html.v2.remove_attachment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class AttachmentsControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  lazy val casesService: CasesService                         = mock[CasesService]
  lazy val fileService: FileStoreService                      = mock[FileStoreService]
  lazy val operator: Operator                                 = Operator(id = "id")
  lazy val liabilityController: LiabilityController           = mock[LiabilityController]
  lazy val atarController: AtarController                     = mock[AtarController]
  lazy val correspondenceController: CorrespondenceController = mock[CorrespondenceController]
  lazy val miscellaneousController: MiscellaneousController   = mock[MiscellaneousController]
  lazy val attachments_details: attachments_details           = mock[attachments_details]
  lazy val remove_attachment: remove_attachment               = mock[remove_attachment]

  def controller: AttachmentsController =
    new AttachmentsController(
      verify            = new SuccessfulRequestActions(playBodyParsers, operator, c = Cases.btiCaseExample),
      casesService      = casesService,
      mcc               = mcc,
      remove_attachment = remove_attachment,
      appConfig         = realAppConfig,
      mat               = mat
    )

  def controller(requestCase: Case, permission: Set[Permission]): AttachmentsController =
    new AttachmentsController(
      verify            = new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService      = casesService,
      mcc               = mcc,
      remove_attachment = remove_attachment,
      appConfig         = realAppConfig,
      mat               = mat
    )

  override protected def beforeEach(): Unit =
    reset(remove_attachment)

  "Remove attachment" should {

    val aCase = Cases.btiCaseExample

    "return OK when user has correct permissions" in {
      givenACaseWithNoAttachmentsAndNoLetterOfAuthority(aCase.reference, aCase)

      val result: Result = await(
        controller(aCase, Set(Permission.REMOVE_ATTACHMENTS))
          .removeAttachment(aCase.reference, "reference", "some-file.jpg")(newFakeGETRequestWithCSRF())
      )

      status(result) shouldBe Status.OK
    }

    "redirect unauthorised when does not have correct permissions" in {
      val result: Result = await(
        controller(aCase, Set.empty)
          .removeAttachment(aCase.reference, "reference", "some-file.jpg")(newFakeGETRequestWithCSRF())
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }
  }

  "Confirm Remove Attachment" should {

    val aCase = Cases.btiCaseExample

    "redirect to attachments tab when user selects `yes`" in {
      when(casesService.removeAttachment(any[Case], any[String])(any[HeaderCarrier])).thenReturn(successful(aCase))

      val result: Result = await(
        controller(aCase, Set(Permission.REMOVE_ATTACHMENTS))
          .confirmRemoveAttachment(aCase.reference, "fileId", "some-file.jpg")(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody("state" -> "true")
          )
      )

      redirectLocation(result) shouldBe Some(CaseController.attachmentsDetails(aCase.reference).path)
    }

    "redirect to attachments tab when user selects `no`" in {
      val result: Result = await(
        controller(aCase, Set(Permission.REMOVE_ATTACHMENTS))
          .confirmRemoveAttachment(aCase.reference, "reference", "some-file.jpg")(
            newFakePOSTRequestWithCSRF()
              .withFormUrlEncodedBody("state" -> "false")
          )
      )

      redirectLocation(result) shouldBe Some(CaseController.attachmentsDetails(aCase.reference).path)
    }

    "redirect back to confirm remove view on form error" in {
      givenACaseWithNoAttachmentsAndNoLetterOfAuthority(aCase.reference, aCase)
      when(casesService.removeAttachment(any[Case], any[String])(any[HeaderCarrier])).thenReturn(successful(aCase))

      val result: Result = await(
        controller(aCase, Set(Permission.REMOVE_ATTACHMENTS))
          .confirmRemoveAttachment(aCase.reference, "fileId", "some-file.jpg")(newFakePOSTRequestWithCSRF())
      )

      status(result) shouldBe Status.OK

      verify(remove_attachment, times(1)).apply(
        any[CaseHeaderViewModel],
        any[Form[Boolean]],
        anyString(),
        anyString()
      )(any[AuthenticatedRequest[_]], any[Messages], any[AppConfig])
    }

  }

  private def givenACaseWithNoAttachmentsAndNoLetterOfAuthority(testReference: String, aCase: Case): OngoingStubbing[Future[Option[StoredAttachment]]] = {
    when(remove_attachment.apply(
      any[CaseHeaderViewModel],
      any[Form[Boolean]],
      anyString(),
      anyString()
    )(any[AuthenticatedRequest[_]], any[Messages], any[AppConfig])).thenReturn(Html("heading"))
    when(casesService.getOne(refEq(testReference))(any[HeaderCarrier])).thenReturn(successful(Some(aCase)))
    when(fileService.getAttachments(refEq(aCase))(any[HeaderCarrier])).thenReturn(successful(Seq.empty))
    when(fileService.getLetterOfAuthority(refEq(aCase))(any[HeaderCarrier])).thenReturn(successful(None))
  }

}
