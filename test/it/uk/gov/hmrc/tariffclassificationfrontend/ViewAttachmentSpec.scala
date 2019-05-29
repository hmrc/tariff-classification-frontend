package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.response.FileMetadata
import uk.gov.hmrc.tariffclassificationfrontend.utils.JsonFormatters.fileMetaDataFormat


class ViewAttachmentSpec extends IntegrationTest with MockitoSugar {

  private val fileMetadata = Json.toJson(FileMetadata("id", "filename", "mimeType")).toString()

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
      val response: WSResponse = await(ws.url(s"$baseUrl/attachment/id").get())

      // Then
      response.status shouldBe OK
      response.body should include("You are not authorised to access this page.")
    }

    def shouldSucceed = {
      // When
      stubFor(get(urlEqualTo("/file/id"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(fileMetadata))
      )

      // When
      val response: WSResponse = await(ws.url(s"$baseUrl/attachment/id").get())

      // Then
      response.status shouldBe OK
      response.body should include("Attachment is unavailable")
    }
  }

}
