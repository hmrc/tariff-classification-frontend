/*
 * Copyright 2021 HM Revenue & Customs
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

package models

import java.time.{Clock, Instant, ZoneOffset}

import org.scalatest.BeforeAndAfterAll
import utils.Cases
import utils.Cases.{aCase, withBTIApplication, _}

class CaseTest extends ModelsBaseSpec with BeforeAndAfterAll {

  private val pastTime    = Instant.parse("2010-01-01T01:01:00Z")
  private val currentTime = Instant.parse("2010-01-01T01:01:01Z")
  private val futureTime  = Instant.parse("2010-01-01T01:01:02Z")

  private implicit val clockWithFixedTime: Clock = Clock.fixed(currentTime, ZoneOffset.UTC)

  "hasExpiredRuling()'" should {

    "return false for cases without a decision" in {
      val c = caseWithRuling(None)

      c.hasExpiredRuling shouldBe false
    }

    "return false when 'effectiveEndDate' is not defined" in {
      val c = caseWithRuling(Some(rulingWithEffectiveEndDate(None)))

      c.hasExpiredRuling shouldBe false
    }

    "return true when 'effectiveEndDate' is a past time" in {
      val c = caseWithRuling(Some(rulingWithEffectiveEndDate(Some(pastTime))))

      c.hasExpiredRuling shouldBe true
    }

    "return false when 'effectiveEndDate' is the current time" in {
      val c = caseWithRuling(Some(rulingWithEffectiveEndDate(Some(currentTime))))

      c.hasExpiredRuling shouldBe false
    }

    "return false when 'effectiveEndDate' is a future time" in {
      val c = caseWithRuling(Some(rulingWithEffectiveEndDate(Some(futureTime))))

      c.hasExpiredRuling shouldBe false
    }

  }

  "hasLiveRuling()'" should {

    "return false for cases without a decision" in {
      val c = caseWithRuling(None)

      c.hasLiveRuling shouldBe false
    }

    "return false when 'effectiveEndDate' is not defined" in {
      val c = caseWithRuling(Some(rulingWithEffectiveEndDate(None)))

      c.hasLiveRuling shouldBe false
    }

    "return false when 'effectiveEndDate' is a past time" in {
      val c = caseWithRuling(Some(rulingWithEffectiveEndDate(Some(pastTime))))

      c.hasLiveRuling shouldBe false
    }

    "return true when 'effectiveEndDate' is the current time" in {
      val c = caseWithRuling(Some(rulingWithEffectiveEndDate(Some(currentTime))))

      c.hasLiveRuling shouldBe true
    }

    "return true when 'effectiveEndDate' is a future time" in {
      val c = caseWithRuling(Some(rulingWithEffectiveEndDate(Some(futureTime))))

      c.hasLiveRuling shouldBe true
    }

  }

  "isCaseOverdue" should {
    "return true if LiveLiability is above the overdue threshold (5 days)" in {
      val c = aCase(
        withReference("reference"),
        withLiabilityApplication(status = LiabilityStatus.LIVE),
        withSample(Sample(returnStatus = Some(SampleReturn.YES))),
        withDaysElapsed(6)
      )

      c.isCaseOverdue shouldBe true
    }

    "return false if LiveLiability is below the threshold (5 days)" in {
      val c = aCase(
        withReference("reference"),
        withLiabilityApplication(status = LiabilityStatus.LIVE),
        withSample(Sample(returnStatus = Some(SampleReturn.YES))),
        withDaysElapsed(3)
      )

      c.isCaseOverdue shouldBe false
    }

    "return true if Liability is above the overdue threshold (30 days)" in {
      val c = aCase(
        withReference("reference"),
        withLiabilityApplication(),
        withSample(Sample(returnStatus = Some(SampleReturn.YES))),
        withDaysElapsed(30)
      )

      c.isCaseOverdue shouldBe true
    }

    "return false if Liability is below the threshold (30 days)" in {
      val c = aCase(
        withReference("reference"),
        withLiabilityApplication(),
        withSample(Sample(returnStatus = Some(SampleReturn.YES))),
        withDaysElapsed(20)
      )

      c.isCaseOverdue shouldBe false
    }

    "return false if ATaR is below the threshold (30 days)" in {
      val c = aCase(
        withReference("reference"),
        withBTIApplication,
        withSample(Sample(returnStatus = Some(SampleReturn.YES))),
        withDaysElapsed(20)
      )

      c.isCaseOverdue shouldBe false
    }

    "return true if ATaR is above the threshold (30 days)" in {
      val c = aCase(
        withReference("reference"),
        withBTIApplication,
        withSample(Sample(returnStatus = Some(SampleReturn.YES))),
        withDaysElapsed(30)
      )

      c.isCaseOverdue shouldBe true
    }
  }

  "findAppeal()" should {

    "return appeal when valid id presented" in {
      val appeal = Appeal("12345", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_1)
      val c      = caseWithAppeal(appeal)

      c.findAppeal("12345") shouldBe Some(appeal)
    }

    "return none when invalid id presented" in {
      val appeal = Appeal("12345", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_1)
      val c      = caseWithAppeal(appeal)

      c.findAppeal("99999") shouldBe None
    }

    "return none when no appeals" in {
      val c = Cases.btiCaseExample

      c.findAppeal("99999") shouldBe None
    }
  }

  "sampleToBeProvided" should {
    "return true for BIT" in {
      val c = aCase(withReference("reference"), withBTIApplication, withBTIDetails(sampleToBeProvided = true))

      c.sampleToBeProvided shouldBe true
    }

    "return true for Liability" in {
      val c = aCase(
        withReference("reference"),
        withLiabilityApplication(),
        withSample(Sample(status = Some(SampleStatus.AWAITING)))
      )

      c.sampleToBeProvided shouldBe true
    }
  }

  "sampleToBeReturned" should {
    "return true for BIT" in {
      val c = aCase(withReference("reference"), withBTIApplication, withBTIDetails(sampleToBeReturned = true))

      c.sampleToBeReturned shouldBe true
    }

    "return true for Liability" in {
      val c = aCase(
        withReference("reference"),
        withLiabilityApplication(),
        withSample(Sample(returnStatus = Some(SampleReturn.YES)))
      )

      c.sampleToBeReturned shouldBe true
    }
  }

  "hasAssignee" should {
    "return true" in {
      val c = aCase(
        withReference("reference"),
        withBTIApplication,
        withBTIDetails(sampleToBeReturned = true)
      ).copy(assignee = Some(Cases.operatorWithAddAttachment))

      c.hasAssignee shouldBe true
    }

    "return false" in {
      val c = aCase(
        withReference("reference"),
        withBTIApplication,
        withBTIDetails(sampleToBeReturned = true)
      ).copy(assignee = None)

      c.hasAssignee shouldBe false
    }
  }

  private def rulingWithEffectiveEndDate(date: Option[Instant]): Decision =
    Cases.decision.copy(effectiveEndDate = date)

  private def caseWithRuling(d: Option[Decision]): Case =
    Cases.btiCaseExample.copy(decision = d)

  private def caseWithAppeal(appeal: Appeal): Case =
    Cases.btiCaseExample.copy(decision = Some(Cases.decision.copy(appeal = Seq(appeal))))
}
