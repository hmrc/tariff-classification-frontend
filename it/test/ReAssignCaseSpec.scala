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
import play.api.test.Helpers.{CREATED, OK}
import utils.JsonFormatters._
import utils.{CasePayloads, Cases, EventPayloads}

class ReAssignCaseSpec extends IntegrationTest with MockitoSugar {

  private val owner = Some(Operator("111", role = Role.CLASSIFICATION_OFFICER))
  private val caseAssignedToOwner = CasePayloads.jsonOf(
    Cases.btiCaseExample
      .copy(
        status = CaseStatus.OPEN,
        assignee = owner
      )
  )
  private val caseUnassigned = CasePayloads.jsonOf(
    Cases.btiCaseExample
      .copy(
        status = CaseStatus.OPEN
      )
  )
  private val event = EventPayloads.event

  "Re-Assign Assigned Case" should {

    "return status 200 for manager" in {
      givenAuthSuccess()
      whenCaseExists(caseAssignedToOwner)
      shouldSucceed
    }

    "return status 200 for case owner" in {
      givenAuthSuccess("team")
      whenCaseExists(caseAssignedToOwner)
      shouldSucceed
    }

    "redirect for non case owner" in {
      givenAuthSuccess("another team member")
      whenCaseExists(caseAssignedToOwner)
      shouldFail
    }

    "redirect on auth failure" in {
      givenAuthFailed()
      whenCaseExists(caseAssignedToOwner)
      shouldFail
    }
  }

  "Re-Assign Unassigned Case" should {

    "return status 200 for manager" in {
      givenAuthSuccess()
      whenCaseExists(caseUnassigned)
      shouldSucceed
    }

    "redirect for team member" in {
      givenAuthSuccess("team")
      whenCaseExists(caseUnassigned)
      shouldFail
    }

    "redirect on auth failure" in {
      givenAuthFailed()
      whenCaseExists(caseUnassigned)
      shouldFail
    }
  }

  private def whenCaseExists(caseJson: String) = {
    stubFor(
      get(urlEqualTo("/cases/1"))
        .thenReturn(
          aResponse()
            .withStatus(OK)
            .withBody(caseJson)
        )
    )
    stubFor(
      post(urlEqualTo("/cases/1/events"))
        .thenReturn(
          aResponse()
            .withStatus(CREATED)
            .withBody(event)
        )
    )
  }

  private def shouldSucceed = {

    val response: WSResponse = await(requestWithSession("/cases/1/reassign-case?origin=/").get())

    response.status shouldBe OK
    response.body     should include("Choose a team to move this case to")
  }

  private def shouldFail = {

    val response: WSResponse = await(requestWithSession("/cases/1/reassign-case?origin=/").get())

    response.status shouldBe OK
    response.body     should include(messages("not_authorised.paragraph1"))
  }
}
