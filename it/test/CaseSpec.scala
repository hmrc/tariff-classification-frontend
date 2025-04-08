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
import models.{Case, Pagination}
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.test.Helpers._
import utils.Cases._
import utils.JsonFormatters._
import utils.{CasePayloads, EventPayloads, KeywordsPayloads}

class CaseSpec extends IntegrationTest {

  private val c: Case = aCase(withReference("1"), withoutAgent())

  "Unknown Case" should {

    "return status 200 with Case Not Found" in {

      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      val response = await(requestWithSession("/cases/1").get())

      response.status shouldBe NOT_FOUND
      response.body     should include("Case not found")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("/cases/1")
    }
  }

  "Case Summary" should {

    "return status 200" in {

      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.jsonOf(c))
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

      val response = await(requestWithSession("/cases/v2/1/atar").get())

      response.status shouldBe OK
      response.body     should include("id=\"trader-heading\"")
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("/cases/1")
    }
  }

  "Case Ruling Details" should {

    "return status SEE_OTHER" in {

      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.jsonOf(c))
          )
      )
      stubFor(
        post(urlEqualTo("/file?id="))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("[]")
          )
      )

      val response = await(requestWithSession("/cases/1/ruling").withFollowRedirects(false).get())

      response.status           shouldBe SEE_OTHER
      response.header("Location") should be(Some("/manage-tariff-classifications/cases/v2/1/atar#ruling_tab"))
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("/cases/1/ruling")
    }
  }

  "Case activity details" should {

    "return status SEE_OTHER" in {

      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.jsonOf(c))
          )
      )
      givenAuthSuccess()
      stubFor(
        get(
          urlEqualTo(
            s"/events?case_reference=1" +
              s"&type=EXPERT_ADVICE_RECEIVED&type=CASE_REJECTED" +
              "&type=SAMPLE_STATUS_CHANGE&type=SAMPLE_RETURN_CHANGE&type=SAMPLE_SEND_CHANGE" +
              s"&type=QUEUE_CHANGE&type=APPEAL_ADDED&type=APPEAL_STATUS_CHANGE&type=EXTENDED_USE_STATUS_CHANGE" +
              s"&type=CASE_STATUS_CHANGE&type=CASE_REFERRAL&type=NOTE&type=CASE_COMPLETED&type=CASE_CANCELLATION" +
              s"&type=CASE_CREATED&type=ASSIGNMENT_CHANGE&page=1&page_size=${Pagination.unlimited}"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(EventPayloads.pagedEvents)
        )
      )

      val response = await(requestWithSession("/cases/1/activity").withFollowRedirects(false).get())

      response.status           shouldBe SEE_OTHER
      response.header("Location") should be(Some("/manage-tariff-classifications/cases/v2/1/atar#activity_tab"))
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("/cases/1/activity")
    }
  }

  "Case attachments details" should {

    "return status SEE_OTHER" in {

      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.jsonOf(c))
          )
      )
      stubFor(
        post(urlEqualTo("/file?id="))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("[]")
          )
      )

      val response = await(requestWithSession("/cases/1/attachments").withFollowRedirects(false).get())

      response.status           shouldBe SEE_OTHER
      response.header("Location") should be(Some("/manage-tariff-classifications/cases/v2/1/atar#attachments_tab"))
    }

    "redirect on auth failure" in {
      verifyNotAuthorisedFor("/cases/1/attachments")
    }
  }

}
