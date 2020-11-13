/*
 * Copyright 2020 HM Revenue & Customs
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

import connector.FakeDataCacheConnector
import scala.concurrent.ExecutionContext.Implicits.global
import controllers.Tab
import models.ApplicationType
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import uk.gov.hmrc.http.cache.client.CacheMap

class TabCacheServiceSpec extends ServiceSpecBase with ScalaCheckDrivenPropertyChecks {

  val cacheConnector = FakeDataCacheConnector
  val service = new TabCacheService(cacheConnector)

  val tabGenerator = Gen.oneOf(List(
    Tab.ACTIVITY_TAB,
    Tab.ATTACHMENTS_TAB,
    Tab.C592_TAB,
    Tab.KEYWORDS_TAB,
    Tab.RULING_TAB,
    Tab.SAMPLE_TAB
  ))

  override protected def beforeEach(): Unit = {
    await(cacheConnector.remove(CacheMap("id", Map.empty)))
  }

  "TabCacheService" should {
    "get the active tab" in {
      await(service.getActiveTab("id", ApplicationType.LIABILITY_ORDER)) shouldBe None
    }

    "clear the active tab" in {
      await(service.clearActiveTab("id", ApplicationType.LIABILITY_ORDER)) shouldBe (())
    }

    "set the active tab" in {
      await(service.setActiveTab("id", ApplicationType.LIABILITY_ORDER, Tab.ATTACHMENTS_TAB)) shouldBe (())
    }

    "get back out the tab that you put in" in forAll(tabGenerator, tabGenerator) { (firstTab, secondTab) =>
      await(for {
        _              <- service.setActiveTab("id", ApplicationType.LIABILITY_ORDER, firstTab)
        afterFirstSet  <- service.getActiveTab("id", ApplicationType.LIABILITY_ORDER)
        _              <- service.setActiveTab("id", ApplicationType.LIABILITY_ORDER, secondTab)
        afterSecondSet <- service.getActiveTab("id", ApplicationType.LIABILITY_ORDER)
      } yield (afterFirstSet, afterSecondSet)) shouldBe ((Some(firstTab), Some(secondTab)))
    }

    "delete the tab that was saved" in forAll(tabGenerator) { tab =>
      await(for {
        _          <- service.setActiveTab("id", ApplicationType.LIABILITY_ORDER, tab)
        afterSet   <- service.getActiveTab("id", ApplicationType.LIABILITY_ORDER)
        _          <- service.clearActiveTab("id", ApplicationType.LIABILITY_ORDER)
        afterClear <- service.getActiveTab("id", ApplicationType.LIABILITY_ORDER)
      } yield (afterSet, afterClear)) shouldBe ((Some(tab), None))
    }
  }
}
