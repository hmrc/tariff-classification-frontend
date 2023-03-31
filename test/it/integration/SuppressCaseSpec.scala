/*
 * Copyright 2023 HM Revenue & Customs
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
import models.CaseStatus
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import utils.JsonFormatters._
import utils.{CasePayloads, Cases, EventPayloads}

class SuppressCaseSpec extends IntegrationTest with MockitoSugar {

  "Case Suppress" should {
    val caseWithStatusNEW = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.NEW))
    val event             = EventPayloads.event

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(caseWithStatusNEW)
          )
      )
      stubFor(
        post(urlEqualTo("/cases/1/events"))
          .willReturn(
            aResponse()
              .withStatus(CREATED)
              .withBody(event)
          )
      )

      // When
      val response: WSResponse = await(requestWithSession("/cases/1/suppress-reason").get())

      // Then
      response.status shouldBe OK
      response.body   should include("Provide details to suppress this case")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(requestWithSession("/cases/1/suppress-reason").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }
  }

}
