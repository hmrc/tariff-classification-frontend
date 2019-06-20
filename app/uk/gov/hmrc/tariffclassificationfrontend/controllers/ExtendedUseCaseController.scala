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
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.BooleanForm
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.CANCELLED
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Operator, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.Future

@Singleton
class ExtendedUseCaseController @Inject()(override val verify: RequestActions,
                                          override val caseService: CasesService,
                                          override val messagesApi: MessagesApi,
                                          override implicit val config: AppConfig) extends StatusChangeAction[Boolean] {

  override protected val requiredPermission: Permission = Permission.EXTENDED_USE

  override protected val form: Form[Boolean] = BooleanForm.form

  override protected def status(c: Case): Boolean = c.decision.flatMap(_.cancellation).exists(_.applicationForExtendedUse)

  override protected def chooseStatusView(c: Case, preFilledForm: Form[Boolean], options: Option[String] = None)
                                         (implicit request: Request[_]): Html = {
    views.html.change_extended_use_status(c, preFilledForm)
  }

  override protected def update(c: Case, status: Boolean, operator: Operator)
                               (implicit hc: HeaderCarrier): Future[Case] = {
    caseService.updateExtendedUseStatus(c, status, operator)
  }

  override protected def onSuccessRedirect(reference: String): Call = routes.AppealCaseController.appealDetails(reference)

}
