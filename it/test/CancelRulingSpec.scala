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
import models.{CaseStatus, Operator, Role}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import utils.Cases.{aCase, withDecision}
import utils.JsonFormatters._
import utils.{CasePayloads, EventPayloads}

class CancelRulingSpec extends IntegrationTest with MockitoSugar {

  "Cancel Ruling" should {
    val owner = Some(Operator("111", role = Role.CLASSIFICATION_OFFICER))
    val caseWithStatusCOMPLETE =
      CasePayloads.jsonOf(aCase(withDecision()).copy(assignee = owner, status = CaseStatus.COMPLETED))
    val event = EventPayloads.event

    "return status 200 for manager" in {

      givenAuthSuccess()
      shouldSucceed
    }

    "return status 200 for team member" in {
      givenAuthSuccess("team")
      shouldSucceed
    }

    "return status 200 for another team member" in {
      givenAuthSuccess("another team member")
      shouldSucceed
    }

    "redirect on auth failure" in {

      givenAuthFailed()
      shouldFail
    }

    def shouldSucceed = {
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(caseWithStatusCOMPLETE)
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

      val response: WSResponse = await(requestWithSession("/cases/1/ruling/cancel-reason").get())

      response.status shouldBe OK
      response.body     should include("Provide details to cancel")
    }

    def shouldFail = {

      val response: WSResponse = await(requestWithSession("/cases/1/ruling/cancel-reason").get())

      response.status shouldBe OK
      response.body     should include(messages("not_authorised.paragraph1"))
    }

  }

}
