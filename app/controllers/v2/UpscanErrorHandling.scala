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

package controllers.v2

import models.forms.UploadAttachmentForm
import models.request.AuthenticatedCaseRequest
import models.response.UploadError
import play.api.data.Form
import play.api.mvc.Result
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait UpscanErrorHandling { self: FrontendBaseController =>
  private val UpscanErrorCodeKey        = "errorCode"
  private val UploadAttachmentFormField = "file"

  def handleUploadErrorAndRender(
    renderView: Form[String] => Future[Html]
  )(implicit request: AuthenticatedCaseRequest[?], ec: ExecutionContext): Future[Result] =
    request
      .getQueryString(UpscanErrorCodeKey)
      .map { errorCode =>
        // Received an error from Upscan
        val uploadError = UploadError.fromErrorCode(errorCode)
        val uploadForm  = UploadAttachmentForm.form.withError(UploadAttachmentFormField, uploadError.errorMessageKey)
        renderView(uploadForm).map(BadRequest(_))
      }
      .getOrElse {
        // Normal page render
        renderView(UploadAttachmentForm.form).map(Ok(_))
      }
      .recoverWith { case NonFatal(_) =>
        Future.successful(BadGateway)
      }

}
