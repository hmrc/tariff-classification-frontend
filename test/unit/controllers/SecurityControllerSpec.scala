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

package controllers

import play.api.http.Status
import play.api.test.Helpers._
import views.html.not_authorized

class SecurityControllerSpec extends ControllerBaseSpec {

  private val notAuthorised = app.injector.instanceOf[not_authorized]

  private val controller = new SecurityController(
    mcc,
    notAuthorised,
    realAppConfig
  )

  "Unauthorized" should {

    "return 200 OK and HTML content type" in {
      val result = await(controller.unauthorized()(fakeRequest))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

  }

  "keepAlive" should {

    "return 204- No content" in {

      val result = await(controller.keepAlive()(fakeRequest))

      status(result)      shouldBe Status.NO_CONTENT
      contentType(result) shouldBe None
    }

  }

}
