/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.not_authorized
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding

import scala.concurrent.Future.successful

@Singleton
class SecurityController @Inject() (
  mcc: MessagesControllerComponents,
  val not_authorized: not_authorized,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport
    with WithUnsafeDefaultFormBinding {

  def unauthorized(): Action[AnyContent] = Action.async(implicit request => successful(Ok(not_authorized())))

  def keepAlive(): Action[AnyContent] = Action.async(implicit request => successful(NoContent))

}
