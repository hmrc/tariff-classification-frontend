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
import play.api.mvc._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.DecisionForm
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Decision}
import uk.gov.hmrc.tariffclassificationfrontend.forms.LiabilityFormData
import uk.gov.hmrc.tariffclassificationfrontend.models.TabIndexes.tabIndexFor
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedCaseRequest
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage.{CaseDetailPage, LIABILITY}
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.liabilities.liability_details_edit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class LiabilityController @Inject()(verify: RequestActions,
                                    decisionForm: DecisionForm,
                                    val messagesApi: MessagesApi,
                                    casesService: CasesService,
                                    implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  private lazy val menuTitle = LIABILITY

  private lazy val form = LiabilityFormData.form

  def liabilityDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    getCaseAndRenderView(menuTitle,
      c => {
        val form = decisionForm.liabilityCompleteForm(c.decision.getOrElse(Decision()))
        successful(views.html.partials.liability_details(c, tabIndexFor(LIABILITY), form))
      }
    )
  }

  def editLiabilityDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    request.`case` match {
      case c: Case =>
        successful(
          Ok(
            liability_details_edit(c, toLiabilityForm(c.application.asLiabilityOrder), Some(tabIndexFor(LIABILITY)))
          )
        )
    }
  }

  private def toLiabilityForm(l: LiabilityOrder): Form[LiabilityFormData] = {
    LiabilityFormData.form.fill(
      LiabilityFormData(
        entryDate = l.entryDate,
        traderName = l.traderName,
        goodName = l.goodName.getOrElse(""),
        entryNumber = l.entryNumber.getOrElse(""),
        traderCommodityCode = l.traderCommodityCode.getOrElse(""),
        officerCommodityCode = l.officerCommodityCode.getOrElse(""),
        contactName = l.contact.name,
        contactEmail = Some(l.contact.email),
        contactPhone = l.contact.phone.getOrElse("")
      )
    )
  }

  def mergeLiabilityIntoCase(c: Case, validForm: LiabilityFormData): Case = {
    val updatedContact = c.application.contact.copy(
      name = validForm.contactName,
      email = validForm.contactEmail.getOrElse(""),
      phone = Some(validForm.contactPhone)
    )

    val app: Application = c.application.`type` match {
      case ApplicationType.LIABILITY_ORDER => {
        c.application.asLiabilityOrder.copy(
          traderName = validForm.traderName,
          goodName = Some(validForm.goodName),
          entryNumber = Some(validForm.entryNumber),
          entryDate = validForm.entryDate,
          traderCommodityCode = Some(validForm.traderCommodityCode),
          officerCommodityCode = Some(validForm.officerCommodityCode),
          contact = updatedContact
        )
      }
      case _ => c.application
    }

    c.copy(application = app)
  }

  def postLiabilityDetails(reference: String): Action[AnyContent] = (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
    LiabilityFormData.form.bindFromRequest.fold(
      errorForm =>
        successful(
          Ok(
            liability_details_edit(request.`case`, errorForm)
          )
        ),
      validForm =>
        getCaseAndRedirect(menuTitle, c => for {
          update <- casesService.updateCase(mergeLiabilityIntoCase(c, validForm))
        } yield routes.LiabilityController.liabilityDetails(update.reference)
        )
    )
  }

  private def getCaseAndRenderView(page: CaseDetailPage, toHtml: Case => Future[HtmlFormat.Appendable])
                                  (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    toHtml(request.`case`).map(html => Ok(views.html.case_details(request.`case`, page, html, activeTab = Some("tab-item-Liability"))))
  }


  private def getCaseAndRedirect(page: CaseDetailPage, toResult: Case => Future[Call])
                                (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    if (request.`case`.status == CaseStatus.OPEN) {
      toResult(request.`case`).map(Redirect)
    } else {
      successful(Redirect(routes.LiabilityController.liabilityDetails(request.`case`.reference)))
    }
  }

}

