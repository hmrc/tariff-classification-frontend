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
import uk.gov.hmrc.tariffclassificationfrontend.models.Operator
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest

abstract class ViewSpec extends UnitSpec with WithFakeApplication {

  private def injector = fakeApplication.injector

  implicit val appConfig: AppConfig = injector.instanceOf[AppConfig]

  private val tokenProvider: TokenProvider = fakeApplication.injector.instanceOf[TokenProvider]
  private val csrfTags = Map(Token.NameRequestTag -> "csrfToken", Token.RequestTag -> tokenProvider.generateToken)

  private val request = FakeRequest("GET", "/", FakeHeaders(), AnyContentAsEmpty, tags = csrfTags)
  protected val authenticatedOperator = Operator("operator-id")
  implicit val authenticatedFakeRequest: AuthenticatedRequest[AnyContentAsEmpty.type] = AuthenticatedRequest(authenticatedOperator, request)

  implicit val messages: Messages = injector.instanceOf[MessagesApi].preferred(authenticatedFakeRequest)

  protected def view(html: Html): Document = {
    Jsoup.parse(html.toString())
  }
}
