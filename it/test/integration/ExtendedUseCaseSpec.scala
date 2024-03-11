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

package integration

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{CancelReason, Cancellation, CaseStatus}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import utils.CasePayloads
import utils.Cases._
import utils.JsonFormatters._

class ExtendedUseCaseSpec extends IntegrationTest with MockitoSugar {

  "Case Extended Use Change" should {
    val c = aCase(
      withReference("1"),
      withStatus(CaseStatus.CANCELLED),
      withDecision(cancellation = Some(Cancellation(reason = CancelReason.ANNULLED, applicationForExtendedUse = true)))
    )
    val caseWithStatusCOMPLETED = CasePayloads.jsonOf(c)

    "return status 200" in {

      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(caseWithStatusCOMPLETED)
          )
      )

      val response: WSResponse =
        await(requestWithSession("/cases/1/extended-use/status").get())

      response.status shouldBe OK
      response.body   should include("Do you want to extend the use of this case ruling?")
    }

    "redirect on auth failure" in {

      givenAuthFailed()

      val response: WSResponse =
        await(requestWithSession("/cases/1/extended-use/status").get())

      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }
  }

}
