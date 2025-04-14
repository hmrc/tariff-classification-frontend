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

package services

import connectors.EmailConnector
import models._
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.*
import play.api.libs.json.{Format, Writes}
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
      when(aCase.application).thenReturn(application)
      when(application.isBTI).thenReturn(false)

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

      when(aCase.reference).thenReturn("ref")
      when(aCase.application).thenReturn(application)
      when(aCase.createdDate).thenReturn(aDate)
      when(application.isBTI).thenReturn(true)
      when(application.asATAR).thenReturn(application)
      when(application.contact).thenReturn(contact)
      when(application.goodName).thenReturn("item")

      when(connector.send(any[CaseCompletedEmail])(any[HeaderCarrier], any[Writes[Email[?]]]))
        .thenReturn(successful((): Unit))
      when(connector.generate(any[CaseCompletedEmail])(any[HeaderCarrier], any[Format[CaseCompletedEmailParameters]]))
        .thenReturn(successful(template))

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
      )(any[HeaderCarrier], any[Writes[Email[?]]])
    }
  }

}
