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

import akka.util.ByteString
import com.github.tomakehurst.wiremock.client.WireMock._
import models.CaseStatus
import models.response.FileMetadata
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import utils.JsonFormatters.{caseFormat, fileMetaDataFormat}
import utils.{CasePayloads, Cases}

class ViewAttachmentSpec extends IntegrationTest with MockitoSugar {

  private val cse     = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED))
  private val caseRef = 123456
  private val fileMetadata = Json
    .toJson(FileMetadata("id", Some("file.txt"), Some("text/plain"), Some(s"$wireMockUrl/$caseRef/file.txt")))
    .toString()

  "View Attachment" should {

    "return status 200 for manager" in {
      givenAuthSuccess()
      shouldSucceed
    }

    "return status 200 for team member" in {
      givenAuthSuccess("team")
      shouldSucceed
    }

    "return status 200 for read-only user" in {
      givenAuthSuccess("read-only")
      shouldSucceed
    }

    "redirect on auth failure" in {
      givenAuthFailed()
      shouldFail
    }

    def shouldFail = {

      val response: WSResponse = await(requestWithSession("/attachment/ref/id").get())


      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }

    def shouldSucceed = {

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
              .withBody(fileMetadata)
          )
      )

      stubFor(
        get(urlEqualTo(s"/$caseRef/file.txt"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("FILE_CONTENTS")
          )
      )


      val response: WSResponse = await(requestWithSession(s"/attachment/$caseRef/id").get())


      response.status      shouldBe OK
      response.bodyAsBytes shouldBe ByteString("FILE_CONTENTS")
    }
  }

}
