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
import models.CaseStatus
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import utils.JsonFormatters._
import utils.{CasePayloads, Cases, EventPayloads}

class ReleaseCaseSpec extends IntegrationTest {

  "Case Release" should {
    val caseWithStatusNEW = CasePayloads.jsonOf(
      Cases.btiCaseExample.copy(
        status = CaseStatus.NEW,
        application = Cases.btiApplicationExample.copy(envisagedCommodityCode = Some("01234567890"))
      )
    )
    val event = EventPayloads.event

    "return status 200" in {

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

      val response: WSResponse =
        await(requestWithSession(s"/cases/1/release").get())

      response.status shouldBe OK
      response.body     should include("Choose a team to release this case to")
    }

    "redirect on auth failure" in {

      givenAuthFailed()

      val response: WSResponse =
        await(requestWithSession("/cases/1/release").get())

      response.status shouldBe OK
      response.body     should include(messages("not_authorised.paragraph1"))
    }
  }

  // TODO: DIT-246 - fix this test
//  "Case Release To Queue" should {
//    val caseWithStatusNEW = CasePayloads.jsonOf(CaseExamples.btiCaseExample.copy(status = CaseStatus.NEW))
//    val caseWithStatusOPEN = CasePayloads.jsonOf(CaseExamples.btiCaseExample.copy(status = CaseStatus.OPEN))
//    val token = fakeApplication().injector.instanceOf[TokenProvider].generateToken
//
//    "return status 200" in {
//
//      stubFor(get(urlEqualTo("/cases/1"))
//        .willReturn(aResponse()
//          .withStatus(OK)
//          .withBody(caseWithStatusNEW))
//      )
//      stubFor(put(urlEqualTo("/cases/1"))
//        .willReturn(aResponse()
//          .withStatus(OK)
//          .withBody(caseWithStatusOPEN))
//      )
//
//
//      val response: WSResponse = await(requestWithSession(s"http://localhost:$port/manage-tariff-classifications/cases/1/release").post(Map("queue" -> Seq("cars"), "csrfToken" -> Seq(token))))
//
//
//      response.status shouldBe OK
//      response.body should include("<h3 class=\"heading-medium mt-0\">This case has been released to the Cars queue</h3>")
//    }
//  }

}
