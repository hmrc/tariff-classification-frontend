/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.models

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.tariffclassificationfrontend.utils.Cases
import java.time.{Clock, Instant, ZoneOffset}

import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.tariffclassificationfrontend.utils.Cases.{aCase, withBTIApplication, _}

class CaseTest extends UnitSpec with MockitoSugar with BeforeAndAfterAll {

  private implicit val appConfig: AppConfig = mock[AppConfig]

  private val pastTime = Instant.parse("2010-01-01T01:01:00Z")
  private val currentTime = Instant.parse("2010-01-01T01:01:01Z")
  private val futureTime = Instant.parse("2010-01-01T01:01:02Z")

  private val clockWithFixedTime = Clock.fixed(currentTime, ZoneOffset.UTC)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    when(appConfig.clock).thenReturn(clockWithFixedTime)
  }

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

  "findAppeal()" should {

    "return appeal when valid id presented" in {
      val appeal = Appeal("12345", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_1)
      val c = caseWithAppeal(appeal)

      c.findAppeal("12345") shouldBe Some(appeal)
    }

    "return none when invalid id presented" in {
      val appeal = Appeal("12345", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_1)
      val c = caseWithAppeal(appeal)

      c.findAppeal("99999") shouldBe None
    }

    "return none when no appeals" in {
      val appeal = Appeal("12345", AppealStatus.ALLOWED, AppealType.APPEAL_TIER_1)
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
      val c = aCase(withReference("reference"), withLiabilityApplication, withSample(Sample(status = Some(SampleStatus.AWAITING))))

      c.sampleToBeProvided shouldBe true
    }
  }

  "sampleToBeReturned" should {
    "return true for BIT" in {
      val c = aCase(withReference("reference"), withBTIApplication, withBTIDetails(sampleToBeReturned = true))

      c.sampleToBeReturned shouldBe true
    }

    "return true for Liability" in {
      val c = aCase(withReference("reference"), withLiabilityApplication, withSample(Sample(returnStatus = Some(SampleReturn.YES))))

      c.sampleToBeReturned shouldBe true
    }
  }

  private def rulingWithEffectiveEndDate(date: Option[Instant]): Decision = {
    Cases.decision.copy(effectiveEndDate = date)
  }

  private def caseWithRuling(d: Option[Decision]): Case = {
    Cases.btiCaseExample.copy(decision = d)
  }

  private def caseWithAppeal(appeal: Appeal): Case = {
    Cases.btiCaseExample.copy(decision = Some(Cases.decision.copy(appeal = Seq(appeal))))
  }
}
