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

package controllers

import connector.FakeDataCacheConnector
import controllers.actions.{FakeDataRetrievalAction, FakeIdentifierAction}
import models.{ApplicationType, Operator}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import service.TabCacheService
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global

class TabCacheControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val operator           = Operator(id = "id")
  private val dataCacheConnector = FakeDataCacheConnector
  private val tabCacheService    = new TabCacheService(dataCacheConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(dataCacheConnector.remove(CacheMap("id", Map.empty)))
  }

  def controller() =
    new TabCacheController(
      tabCacheService,
      FakeIdentifierAction(),
      new FakeDataRetrievalAction(None),
      new SuccessfulRequestActions(playBodyParsers, operator),
      mcc
    )

  "POST" should {
    "save active tab in the database" in {
      val result = controller()
        .post("caseRef", ApplicationType.LIABILITY)
        .apply(newFakePOSTRequestWithCSRF(app, Tab.C592_TAB.name))
      status(result)                                                                  shouldBe Status.ACCEPTED
      await(tabCacheService.getActiveTab("id", "caseRef", ApplicationType.LIABILITY)) shouldBe Some(Tab.C592_TAB)
    }

    "not save active tab when anchor is empty" in {
      val result = controller().post("caseRef", ApplicationType.LIABILITY).apply(newFakePOSTRequestWithCSRF(app, ""))
      status(result)                                                                  shouldBe Status.BAD_REQUEST
      await(tabCacheService.getActiveTab("id", "caseRef", ApplicationType.LIABILITY)) shouldBe None
    }

    "not save active tab when tab name does not exist" in {
      val result = controller().post("caseRef", ApplicationType.LIABILITY).apply(newFakePOSTRequestWithCSRF(app, "foo"))
      status(result)                                                                  shouldBe Status.BAD_REQUEST
      await(tabCacheService.getActiveTab("id", "caseRef", ApplicationType.LIABILITY)) shouldBe None
    }

    "not save active tab when body is null" in {
      val result = controller().post("caseRef", ApplicationType.LIABILITY).apply(newFakePOSTRequestWithCSRF(app))
      status(result)                                                                  shouldBe Status.BAD_REQUEST
      await(tabCacheService.getActiveTab("id", "caseRef", ApplicationType.LIABILITY)) shouldBe None
    }
  }

  "GET" should {
    "retrieve active tab from the database" in {
      await(
        controller().post("caseRef", ApplicationType.ATAR).apply(newFakePOSTRequestWithCSRF(app, Tab.ACTIVITY_TAB.name))
      )
      val result = controller().get("caseRef", ApplicationType.ATAR).apply(newFakeGETRequestWithCSRF(app))
      status(result)          shouldBe Status.OK
      contentAsString(result) shouldBe Tab.ACTIVITY_TAB.name
    }

    "clear active tab after it is fetched" in {
      await(
        controller().post("caseRef", ApplicationType.ATAR).apply(newFakePOSTRequestWithCSRF(app, Tab.KEYWORDS_TAB.name))
      )
      val afterSet = await(controller().get("caseRef", ApplicationType.ATAR).apply(newFakeGETRequestWithCSRF(app)))
      status(afterSet)          shouldBe Status.OK
      contentAsString(afterSet) shouldBe Tab.KEYWORDS_TAB.name
      val afterGet = await(controller().get("caseRef", ApplicationType.ATAR).apply(newFakeGETRequestWithCSRF(app)))
      status(afterGet)          shouldBe Status.OK
      contentAsString(afterGet) shouldBe ""
    }
  }
}
