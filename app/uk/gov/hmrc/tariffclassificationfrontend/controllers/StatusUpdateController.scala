package uk.gov.hmrc.tariffclassificationfrontend.controllers

import play.api.data.Form
import play.api.mvc.{Action, AnyContent, Call, Request}
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Operator}

import scala.concurrent.Future
import scala.concurrent.Future.successful

trait StatusUpdateController[T] extends RenderCaseAction {

  protected val authenticatedAction: AuthenticatedAction

  protected val form: Form[T]

  protected def status(c: Case): T

  protected def chooseView(c: Case, preFilledForm: Form[T])(implicit request: Request[_]): Html

  protected def update(c: Case, status: T, operator: Operator)(implicit hc: HeaderCarrier): Future[Case]

  protected def onSuccessRedirect(reference: String): Call

  def chooseStatus(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    getCaseAndRenderView(
      reference,
      c => successful(chooseView(c, form.fill(status(c))))
    )
  }

  def updateStatus(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    form.bindFromRequest().fold(
      errors => {
        getCaseAndRenderView(
          reference,
          c => successful(chooseView(c, errors))
        )
      },
      (status: T) => {
        getCaseAndRespond(
          reference,
          c => {
            if(statusHasChanged(c, status)) {
              update(c, status, request.operator).flatMap { _ =>
                successful(Redirect(onSuccessRedirect(reference)))
              }
            } else {
              successful(Redirect(onSuccessRedirect(reference)))
            }

          }
        )
      }
    )

  }

  private def statusHasChanged(c: Case, updated: T): Boolean = {
    status(c) != updated
  }

}
