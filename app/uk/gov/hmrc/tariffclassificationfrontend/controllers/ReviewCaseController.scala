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
import uk.gov.hmrc.tariffclassificationfrontend.forms.ReviewForm
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.{CANCELLED, COMPLETED}
import uk.gov.hmrc.tariffclassificationfrontend.models.ReviewStatus.ReviewStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Operator}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.Future

@Singleton
class ReviewCaseController @Inject()(override val verify: RequestActions,
                                     override val caseService: CasesService,
                                     override val messagesApi: MessagesApi,
                                     override implicit val config: AppConfig) extends StatusChangeAction[Option[ReviewStatus]] {

  override protected def redirect: String => Call = routes.CaseController.trader

  override protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean = {
    (c.status == COMPLETED || c.status == CANCELLED) && c.decision.isDefined
  }

  override protected val form: Form[Option[ReviewStatus]] = ReviewForm.form

  override protected def status(c: Case): Option[ReviewStatus] = c.decision.flatMap(_.review).map(_.status)

  override protected def chooseStatusView(c: Case, preFilledForm: Form[Option[ReviewStatus]])
                                         (implicit request: Request[_]): Html = {
    views.html.change_review_status(c, preFilledForm)
  }

  override protected def update(c: Case, status: Option[ReviewStatus], operator: Operator)
                               (implicit hc: HeaderCarrier): Future[Case] = {
    caseService.updateReviewStatus(c, status, operator)
  }

  override protected def onSuccessRedirect(reference: String): Call = routes.AppealCaseController.appealDetails(reference)

}
