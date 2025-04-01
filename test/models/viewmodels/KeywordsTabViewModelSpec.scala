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

package models.viewmodels

import base.SpecBase
import models.{Attachment, BTIApplication, Case, CaseStatus, Contact, Decision, EORIDetails, Operator, Sample}

import java.time.Instant

class KeywordsTabViewModelSpec extends SpecBase {
  "KeywordsTabViewModel" should {
    "create an object from Case" in {
      val caseClass =
        KeywordsTabViewModel(caseReference = "test", caseKeywords = Set("a", "bc"), globalKeywords = Seq("abc", "def"))
      val objectFromCase = KeywordsTabViewModel.fromCase(
        Case(
          reference = "test",
          status = CaseStatus.REFERRED,
          createdDate = Instant.parse("2020-01-01T09:00:00.00Z"),
          daysElapsed = 2,
          caseBoardsFileNumber = Some("sdf"),
          assignee = Some(Operator("dsacd")),
          queueId = Some("dfss"),
          application = BTIApplication(
            holder = EORIDetails(
              eori = "sfd",
              businessName = "sdf",
              addressLine1 = "sdf",
              addressLine2 = "sdfd",
              addressLine3 = "dsf",
              postcode = "sdf",
              country = "sdf"
            ),
            contact = Contact(name = "efwef", email = "wefd"),
            offline = true,
            goodName = "sdd",
            goodDescription = "sdfsd",
            confidentialInformation = None,
            otherInformation = None,
            reissuedBTIReference = None,
            knownLegalProceedings = None,
            envisagedCommodityCode = None,
            sampleToBeProvided = true,
            sampleToBeReturned = true,
            applicationPdf = None
          ),
          decision = Some(
            Decision(
              bindingCommodityCode = "dsff",
              effectiveStartDate = None,
              effectiveEndDate = None,
              justification = "fds",
              goodsDescription = "sdf",
              methodSearch = None,
              methodExclusion = None,
              methodCommercialDenomination = None,
              appeal = Seq.empty,
              cancellation = None,
              decisionPdf = None,
              letterPdf = None
            )
          ),
          attachments = Seq(
            Attachment(
              id = "asd",
              operator = None
            )
          ),
          keywords = Set("a", "bc"),
          sample = Sample(),
          dateOfExtract = Some(Instant.parse("2020-01-01T09:00:00.00Z")),
          migratedDaysElapsed = Some(4),
          referredDaysElapsed = 2
        ),
        globalKeywords = Seq("abc", "def")
      )
      caseClass shouldBe objectFromCase
    }
  }
}
