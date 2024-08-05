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

package service

import connector.EmailConnector
import models._
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future.successful

class EmailServiceSpec extends ServiceSpecBase {

  private val connector = mock[EmailConnector]
  private val service   = new EmailService(connector)

  "Email Service 'sendCompleteCaseEmail'" should {
    val aCase       = mock[Case]
    val anOperator  = Operator("1", Some("officer"))
    val application = mock[BTIApplication]
    val contact     = Contact("name", "email", None)
    val template    = mock[EmailTemplate]

    "Throw exception for non-bti" in {
      given(aCase.application).willReturn(application)
      given(application.isBTI).willReturn(false)

      val exception = intercept[IllegalArgumentException] {
        await(service.sendCaseCompleteEmail(aCase, anOperator))
      }
      exception.getMessage shouldBe "Cannot send email for non BTI types"
    }

    "Delegate to connector" in {
      val aDate = LocalDate
        .of(2021, 1, 1)
        .atStartOfDay()
        .atZone(ZoneOffset.UTC)
        .toInstant

      given(aCase.reference).willReturn("ref")
      given(aCase.application).willReturn(application)
      given(aCase.createdDate).willReturn(aDate)
      given(application.isBTI).willReturn(true)
      given(application.asATAR).willReturn(application)
      given(application.contact).willReturn(contact)
      given(application.goodName).willReturn("item")

      given(connector.send(any[CaseCompletedEmail])(any[HeaderCarrier]))
        .willReturn(successful((): Unit))
      given(connector.generate(any[CaseCompletedEmail])(any[HeaderCarrier]))
        .willReturn(successful(template))

      await(service.sendCaseCompleteEmail(aCase, anOperator))

      verify(connector).send(
        refEq(
          CaseCompletedEmail(
            Seq("email"),
            CaseCompletedEmailParameters(
              recipientName_line1 = "name",
              reference = "ref",
              goodsName = "item",
              officerName = "officer",
              dateSubmitted = "01 Jan 2021"
            )
          )
        )
      )(any[HeaderCarrier])
    }
  }

}
