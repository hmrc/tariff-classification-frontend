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
import models.{CaseStatus, Decision, Operator, Role}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import utils.JsonFormatters._
import utils.{CasePayloads, Cases}

class CompleteCaseSpec extends IntegrationTest with MockitoSugar {

  "Case Complete with decision" should {

    val owner = Some(Operator("111", role = Role.CLASSIFICATION_OFFICER))
    val completeDecision = Decision(
      bindingCommodityCode = "0300000000",
      justification        = "justification-content",
      goodsDescription     = "goods-description",
      methodSearch         = Some("method-to-search"),
      explanation          = Some("explanation")
    )
    val caseWithStatusOPEN = CasePayloads.jsonOf(
      Cases.btiCaseExample.copy(status = CaseStatus.OPEN, decision = Some(completeDecision), assignee = owner)
    )

    def shouldSucceed = {
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(caseWithStatusOPEN)
          )
      )

      val response: WSResponse =
        await(requestWithSession("/cases/1/complete").get())

      response.status shouldBe OK
      response.body   should include("Are you sure you want to complete the Laptop case?")
      response.body   should not include "disabled=disabled"
    }

    def shouldNotSucceed = {
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(caseWithStatusOPEN)
          )
      )

      val response: WSResponse =
        await(requestWithSession("/cases/1/complete").get())

      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }

    "return status 200 for manager" in {

      givenAuthSuccess()
      shouldSucceed
    }

    "return status 200 for read only" in {

      givenAuthSuccess("read-only")
      shouldNotSucceed
    }

    "return status 200 for team member" in {
      givenAuthSuccess("team")
      shouldSucceed
    }

    "redirect on auth failure" in {

      givenAuthFailed()
      shouldFail
    }

    "redirect for non case owner" in {

      givenAuthSuccess("another team member")
      shouldFail
    }

    def shouldFail = {

      val response: WSResponse =
        await(requestWithSession("/cases/1/complete").get())

      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }
  }

  //TODO DIT-291 This returns 403s due to CSRF issues
//  "Case Complete Confirm" should {
//    val caseWithStatusOPEN = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.OPEN))
//    val event = EventPayloads.event
//
//    val csrfProvider = injector.instanceOf[TokenProvider]
//
//    "return status 200" in {
//
//      givenAuthSuccess()
//      stubFor(get(urlEqualTo("/cases/1"))
//        .willReturn(aResponse()
//          .withStatus(OK)
//          .withBody(caseWithStatusOPEN))
//      )
//      stubFor(post(urlEqualTo("/cases/1/events"))
//        .willReturn(aResponse()
//          .withStatus(CREATED)
//          .withBody(event))
//      )
//      stubFor(post(urlEqualTo("/hmrc/email"))
//        .willReturn(aResponse()
//          .withStatus(ACCEPTED))
//      )
//      stubFor(post(urlEqualTo(s"/templates/${EmailType.COMPLETE}"))
//        .withRequestBody(new EqualToJsonPattern(fromResource("parameters_email-request.json"), true, false))
//        .willReturn(aResponse()
//          .withBody(fromResource("email_template-response.json"))
//          .withStatus(HttpStatus.SC_OK))
//      )
//
//
//      val response: WSResponse = await(requestWithSession(s"/cases/1/complete").post(Map("csrfToken" -> Seq(csrfProvider.generateToken))))
//
//
//      response.status shouldBe OK
//      response.body should include("<h3 class=\"heading-large mt-0\">Complete this case</h3>")
//    }
//
//    "redirect on auth failure" in {
//
//      givenAuthFailed()
//
//
//      val response: WSResponse = await(requestWithSession(s"/cases/1/complete").post(Map("csrfToken" -> Seq(csrfProvider.generateToken))))
//
//
//      response.status shouldBe OK
//      response.body should include("You are not authorised to access this page.")
//    }
//  }

}
