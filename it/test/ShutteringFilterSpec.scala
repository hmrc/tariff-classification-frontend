/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.routes.IndexController
import org.scalatest.OptionValues
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test.{FakeHeaders, FakeRequest}
import views.html.shutterPage

class ShutteringFilterSpec extends IntegrationTest with OptionValues {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      "shutter.enabled"       -> true,
      "shutter.urls.excluded" -> "/ping/ping"
    )
    .build()

  val request: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", "/", FakeHeaders(Seq("csrfToken" -> "csrfToken")), AnyContentAsEmpty).withCSRFToken
      .asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  "a shuttering filter" should {

    "shutter" when {

      "the `shuttered` config property is true" in {
        val view   = shutterPage
        val result = route(app, FakeRequest(GET, IndexController.get().url)).get

        status(result)          shouldBe SERVICE_UNAVAILABLE
        contentAsString(result) shouldBe view()(request).toString
      }
    }

    "leave excluded URLs un-shuttered" in {
      val result = route(app, FakeRequest(GET, "/ping/ping")).get

      status(result) shouldBe OK
    }
  }
}
