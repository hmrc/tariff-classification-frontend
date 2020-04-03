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

package controllers.v2


import config.AppConfig
import controllers.RequestActions
import javax.inject.{Inject, Singleton}
import models.forms.UploadAttachmentForm
import models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import models.{Case, Permission}
import models.viewmodels.{AttachmentsTabViewModel, C592ViewModel, LiabilityViewModel}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import service.{CasesService, FileStoreService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class LiabilityController @Inject()(
                                     verify: RequestActions,
                                     casesService: CasesService,
                                     fileService: FileStoreService,
                                     mcc: MessagesControllerComponents,
                                     val liability_view: views.html.v2.liability_view,
                                     implicit val appConfig: AppConfig
                                   ) extends FrontendController(mcc) with I18nSupport {


  def displayLiability(reference: String, uploadAttachmentForm: Form[String] = UploadAttachmentForm.form): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async {
    implicit request => {
      buildLiabilityView(reference, uploadAttachmentForm)
    }
  }

  def buildLiabilityView(reference: String, uploadAttachmentForm: Form[String] = UploadAttachmentForm.form)(implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    val liabilityCase: Case = request.`case`
    val liabilityViewModel = LiabilityViewModel.fromCase(liabilityCase, request.operator)
    for {
      //TODO you can hide tabs with feature flags, if you assign None
      //tabs
      attachmentsTab <- getAttachmentTab(liabilityCase)
      c592 = Some(C592ViewModel.fromCase(liabilityCase))
    } yield Ok(liability_view(liabilityViewModel, c592, attachmentsTab, uploadAttachmentForm))
  }

  private def getAttachmentTab(liabilityCase: Case)(implicit hc: HeaderCarrier): Future[Option[AttachmentsTabViewModel]] = {
    for {
      attachments <- fileService.getAttachments(liabilityCase)
      letter <- fileService.getLetterOfAuthority(liabilityCase)
    } yield Some(AttachmentsTabViewModel(liabilityCase.reference, attachments, letter))
  }
}
