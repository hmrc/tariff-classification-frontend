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

package views

import base.SpecBase
import config.AppConfig
import models.request.AuthenticatedRequest
import models.{Operator, Permission, Role}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.{FakeHeaders, FakeRequest}
import play.twirl.api.Html

abstract class ViewSpec extends SpecBase {

  implicit val appConfig: AppConfig = realAppConfig
  protected val errorPrefix         = messages("error.browser.title.prefix")

  protected val authenticatedOperator: Operator         = Operator("operator-id")
  protected val authenticatedOperatorWithName: Operator = Operator("operator-id", Some("operator name officer"))
  protected val authenticatedManager: Operator = Operator(
    id          = "operator-id",
    name        = Some("operator-name"),
    role        = Role.CLASSIFICATION_MANAGER,
    permissions = Set(Permission.VIEW_MY_CASES)
  )

  val request: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/", FakeHeaders(Seq("csrfToken" -> "csrfToken")), AnyContentAsEmpty).withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  val operatorRequest                 = new AuthenticatedRequest(authenticatedOperator, request)
  val operatorRequestWithName         = new AuthenticatedRequest(authenticatedOperatorWithName, request)
  val authenticatedManagerFakeRequest = new AuthenticatedRequest(authenticatedManager, request)

  def requestWithPermissions(permissions: Permission*): AuthenticatedRequest[AnyContentAsEmpty.type] = {
    val operator = authenticatedOperator.copy(permissions = permissions.toSet)
    new AuthenticatedRequest(operator, request)
  }

  implicit val authenticatedFakeRequest: AuthenticatedRequest[AnyContentAsEmpty.type] =
    new AuthenticatedRequest(authenticatedOperator, request)

  protected def view(html: Html): Document =
    Jsoup.parse(html.toString())

}
