/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.service

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{Format, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.connector.EmailConnector
import uk.gov.hmrc.tariffclassificationfrontend.models._

import scala.concurrent.Future.successful

class EmailServiceTest extends UnitSpec with MockitoSugar {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val connector = mock[EmailConnector]
  private val service = new EmailService(connector)

  "Email Service 'sendCompleteCaseEmail'" should {
    val aCase = mock[Case]
    val application = mock[BTIApplication]
    val contact = Contact("name", "email", None)
    val template = mock[EmailTemplate]

    "Throw exception for non-bti" in {
      given(aCase.application).willReturn(application)
      given(application.isBTI).willReturn(false)

      val exception = intercept[IllegalArgumentException] {
        await(service.sendCaseCompleteEmail(aCase))
      }
      exception.getMessage shouldBe "Cannot send email for non BTI types"
    }

    "Delegate to connector" in {
      given(aCase.reference).willReturn("ref")
      given(aCase.application).willReturn(application)
      given(application.isBTI).willReturn(true)
      given(application.asBTI).willReturn(application)
      given(application.contact).willReturn(contact)
      given(application.goodName).willReturn("item")

      given(connector.send(any[CaseCompletedEmail])(any[HeaderCarrier], any[Writes[Any]])).willReturn(successful((): Unit))
      given(connector.generate(any[CaseCompletedEmail])(any[HeaderCarrier], any[Format[CaseCompletedEmailParameters]])).willReturn(successful(template))

      await(service.sendCaseCompleteEmail(aCase))

      verify(connector).send(refEq(CaseCompletedEmail(Seq("email"), CaseCompletedEmailParameters("name", "ref", "item"))))(any[HeaderCarrier], any[Writes[Any]])
    }
  }

}
