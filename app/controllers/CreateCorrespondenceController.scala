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
import models.forms.CorrespondenceForm
import javax.inject.{Inject, Singleton}
import models.{Case, CorrespondenceApplication, Permission, Queues}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.api.data.Forms._
import models.forms.mappings.FormMappings.fieldNonEmpty
import models.forms.v2.{CorrespondenceContactForm, CorrespondenceDetailsForm}
import service.{CasesService, QueuesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.http.HeaderCarrier
import models.request.AuthenticatedRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

@Singleton
class CreateCorrespondenceController @Inject()(
  verify: RequestActions,
  casesService: CasesService,
  queueService: QueuesService,
  mcc: MessagesControllerComponents,
  val releaseCaseView: views.html.release_case,
  val releaseCaseQuestionView: views.html.v2.release_option_choice,
  val confirmation_case_creation: views.html.v2.confirmation_case_creation,
  val correspondence_details_edit: views.html.v2.correspondence_details_edit,
  val correspondence_contact_edit: views.html.v2.correspondence_contact_edit,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  private val form: Form[CorrespondenceApplication] = CorrespondenceForm.newCorrespondenceForm
  private val formReleaseChoice: Form[String] = Form(
    mapping(
      "choice" -> fieldNonEmpty("error.empty.queue")
    )(identity)(Some(_))
  )

  def get(): Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.CREATE_CASES)).async {
    implicit request =>
      Future.successful(Ok(views.html.v2.create_correspondence(form)))
  }

  def post(): Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.CREATE_CASES)).async {
    implicit request =>
      form.bindFromRequest.fold(
        formWithErrors => Future.successful(Ok(views.html.v2.create_correspondence(formWithErrors))),
        correspondenceApp =>
          casesService.createCase(correspondenceApp, request.operator).map { caseCreated: Case =>
            Redirect(routes.CreateCorrespondenceController.displayQuestion(caseCreated.reference))
        }
      )

  }

  def displayQuestion(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference)).async { implicit request =>
      getCaseAndRenderChoiceView(reference)
    }

  private def getCaseAndRenderChoiceView(reference: String, form: Form[String] = formReleaseChoice)(
    implicit hc: HeaderCarrier,
    request: AuthenticatedRequest[_]): Future[Result] =
    casesService.getOne(reference).flatMap {
      case Some(c: Case) => successful(Ok(releaseCaseQuestionView(c, form)))
      case _             => successful(Ok(views.html.case_not_found(reference)))
    }

  def postChoice(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(Permission.RELEASE_CASE)).async { implicit request =>
      formReleaseChoice
        .bindFromRequest()
        .fold(
          errors => getCaseAndRenderChoiceView(reference, errors),
          (choice: String) => {
            choice match {
              case "Yes" => successful(Redirect(routes.ReleaseCaseController.releaseCase(reference)))
              case _     => successful(Redirect(routes.CreateCorrespondenceController.displayConfirmation(reference)))
            }
          }
        )
    }

  def displayConfirmation(reference: String) =
    (verify.authenticated andThen verify.mustHave(Permission.CREATE_CASES)).async { implicit request =>
      {
        casesService.getOne(reference).flatMap {
          case Some(c: Case) => {
            c.queueId
              .map(id =>
                queueService.getOneById(id) flatMap {
                  case Some(queue) => Future.successful(Ok(confirmation_case_creation(c, queue.name)))
                  case None        => Future.successful(Ok(views.html.resource_not_found(s"Case Queue")))
              })
              .getOrElse(Future.successful(Ok(confirmation_case_creation(c, ""))))

          }
          case _ => successful(Ok(views.html.case_not_found(reference)))
        }
      }
    }

  def editCorrespondence(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(Permission.EDIT_CORRESPONDENCE)).async { implicit request =>
      successful(
        Ok(
          correspondence_details_edit(
            request.`case`,
            CorrespondenceDetailsForm.correspondenceDetailsForm(request.`case`)))
      )
    }

  def postCorrespondenceDetails(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(Permission.EDIT_CORRESPONDENCE)).async { implicit request =>
      CorrespondenceDetailsForm
        .correspondenceDetailsForm(request.`case`)
        .discardingErrors
        .bindFromRequest
        .fold(
          errorForm => successful(Ok(correspondence_details_edit(request.`case`, errorForm))),
          updatedCase =>
            casesService
              .updateCase(updatedCase)
              .map(_ => Redirect(v2.routes.CorrespondenceController.displayCorrespondence(reference)))
        )
    }

  def editCorrespondenceContact(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(Permission.EDIT_CORRESPONDENCE)).async { implicit request =>
      successful(
        Ok(
          correspondence_contact_edit(
            request.`case`,
            CorrespondenceContactForm.correspondenceContactForm(request.`case`)))
      )
    }

  def postCorrespondenceContact(reference: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen
      verify.mustHave(Permission.EDIT_CORRESPONDENCE)).async { implicit request =>
      CorrespondenceContactForm
        .correspondenceContactForm(request.`case`)
        .discardingErrors
        .bindFromRequest
        .fold(
          errorForm => successful(Ok(correspondence_contact_edit(request.`case`, errorForm))),
          updatedCase =>
            casesService
              .updateCase(updatedCase)
              .map(_ => Redirect(v2.routes.CorrespondenceController.displayCorrespondence(reference)))
        )
    }

}
