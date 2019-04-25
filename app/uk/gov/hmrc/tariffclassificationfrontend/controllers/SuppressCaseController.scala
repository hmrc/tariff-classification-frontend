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
import play.api.i18n.MessagesApi
import play.api.mvc._
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.MandatoryBooleanForm
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.NEW
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class SuppressCaseController @Inject()(verify: RequestActions,
                                       casesService: CasesService,
                                       val messagesApi: MessagesApi,
                                       implicit val appConfig: AppConfig) extends RenderCaseAction {

  private val form: Form[Boolean] = MandatoryBooleanForm.form("suppress_case")

  override protected val config: AppConfig = appConfig
  override protected val caseService: CasesService = casesService

  override protected def redirect: String => Call = routes.CaseController.applicationDetails
  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = c.status == NEW

  private def showCase(reference: String, f: Form[Boolean])
                      (implicit request: AuthenticatedCaseRequest[AnyContent]): Future[Result] = {
    getCaseAndRenderView(reference, c => successful(views.html.suppress_case(c, f)))
  }

  def suppressCase(reference: String): Action[AnyContent] = (verify.authenticate andThen verify.casePermissions(reference) andThen
    verify.mustHave(Permission.SUPPRESS_CASE)).async { implicit request =>
    showCase(reference, form)
  }

  def confirmSuppressCase(reference: String): Action[AnyContent] = (verify.authenticate andThen verify.casePermissions(reference) andThen
    verify.mustHave(Permission.SUPPRESS_CASE)).async { implicit request =>

    form.bindFromRequest().fold(
      errors => showCase(reference, errors),
      {
        case true => getCaseAndRenderView(reference, casesService.suppressCase(_, request.operator).map(views.html.confirm_supressed_case(_)))
        case _ => getCaseAndRenderView(reference, c => successful(views.html.supressed_case_error(c)))
      }
    )

  }

}
