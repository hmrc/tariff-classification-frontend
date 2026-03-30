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
import models.response.FileMetadata
import play.api.libs.ws.WSResponse
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.test.Helpers._
import utils.JsonFormatters._
import utils.{CasePayloads, Cases}

class PdfGenerationSpec extends IntegrationTest {

  private val cse     = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED))
  private val caseRef = 12
  private val pdfUrl  = s"$wireMockUrl/digital-tariffs-local/id"
  private val pdfMeta =
    CasePayloads.jsonOf(Some(FileMetadata("id", Some("some.pdf"), Some("application/pdf"), Some(pdfUrl))))

  "PDF Application" should {

    "return status 200" in {

      givenAuthSuccess()

      stubFor(
        get(urlEqualTo(s"/cases/$caseRef"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(cse)
          )
      )

      stubFor(
        get(urlEqualTo("/file/id"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(pdfMeta)
          )
      )

      stubFor(
        get(urlEqualTo("/digital-tariffs-local/id"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("my application pdf content".getBytes)
          )
      )

      val response: WSResponse =
        await(requestWithSession(s"/pdf/application/$caseRef").get())

      response.status shouldBe OK
      response.body   shouldBe "my application pdf content"
    }

    "redirect on auth failure" in {

      givenAuthFailed()

      val response: WSResponse =
        await(requestWithSession(s"/pdf/application/$caseRef").get())

      response.status shouldBe OK
      response.body     should include(messages("not_authorised.paragraph1"))
    }

  }

  "PDF Ruling" should {

    "return status 200" in {

      givenAuthSuccess()

      stubFor(
        get(urlEqualTo(s"/cases/$caseRef"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(cse)
          )
      )

      stubFor(
        get(urlEqualTo("/file/id"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(pdfMeta)
          )
      )

      stubFor(
        get(urlEqualTo("/digital-tariffs-local/id"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("my ruling pdf content".getBytes)
          )
      )

      val response: WSResponse =
        await(requestWithSession(s"/pdf/ruling/$caseRef").get())

      response.status shouldBe OK
      response.body   shouldBe "my ruling pdf content"
    }

    "redirect on auth failure" in {

      givenAuthFailed()

      val response: WSResponse =
        await(requestWithSession(s"/pdf/ruling/$caseRef").get())

      response.status shouldBe OK
      response.body     should include(messages("not_authorised.paragraph1"))
    }

  }

}
