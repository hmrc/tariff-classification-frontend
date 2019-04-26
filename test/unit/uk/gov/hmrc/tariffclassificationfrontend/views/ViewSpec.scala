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

package uk.gov.hmrc.tariffclassificationfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeHeaders, FakeRequest}
import play.filters.csrf.CSRF.{Token, TokenProvider}
import play.twirl.api.Html
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.models.{Operator, Role}

abstract class ViewSpec extends UnitSpec with WithFakeApplication {

  private def injector = fakeApplication.injector

  implicit val messages: Messages = injector.instanceOf[MessagesApi].preferred(authenticatedFakeRequest)
  implicit val appConfig: AppConfig = injector.instanceOf[AppConfig]

  protected val authenticatedOperator = Operator("operator-id")
  protected val authenticatedManager = Operator("operator-id", role = Role.CLASSIFICATION_MANAGER)

  private val tokenProvider: TokenProvider = fakeApplication.injector.instanceOf[TokenProvider]
  private val csrfTags = Map(Token.NameRequestTag -> "csrfToken", Token.RequestTag -> tokenProvider.generateToken)

  val request = FakeRequest("GET", "/", FakeHeaders(), AnyContentAsEmpty, tags = csrfTags)
  val operatorRequest = new AuthenticatedRequest(authenticatedOperator, request)
  val authenticatedManagerFakeRequest: AuthenticatedRequest[AnyContentAsEmpty.type] = new AuthenticatedRequest(authenticatedManager, request)

  def requestWithPermissions(permissions: Permission*): AuthenticatedRequest[AnyContentAsEmpty.type] = {
    val operator = authenticatedOperator.copy(permissions = permissions.toSet)
    new AuthenticatedRequest(operator, request)
  }

  implicit val authenticatedFakeRequest: AuthenticatedRequest[AnyContentAsEmpty.type] = new AuthenticatedRequest(authenticatedOperator, request)

  protected def view(html: Html): Document = {
    Jsoup.parse(html.toString())
  }
}
