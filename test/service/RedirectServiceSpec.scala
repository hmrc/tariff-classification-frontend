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

package service

import models.{Case, Operator}
import models.request.AuthenticatedCaseRequest
import play.api.http.HeaderNames.LOCATION
import play.api.http.Status
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.FakeRequest
import utils.Cases
import play.api.test.CSRFTokenHelper.CSRFRequest

class RedirectServiceSpec extends ServiceSpecBase {

  private val service = injector.instanceOf[RedirectService]

  private val request: Request[AnyContent] = FakeRequest().withCSRFToken

  def testRequest(c: Case) = new AuthenticatedCaseRequest(
    operator = Operator("operator-id"),
    request = request,
    requestedCase = c
  )

  "Redirect Service" should {

    "call atar Controller based on case's application type" in {
      val `case` = Cases.btiCaseExample

      val result = await(service.redirectApplication(`case`.reference)(testRequest(`case`)))

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(controllers.v2.routes.AtarController.displayAtar(`case`.reference).url)
    }

    "call liability Controller based on case's application type" in {
      val `case` = Cases.liabilityCaseExample

      val result = await(service.redirectApplication(`case`.reference)(testRequest(`case`)))

      status(result)     shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(controllers.v2.routes.LiabilityController.displayLiability(`case`.reference).url)
    }

    "call correspondence Controller based on case's application type" in {
      val `case` = Cases.correspondenceCaseExample

      val result = await(service.redirectApplication(`case`.reference)(testRequest(`case`)))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(
        controllers.v2.routes.CorrespondenceController.displayCorrespondence(`case`.reference).url
      )
    }

    "call miscellaneous Controller based on case's application type" in {
      val `case` = Cases.miscellaneousCaseExample

      val result = await(service.redirectApplication(`case`.reference)(testRequest(`case`)))

      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(
        controllers.v2.routes.MiscellaneousController.displayMiscellaneous(`case`.reference).url
      )
    }
  }

  private def locationOf(result: Result): Option[String] =
    result.header.headers.get(LOCATION)
}
