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
import models.SampleStatus
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import utils.CasePayloads

class SampleStatusSpec extends IntegrationTest with MockitoSugar {

  "Sample Status'" should {

    "Return all options for BTI Case" in {

      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.simpleBtiCase)
          )
      )

      val response: WSResponse = await(requestWithSession("/cases/1/sample/status").get())

      response.status shouldBe OK
      SampleStatus.values.foreach(s => response.body should include(s">${SampleStatus.format(Some(s))}<"))
    }

    "Return limited options for Liability Case" in {

      givenAuthSuccess()
      stubFor(
        get(urlEqualTo("/cases/1"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(CasePayloads.simpleLiabilityCase)
          )
      )

      val response: WSResponse = await(requestWithSession("/cases/1/sample/status?options=liability").get())

      response.status shouldBe OK
      response.body   should include(">Yes<")
      response.body   should include(">No<")
      response.body   should include("AWAITING")

      response.body shouldNot include(s">${SampleStatus.format(Some(SampleStatus.RETURNED_APPLICANT))}<")
    }
  }

}
