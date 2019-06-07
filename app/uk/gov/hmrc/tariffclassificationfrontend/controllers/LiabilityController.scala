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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.{DecisionForm, DecisionFormMapper}
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
class LiabilityController @Inject()(verify: RequestActions,
                                    casesService: CasesService,
                                    fileStoreService: FileStoreService,
                                    mapper: DecisionFormMapper,
                                    decisionForm: DecisionForm,
                                    val messagesApi: MessagesApi,
                                    implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  private lazy val menuTitle = CaseDetailPage.LIABILITY
  private lazy val editMenuTitle = CaseDetailPage.EDIT_RULING

  private val startTabIndex = 11000


  def liabilityDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    getCaseAndRenderView(
      menuTitle,
      c => {
        successful(views.html.partials.liability_details(c, 110000))
      }
    )
  }


  private def getCaseAndRenderView(page: CaseDetailPage, toHtml: Case => Future[HtmlFormat.Appendable])
                                  (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    if (request.`case`.status == CaseStatus.OPEN) {
      toHtml(request.`case`).map(html => Ok(views.html.case_details(request.`case`, page, html, activeTab = Some("tab-item-Liability"))))
    } else {
      successful(Redirect(routes.CaseController.rulingDetails(request.`case`.reference)))
    }
  }

  private def getCaseAndRedirect(page: CaseDetailPage, toResult: Case => Future[Call])
                                (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    if (request.`case`.status == CaseStatus.OPEN) {
      toResult(request.`case`).map(Redirect)
    } else {
      successful(Redirect(routes.CaseController.rulingDetails(request.`case`.reference)))
    }
  }

}
