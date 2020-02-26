package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.ws.WSResponse
import play.api.test.Helpers._
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, Cases}

class PdfGenerationSpec extends IntegrationTest {

  private val c = CasePayloads.jsonOf(Cases.btiCaseExample.copy(status = CaseStatus.COMPLETED))
  private val caseRef = 12
  private val pdfGeneratorServiceUrl = "/pdf-generator-service/generate"

  "PDF Application" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()

      stubFor(get(urlEqualTo(s"/cases/$caseRef"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(c))
      )

      stubFor(post(urlEqualTo(pdfGeneratorServiceUrl))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody("my application pdf content".getBytes))
      )

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/manage-tariff-classifications/pdf/application/$caseRef").get())

      // Then
      response.status shouldBe OK
      response.body shouldBe "my application pdf content"
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/manage-tariff-classifications/pdf/application/$caseRef").get())

      // Then
      response.status shouldBe OK
      response.body should include(messages("not_authorised.paragraph1"))
    }

  }

  "PDF Ruling" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()

      stubFor(get(urlEqualTo(s"/cases/$caseRef"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(c))
      )

      stubFor(post(urlEqualTo(pdfGeneratorServiceUrl))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody("my ruling pdf content".getBytes))
      )

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/manage-tariff-classifications/pdf/ruling/$caseRef").get())

      // Then
      response.status shouldBe OK
      response.body shouldBe "my ruling pdf content"
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response: WSResponse = await(ws.url(s"http://localhost:$port/manage-tariff-classifications/pdf/ruling/$caseRef").get())

      // Then
      response.status shouldBe OK
      response.body should include(messages("not_authorised.paragraph1"))
    }

  }

}
