package uk.gov.hmrc.tariffclassificationfrontend

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.tariffclassificationfrontend.utils.{CasePayloads, EventPayloads}


class CaseSpec extends IntegrationTest with MockitoSugar {

  "Unknown Case" should {

    "return status 200 with Case Not Found" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(NOT_FOUND))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/cases/1").get())

      // Then
      response.status shouldBe OK
      response.body should include("Case not found")
    }

    "redirect on auth failure" in {
      notAuthorisedFor("")
    }
  }

  "Case Summary" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.btiCase))
      )

      // When
      val response = await(ws.url(s"$frontendRoot/cases/1").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"summary-heading\"")
    }

    "redirect on auth failure" in {
      notAuthorisedFor("")
    }
  }

  "Case Application Details" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.btiCase))
      )
      stubFor(post(urlEqualTo("/file?id="))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody("[]")
        )
      )

      // When
      val response = await(ws.url(s"$frontendRoot/cases/1/application").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"application-heading\"")
    }

    "redirect on auth failure" in {
      notAuthorisedFor("application")
    }
  }

  "Case Ruling Details" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.btiCase))
      )
      stubFor(post(urlEqualTo("/file?id="))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody("[]")
        )
      )

      // When
      val response = await(ws.url(s"$frontendRoot/cases/1/ruling").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"ruling-heading\"")
    }

    "redirect on auth failure" in {
      notAuthorisedFor("ruling")
    }
  }

  "Case activity details" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.btiCase))
      )
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1/events"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(EventPayloads.events))
      )
      // When
      val response = await(ws.url(s"$frontendRoot/cases/1/activity").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"activity-heading\"")
    }

    "redirect on auth failure" in {
      notAuthorisedFor("activity")
    }
  }


  "Case attachments details" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(get(urlEqualTo("/cases/1"))
        .willReturn(aResponse()
          .withStatus(OK)
          .withBody(CasePayloads.btiCase))
      )
      stubFor(post(urlEqualTo("/file?id="))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody("[]")
        )
      )

      // When
      val response = await(ws.url(s"$frontendRoot/cases/1/attachments").get())

      // Then
      response.status shouldBe OK
      response.body should include("id=\"attachments-heading\"")
    }

    "redirect on auth failure" in {
      notAuthorisedFor("attachments")
    }
  }

  private def notAuthorisedFor(tabName : String) = {
    // Given
    givenAuthFailed()

    // When
    val response = await(ws.url(s"$frontendRoot/cases/1/$tabName").get())

    // Then
    response.status shouldBe OK
    response.body should include("You are not authorised to access this page.")
  }
}
