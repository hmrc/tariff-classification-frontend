package integration

import akka.util.ByteString
import com.github.tomakehurst.wiremock.client.WireMock._
import models.CaseStatus
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import models.response.FileMetadata
import utils.{CasePayloads, Cases}
import utils.JsonFormatters.{caseFormat, fileMetaDataFormat}

class ViewAttachmentSpec extends IntegrationTest with MockitoSugar {

  private val cse     = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED))
  private val caseRef = 123456
  private val fileMetadata = Json.toJson(FileMetadata("id", "file.txt", "text/plain", Some(s"$wireMockUrl/$caseRef/file.txt"))).toString()

  "View Attachment" should {

    "return status 200 for manager" in {
      givenAuthSuccess("manager")
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
      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/attachment/ref/id").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }

    def shouldSucceed = {
      // When
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
        get(urlEqualTo(s"/${caseRef}/file.txt"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody("FILE_CONTENTS")
          )
      )

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/attachment/$caseRef/id").get())

      // Then
      response.status shouldBe OK
      response.bodyAsBytes shouldBe ByteString("FILE_CONTENTS")
    }
  }

}
