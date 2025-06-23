/*
 * Copyright 2025 HM Revenue & Customs
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

package services

import models._
import models.request.AuthenticatedCaseRequest
import play.api.mvc.ControllerHelpers._
import play.api.mvc.Results.Redirect
import play.api.mvc._

import javax.inject.Inject

class RedirectService @Inject() {

  def redirectApplication(reference: String, fileId: Option[String] = None)(implicit
    request: AuthenticatedCaseRequest[AnyContent]
  ): Result =
    Option(request.`case`.application.`type`) match {
      case Some(ApplicationType.ATAR) =>
        Redirect(controllers.v2.routes.AtarController.displayAtar(reference, fileId))
          .flashing(request2flash)
      case Some(ApplicationType.LIABILITY) =>
        Redirect(controllers.v2.routes.LiabilityController.displayLiability(reference, fileId))
          .flashing(request2flash)
      case Some(ApplicationType.CORRESPONDENCE) =>
        Redirect(controllers.v2.routes.CorrespondenceController.displayCorrespondence(reference, fileId))
          .flashing(request2flash)
      case Some(ApplicationType.MISCELLANEOUS) =>
        Redirect(controllers.v2.routes.MiscellaneousController.displayMiscellaneous(reference, fileId))
          .flashing(request2flash)
      case None => BadRequest
    }
}
