/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Request, Result}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.{DecisionForm, DecisionFormData, DecisionFormMapper}
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedCaseRequest
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus}
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, FileStoreService}
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage.CaseDetailPage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class RulingController @Inject()(verify: RequestActions,
                                 casesService: CasesService,
                                 fileStoreService: FileStoreService,
                                 mapper: DecisionFormMapper,
                                 decisionForm: DecisionForm,
                                 val messagesApi: MessagesApi,
                                 implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  private lazy val menuTitle = CaseDetailPage.RULING

  def editRulingDetails(reference: String): Action[AnyContent] = (verify.authenticate andThen verify.caseExists(reference) andThen verify.mustHaveWritePermission).async { implicit request =>
    getCaseAndRenderView(menuTitle, c => {
      val formData = mapper.caseToDecisionFormData(c)
      val df = decisionForm.form.fill(formData)
      editRuling(df, c)
    })
  }

  private def editRuling(f: Form[DecisionFormData], c: Case)
                        (implicit request: Request[_]): Future[HtmlFormat.Appendable] = {
    fileStoreService.getAttachments(c).map(views.html.partials.ruling.ruling_details_edit(c, _, f))
  }

  private def getCaseAndRenderView(page: CaseDetailPage, toHtml: Case => Future[HtmlFormat.Appendable])
                                  (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    if (request.`case`.status == CaseStatus.OPEN){
      toHtml(request.`case`).map(html => Ok(views.html.case_details(request.`case`, page, html)))
    }else{
      successful(Redirect(routes.CaseController.rulingDetails(request.`case`.reference)))
    }
  }

  def updateRulingDetails(reference: String): Action[AnyContent] = (verify.authenticate andThen verify.caseExists(reference) andThen verify.mustHaveWritePermission).async { implicit request =>
    decisionForm.form.bindFromRequest.fold(
      errorForm =>
        getCaseAndRenderView(
          menuTitle,
          c => editRuling(errorForm, c)
        ),

      validForm =>
        getCaseAndRenderView(menuTitle, c => {
          casesService
            .updateCase(mapper.mergeFormIntoCase(c, validForm))
            .flatMap { updated =>
              val form = decisionForm.bindFrom(updated.decision)
              fileStoreService
                .getAttachments(updated)
                .map(views.html.partials.ruling.ruling_details(updated, form, _))
            }
        })
    )
  }

}
