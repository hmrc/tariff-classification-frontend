package uk.gov.hmrc.tariffclassificationfrontend.controllers

import play.api.mvc.{Request, Result}
import uk.gov.hmrc.tariffclassificationfrontend.models.{AuthenticatedRequest, Operator}

import scala.concurrent.Future

class SuccessfulAuthenticatedAction extends AuthenticatedAction(null, null, null) {
  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    block(AuthenticatedRequest(Operator("0", "name"), request))
  }
}
