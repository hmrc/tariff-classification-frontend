/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import connector.EmailConnector
import models.{Case, CaseCompletedEmail, CaseCompletedEmailParameters, EmailTemplate, Operator}
import utils.JsonFormatters.{emailCompleteParamsFormat, emailFormat}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import utils.Dates

@Singleton
class EmailService @Inject() (connector: EmailConnector) {

  def sendCaseCompleteEmail(c: Case, operator: Operator)(implicit hc: HeaderCarrier): Future[EmailTemplate] = {
    if (!c.application.isBTI) {
      throw new IllegalArgumentException("Cannot send email for non BTI types")
    }

    val email: CaseCompletedEmail = CaseCompletedEmail(
      Seq(c.application.contact.email),
      CaseCompletedEmailParameters(
        recipientName_line1 = c.application.contact.name,
        reference           = c.reference,
        goodsName           = c.application.asATAR.goodName,
        officerName         = operator.name.getOrElse(""),
        dateSubmitted       = Dates.format(c.createdDate)
      )
    )

    connector.send(email) flatMap (_ => connector.generate(email))
  }

}
