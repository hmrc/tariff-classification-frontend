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

import config.AppConfig
import javax.inject.Inject
import models.forms.v2.{MiscDetailsForm, MiscellaneousForm}
import models.request.AuthenticatedRequest
import models.viewmodels.CaseViewModel
import models.{Case, MiscApplication, Permission}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{MessagesControllerComponents, _}
import service.{CasesService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class CreateMiscellaneousController @Inject() (
  verify: RequestActions,
  casesService: CasesService,
  queueService: QueuesService,
  mcc: MessagesControllerComponents,
  val releaseCaseView: views.html.release_case,
  val confirmation_case_creation: views.html.v2.confirmation_case_creation,
  val misc_details_edit: views.html.v2.misc_details_edit,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  private val form: Form[MiscApplication] = MiscellaneousForm.newMiscForm

  def get(): Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.CREATE_CASES)).async {
    implicit request => Future.successful(Ok(views.html.v2.create_misc(form)))
  }

  def post(): Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.CREATE_CASES)).async {
    implicit request =>
      form.bindFromRequest.fold(
        formWithErrors => Future.successful(Ok(views.html.v2.create_misc(formWithErrors))),
        miscApp =>
          casesService.createCase(miscApp, request.operator).map { caseCreated: Case =>
            Redirect(routes.CreateMiscellaneousController.displayQuestion(caseCreated.reference))
          }
      )

  }

  def displayQuestion(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen verify.mustHave(Permission.RELEASE_CASE))
      .async(implicit request => getCaseAndRenderChoiceView(reference))

  private def getCaseAndRenderChoiceView(
    reference: String
  )(implicit hc: HeaderCarrier, request: AuthenticatedRequest[_]): Future[Result] =
    casesService.getOne(reference).flatMap {
      case Some(_: Case) => successful(Redirect(routes.ReleaseCaseController.releaseCase(reference)))
      case _             => successful(Ok(views.html.case_not_found(reference)))
    }

  def displayConfirmation(reference: String) =
    (verify.authenticated andThen verify.mustHave(Permission.CREATE_CASES)).async {
      implicit request: AuthenticatedRequest[AnyContent] =>
        casesService.getOne(reference).flatMap {
          case Some(c: Case) => {
            c.queueId
              .map(id =>
                queueService.getOneById(id) flatMap {
                  case Some(queue) => Future.successful(Ok(confirmation_case_creation(c, queue.name)))
                  case None        => Future.successful(Ok(views.html.resource_not_found(s"Case Queue")))
                }
              )
              .getOrElse(Future.successful(Ok(confirmation_case_creation(c, ""))))

          }
          case _ => successful(Ok(views.html.case_not_found(reference)))
        }
    }

  def editMiscDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(Permission.EDIT_MISCELLANEOUS)).async { implicit request =>
      successful(
        Ok(
          misc_details_edit(
            request.`case`,
            MiscDetailsForm.miscDetailsForm(request.`case`),
            CaseViewModel.fromCase(request.`case`, request.operator)
          )
        )
      )
    }

  def postMiscDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(Permission.EDIT_MISCELLANEOUS)).async { implicit request =>
      MiscDetailsForm
        .miscDetailsForm(request.`case`)
        .discardingErrors
        .bindFromRequest
        .fold(
          errorForm =>
            successful(
              Ok(misc_details_edit(request.`case`, errorForm, CaseViewModel.fromCase(request.`case`, request.operator)))
            ),
          updatedCase =>
            casesService
              .updateCase(request.`case`, updatedCase, request.operator)
              .map(_ => Redirect(v2.routes.MiscellaneousController.displayMiscellaneous(reference)))
        )
    }
}
