/*
 * Copyright 2020 HM Revenue & Customs
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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeHeaders, FakeRequest}
import play.filters.csrf.CSRF.{Token, TokenProvider}
import play.twirl.api.Html
import uk.gov.hmrc.play.test.UnitSpec
import config.AppConfig
import models.request.AuthenticatedRequest
import models.{Operator, Permission, Role}
import play.api.test.CSRFTokenHelper._

abstract class ViewSpec extends UnitSpec with GuiceOneAppPerSuite {

  private def injector = app.injector

  implicit val appConfig: AppConfig = injector.instanceOf[AppConfig]

  protected val authenticatedOperator = Operator("operator-id")
  protected val authenticatedManager = Operator("operator-id", role = Role.CLASSIFICATION_MANAGER)

  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/", FakeHeaders(Seq("csrfToken"->"csrfToken")), AnyContentAsEmpty)
    .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  val operatorRequest = new AuthenticatedRequest(authenticatedOperator, request)
  val authenticatedManagerFakeRequest = new AuthenticatedRequest(authenticatedManager, request)

  def requestWithPermissions(permissions: Permission*): AuthenticatedRequest[AnyContentAsEmpty.type] = {
    val operator = authenticatedOperator.copy(permissions = permissions.toSet)
    new AuthenticatedRequest(operator, request)
  }

  implicit val authenticatedFakeRequest = new AuthenticatedRequest(authenticatedOperator, request)
  implicit val messages: Messages = injector.instanceOf[MessagesApi].preferred(authenticatedFakeRequest)

  protected def view(html: Html): Document = {
    Jsoup.parse(html.toString())
  }

}
