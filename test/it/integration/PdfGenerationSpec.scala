package integration

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import models.CaseStatus
import models.response.FileMetadata
import utils.{CasePayloads, Cases}
import utils.JsonFormatters._

class PdfGenerationSpec extends IntegrationTest {

  private val cse     = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED))
  private val caseRef = 12
  private val pdfUrl  = s"${wireMockUrl}/digital-tariffs-local/id"
  private val pdfMeta = CasePayloads.jsonOf(Some(FileMetadata("id", Some("some.pdf"), Some("application/pdf"), Some(pdfUrl))))

  "PDF Application" should {

    "return status 200" in {
      // Given
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

      // When
      val response: WSResponse =
        await(ws.url(s"http://localhost:$port/manage-tariff-classifications/pdf/application/$caseRef").get())

      // Then
      response.status shouldBe OK
      response.body   shouldBe "my application pdf content"
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse =
        await(ws.url(s"http://localhost:$port/manage-tariff-classifications/pdf/application/$caseRef").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }

  }

  "PDF Ruling" should {

    "return status 200" in {
      // Given
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

      // When
      val response: WSResponse =
        await(ws.url(s"http://localhost:$port/manage-tariff-classifications/pdf/ruling/$caseRef").get())

      // Then
      response.status shouldBe OK
      response.body   shouldBe "my ruling pdf content"
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse =
        await(ws.url(s"http://localhost:$port/manage-tariff-classifications/pdf/ruling/$caseRef").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }

  }

}
