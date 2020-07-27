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

import controllers.actions._
import play.api.test.Helpers._
import models.Role.Role
import models._
import play.api.http.Status
import play.api.test.Helpers._
import views.html.accessibilityView

class AccessibilityControllerSpec extends ControllerBaseSpec {

  var accessibility_view: views.html.accessibilityView

  def viewAsString(): String = accessibility_view(frontendAppConfig)(fakeRequest, messages).toString

  "AccessibilityView Controller" must {

    "return OK and the correct view for a GET" in {
      val result = new AccessibilityController(frontendAppConfig, cc).onPageLoad()(fakeRequest)
      status(result) shouldBe OK
      contentAsString(result) shouldBe viewAsString()
    }
  }
}