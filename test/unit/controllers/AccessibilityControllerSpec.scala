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

package controllers

import models._
import models.request._
import play.api.test.Helpers._
import play.api.mvc.Request

class AccessibilityControllerSpec extends ControllerBaseSpec {
  implicit val appConfig = realAppConfig
  implicit val operator = Operator(id = "0", role = Role.CLASSIFICATION_OFFICER)
  implicit val request = fakeRequest

  implicit def authenticatedRequest[A](
    implicit
    operator: Operator,
    request: Request[A]
  ): AuthenticatedRequest[A] =
    AuthenticatedRequest(operator, request)

  val accessibility_view = new views.html.accessibility_view()

  def viewAsString(): String = accessibility_view().toString

  private def action = new SuccessfulAuthenticatedAction(
    defaultPlayBodyParsers,
    operator
  )

  private def controller =  new AccessibilityController(
    action,
    mcc,
    accessibility_view,
    realAppConfig
  )

  "AccessibilityView Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller.onPageLoad(request)

      status(result) shouldBe OK

      contentAsString(result) shouldBe viewAsString()
    }
  }
}