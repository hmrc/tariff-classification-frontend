/*
 * Copyright 2025 HM Revenue & Customs
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

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.ws.WSResponse
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.test.Helpers._
import utils.JsonFormatters._
import utils.{CasePayloads, Cases, EventPayloads}

class AssignCaseSpec extends IntegrationTest {

  "Case Assign" should {
    val caseWithStatusOPEN = CasePayloads.jsonOf(Cases.btiCaseExample.copy(queueId = Some("1"), assignee = None))
    val event              = EventPayloads.event

    "return status 200" in {

      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(caseWithStatusOPEN)
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

      val response: WSResponse = await(requestWithSession("/cases/1/assign").get())

      response.status shouldBe OK
      response.body     should include("assign_case-heading")
    }

    "redirect on auth failure" in {

      givenAuthFailed()

      val response: WSResponse = await(requestWithSession("/cases/1/assign").get())

      response.status shouldBe OK
      response.body     should include(messages("not_authorised.paragraph1"))
    }
  }

}
