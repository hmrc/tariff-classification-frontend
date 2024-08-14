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

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{CaseStatus, Operator, Pagination, Role}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import utils.Cases.{aCase, withDecision}
import utils.JsonFormatters._
import utils.{CasePayloads, EventPayloads, KeywordsPayloads}

class AppealCaseSpec extends IntegrationTest with MockitoSugar {

  val owner: Some[Operator] = Some(Operator("111", role = Role.CLASSIFICATION_OFFICER))
  val caseWithStatusCOMPLETE: String =
    CasePayloads.jsonOf(aCase(withDecision()).copy(assignee = owner, status = CaseStatus.COMPLETED))

  "Case Appeal" should {

    "return status 200 for manager" in {

      givenAuthSuccess()
      shouldSucceed
    }

    "return status 200 for team member" in {
      givenAuthSuccess("team")
      shouldSucceed
    }
// TODO GASTON - ONE FAST MOVE
    "return status 200 for another team member" in {
      givenAuthSuccess("another team member")
      shouldSucceed
    }

    "redirect on auth failure" in {

      givenAuthFailed()
      shouldFail
    }

    def shouldFail = {

      val response: WSResponse =
        await(requestWithSession("/cases/1/appeal").get())

      response.status shouldBe OK
      response.body     should include(messages("not_authorised.paragraph1"))
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
        get(
          urlEqualTo(
            "/events?case_reference=1" +
              "&type=SAMPLE_STATUS_CHANGE&type=SAMPLE_RETURN_CHANGE&type=SAMPLE_SEND_CHANGE" +
              s"&page=1&page_size=${Pagination.unlimited}"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(EventPayloads.pagedSampleEvents)
        )
      )
      stubFor(
        get(
          urlEqualTo(
            "/events?case_reference=1" +
              "&type=EXPERT_ADVICE_RECEIVED&type=CASE_REJECTED" +
              "&type=SAMPLE_SEND_CHANGE" +
              "&type=EXTENDED_USE_STATUS_CHANGE" +
              "&type=CASE_STATUS_CHANGE" +
              "&type=CASE_REFERRAL" +
              "&type=NOTE" +
              "&type=CASE_COMPLETED" +
              "&type=CASE_CANCELLATION" +
              "&type=SAMPLE_STATUS_CHANGE" +
              "&type=CASE_CREATED&type=ASSIGNMENT_CHANGE" +
              "&type=QUEUE_CHANGE&type=APPEAL_ADDED" +
              "&type=APPEAL_STATUS_CHANGE" +
              "&type=SAMPLE_RETURN_CHANGE" +
              s"&page=1&page_size=${Pagination.unlimited}"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(EventPayloads.pagedEvents)
        )
      )
      stubFor(
        get(
          urlEqualTo(
            s"/keywords?page=1&page_size=${Pagination.unlimited}"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(KeywordsPayloads.pagedKeywords)
        )
      )
      stubFor(
        post(urlEqualTo("/file/initiate"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(fromResource("filestore/binding-tariff-filestore_initiate-response.json"))
          )
      )

      val response: WSResponse =
        await(requestWithSession("/cases/v2/1/atar").get())

      response.status shouldBe OK
      response.body     should include("id=\"appeal-heading\"")
    }
  }

  "Case Appeal Change" should {

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

      val response: WSResponse =
        await(requestWithSession("/cases/1/new-appeal").get())

      response.status shouldBe OK
      response.body     should include("id=\"appeal_choose_type-heading\"")
    }

    def shouldFail = {

      val response: WSResponse =
        await(requestWithSession("/cases/1/new-appeal").get())

      response.status shouldBe OK
      response.body     should include(messages("not_authorised.paragraph1"))
    }

  }

}
