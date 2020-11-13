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

package controllers

import connector.DataCacheConnector
import controllers.actions.{FakeDataRetrievalAction, FakeIdentifierAction}
import models.Operator
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import service.TabCacheService
import models.ApplicationType
import connector.FakeDataCacheConnector

class TabCacheControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val dataCacheConnector = FakeDataCacheConnector
  private val tabCacheService = new TabCacheService(dataCacheConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(dataCacheConnector.remove(CacheMap("id", Map.empty)))
  }

  def controller() =
    new TabCacheController(
      tabCacheService,
      FakeIdentifierAction(),
      mcc
    )

  "POST" should {
    "save active tab in the database" in {
      val result = controller().post(ApplicationType.LIABILITY_ORDER).apply(newFakePOSTRequestWithCSRF(app, Tab.C592_TAB.name))
      status(result) shouldBe Status.ACCEPTED
      await(tabCacheService.getActiveTab("id", ApplicationType.LIABILITY_ORDER)) shouldBe Some(Tab.C592_TAB)
    }

    "not save active tab when anchor is empty" in {
      val result = controller().post(ApplicationType.LIABILITY_ORDER).apply(newFakePOSTRequestWithCSRF(app, ""))
      status(result) shouldBe Status.BAD_REQUEST
      await(tabCacheService.getActiveTab("id", ApplicationType.LIABILITY_ORDER)) shouldBe None
    }

    "not save active tab when tab name does not exist" in {
      val result = controller().post(ApplicationType.LIABILITY_ORDER).apply(newFakePOSTRequestWithCSRF(app, "foo"))
      status(result) shouldBe Status.BAD_REQUEST
      await(tabCacheService.getActiveTab("id", ApplicationType.LIABILITY_ORDER)) shouldBe None
    }

    "not save active tab when body is null" in {
      val result = controller().post(ApplicationType.LIABILITY_ORDER).apply(newFakePOSTRequestWithCSRF(app))
      status(result) shouldBe Status.BAD_REQUEST
      await(tabCacheService.getActiveTab("id", ApplicationType.LIABILITY_ORDER)) shouldBe None
    }
  }
}
