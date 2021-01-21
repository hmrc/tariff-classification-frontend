package integration

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import models.{ApplicationType, NoPagination}
import utils.{CasePayloads, CaseQueueBuilder}

class QueuesSpec extends IntegrationTest with MockitoSugar with CaseQueueBuilder {

  "My Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(
          urlEqualTo(
            buildQueryUrl(
              types        = ApplicationType.values.toSeq,
              withStatuses = "NEW,OPEN,REFERRED,SUSPENDED",
              assigneeId   = "123",
              pag          = NoPagination()
            )
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.pagedGatewayCases)
        )
      )

      stubFor(
        get(
          urlEqualTo(
            "/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id%2Capplication-type&report_field=active-days-elapsed"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.report)
        )
      )

      // When
      val response = await(ws.url(s"$baseUrl/queues").get())

      // Then
      response.status shouldBe OK
      response.body   should include("Cases for Forename Surname")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl/queues").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }
  }

  "Gateway Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()

      stubFor(
        get(
          urlEqualTo(
            buildQueryUrl(
              types = Seq(
                ApplicationType.ATAR,
                ApplicationType.LIABILITY,
                ApplicationType.CORRESPONDENCE,
                ApplicationType.MISCELLANEOUS
              ),
              withStatuses = "NEW,OPEN,REFERRED,SUSPENDED",
              queueId      = "none",
              assigneeId   = "none",
              pag          = NoPagination()
            )
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.pagedGatewayCases)
        )
      )

      stubFor(
        get(
          urlEqualTo(
            "/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id&report_field=active-days-elapsed"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.report)
        )
      )

      // When
      val response = await(ws.url(s"$baseUrl/queues/gateway").get())

      // Then
      response.status shouldBe OK
      response.body   should include("Gateway cases")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl/queues/gateway").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }
  }

  "ACT Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(
          urlEqualTo(
            buildQueryUrl(
              types = Seq(
                ApplicationType.ATAR,
                ApplicationType.LIABILITY,
                ApplicationType.CORRESPONDENCE,
                ApplicationType.MISCELLANEOUS
              ),
              withStatuses = "NEW,OPEN,REFERRED,SUSPENDED",
              queueId      = "2",
              assigneeId   = "none",
              pag          = NoPagination()
            )
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.pagedGatewayCases)
        )
      )

      stubFor(
        get(
          urlEqualTo(
            "/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id&report_field=active-days-elapsed"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.report)
        )
      )

      // When
      val response = await(ws.url(s"$baseUrl/queues/act").get())

      // Then
      response.status shouldBe OK
      response.body   should include("ACT BTI cases")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl/queues/gateway").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }
  }

  "CAP Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(
          urlEqualTo(
            buildQueryUrl(
              types = Seq(
                ApplicationType.ATAR,
                ApplicationType.LIABILITY,
                ApplicationType.CORRESPONDENCE,
                ApplicationType.MISCELLANEOUS
              ),
              withStatuses = "NEW,OPEN,REFERRED,SUSPENDED",
              queueId      = "3",
              assigneeId   = "none",
              pag          = NoPagination()
            )
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.pagedGatewayCases)
        )
      )

      stubFor(
        get(
          urlEqualTo(
            "/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id%2Capplication-type&report_field=active-days-elapsed"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.report)
        )
      )

      // When
      val response = await(ws.url(s"$baseUrl/queues/cap").get())

      // Then
      response.status shouldBe OK
      response.body   should include("CAP BTI cases")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl/queues/gateway").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }
  }

  "CAR Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(
          urlEqualTo(
            buildQueryUrl(
              types = Seq(
                ApplicationType.ATAR,
                ApplicationType.LIABILITY,
                ApplicationType.CORRESPONDENCE,
                ApplicationType.MISCELLANEOUS
              ),
              withStatuses = "NEW,OPEN,REFERRED,SUSPENDED",
              queueId      = "4",
              assigneeId   = "none",
              pag          = NoPagination()
            )
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.pagedGatewayCases)
        )
      )

      stubFor(
        get(
          urlEqualTo(
            "/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id%2Capplication-type&report_field=active-days-elapsed"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.report)
        )
      )

      // When
      val response = await(ws.url(s"$baseUrl/queues/car").get())

      // Then
      response.status shouldBe OK
      response.body   should include("CAR BTI cases")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl/queues/car").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }
  }

  "ELM Cases" should {

    "return status 200" in {
      // Given
      givenAuthSuccess()
      stubFor(
        get(
          urlEqualTo(
            buildQueryUrl(
              types = Seq(
                ApplicationType.ATAR,
                ApplicationType.LIABILITY,
                ApplicationType.CORRESPONDENCE,
                ApplicationType.MISCELLANEOUS
              ),
              withStatuses = "NEW,OPEN,REFERRED,SUSPENDED",
              queueId      = "5",
              assigneeId   = "none",
              pag          = NoPagination()
            )
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.pagedGatewayCases)
        )
      )

      stubFor(
        get(
          urlEqualTo(
            "/report?status=NEW&status=OPEN&status=REFERRED&status=SUSPENDED&assignee_id=none&report_group=queue-id%2Capplication-type&report_field=active-days-elapsed"
          )
        ).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(CasePayloads.report)
        )
      )

      // When
      val response = await(ws.url(s"$baseUrl/queues/elm").get())

      // Then
      response.status shouldBe OK
      response.body   should include("ELM BTI cases")
    }

    "redirect on auth failure" in {
      // Given
      givenAuthFailed()

      // When
      val response = await(ws.url(s"$baseUrl/queues/elm").get())

      // Then
      response.status shouldBe OK
      response.body   should include(messages("not_authorised.paragraph1"))
    }
  }

}
