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
import models.{AppealType, CaseStatus, Operator, Role}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import utils.CasePayloads
import utils.Cases.{aCase, withDecision}
import utils.JsonFormatters._

class AppealCaseTypeSpec extends IntegrationTest with MockitoSugar {

  val owner: Option[Operator] = Some(Operator("111", role = Role.CLASSIFICATION_OFFICER))
  val caseWithStatusCOMPLETE: String =
    CasePayloads.jsonOf(aCase(withDecision()).copy(assignee = owner, status = CaseStatus.COMPLETED))

  "Case Review Change" should {

    "return status 200 for manager" in {
      givenAuthSuccess("manager")
      shouldSucceed()
    }

    "return status 200 for team member" in {
      givenAuthSuccess("team")
      shouldSucceed()
    }

    "return status 200 for another team member" in {
      givenAuthSuccess("another team member")
      shouldSucceed()
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()
      shouldFail
    }

    def shouldFail = {
      // When
      val response: WSResponse =
        await(requestWithSession(s"/cases/1/new-appeal/ANY").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }

    def shouldSucceed(): Unit = {
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(caseWithStatusCOMPLETE)
          )
      )

      AppealType.values.foreach { appealType =>
        // When
        val response: WSResponse =
          await(requestWithSession(s"/cases/1/new-appeal/$appealType").get())

        // Then
        response.status shouldBe OK
        response.body   should include("id=\"appeal_choose_status-heading\"")
      }
    }
  }
}
