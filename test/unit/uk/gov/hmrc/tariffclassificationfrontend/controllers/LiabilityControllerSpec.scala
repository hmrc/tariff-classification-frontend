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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import org.scalatest.Matchers
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Permission.Permission
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.tariffclassificationfrontend.utils.Cases

class LiabilityControllerSpec extends UnitSpec with Matchers with WithFakeApplication with MockitoSugar with ControllerCommons {

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
//  private val casesService = mock[CasesService]
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def controller(permissions: Set[Permission]) = new LiabilityController(
    new RequestActionsWithPermissions(permissions = permissions, addViewCasePermission = false,
      c = Cases.liabilityCaseExample), messageApi, appConfig
  )

  "GET" should {
    "redirect to unauthorised if not permitted" in {
      val request = newFakeGETRequestWithCSRF(fakeApplication)
      val result = await(controller(Set.empty).liabilityDetails("ref")(request))
      status(result) shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.SecurityController.unauthorized().url)
    }

    "return 200 OK and HTML content type" in {
      val request = newFakeGETRequestWithCSRF(fakeApplication)
      val result = await(controller(Set(Permission.VIEW_CASES)).liabilityDetails("ref")(request))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")

      contentAsString(result) should include ("liability-heading")
    }
  }

}
