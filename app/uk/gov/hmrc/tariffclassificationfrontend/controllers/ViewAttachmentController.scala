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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models.response.FileMetadata
import uk.gov.hmrc.tariffclassificationfrontend.service.FileStoreService
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ViewAttachmentController @Inject()(verify: RequestActions,
                                         fileService: FileStoreService,
                                         val messagesApi: MessagesApi,
                                         implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  def get(id: String): Action[AnyContent] = (verify.authenticated andThen verify.mustHave(Permission.VIEW_CASES)).async
  { implicit request: Request[AnyContent] =>
    fileService.getFileMetadata(id) map {
      case Some(fileSubmitted: FileMetadata) if fileSubmitted.url.isDefined =>
        Redirect(Call(method = "GET", url = fileSubmitted.url.get))
      case fileSubmitted: Option[FileMetadata] =>
        Ok(views.html.view_attachment_unavailable(fileSubmitted))
    }
  }
}
