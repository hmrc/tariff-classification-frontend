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
import play.api.mvc.Result
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.Cases

import scala.concurrent.Future

class TabCacheControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val dataCacheConnector = mock[DataCacheConnector]
  private val operator = mock[Operator]
  private val cacheMap = mock[CacheMap]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(dataCacheConnector, operator, cacheMap)
  }

  def controller() = new TabCacheController(dataCacheConnector, FakeIdentifierAction(), new FakeDataRetrievalAction(None),
    new SuccessfulRequestActions(defaultPlayBodyParsers, operator), mcc)

  "GET" should {

    "retrieve anchor from storage get" in {
      val reference = Cases.aLiabilityCase().reference
      val itemType = Cases.aLiabilityCase().application.getType.toLowerCase

      when(dataCacheConnector.fetch(any())).thenReturn(Future.successful(Some(cacheMap)))
      when(dataCacheConnector.remove(any())).thenReturn(Future.successful(true))
      when(cacheMap.getEntry[String](reference + itemType)).thenReturn(Some(s"#${Tab.ATTACHMENTS_TAB}"))

      val result: Future[Result] = controller().get(reference, itemType).apply(
        newFakeGETRequestWithCSRF(app))

      await(bodyOf(result)) shouldBe s"#${Tab.ATTACHMENTS_TAB}"
    }

    "return default tab when there is no entry for the case and case is Liability" in {
      val reference = Cases.aLiabilityCase().reference
      val itemType = Cases.aLiabilityCase().application.getType.toLowerCase

      when(dataCacheConnector.fetch(any())).thenReturn(Future.successful(Some(cacheMap)))
      when(dataCacheConnector.remove(any())).thenReturn(Future.successful(true))
      when(cacheMap.getEntry[String](reference + itemType)).thenReturn(None)

      val result: Future[Result] = controller().get(reference, itemType).apply(
        newFakeGETRequestWithCSRF(app))

      await(bodyOf(result)) shouldBe s"#${Tab.C592_TAB}"
    }

    "return default tab when there is no entry for the case and case is Bti" in {
      val reference = Cases.aCase().reference
      val itemType = Cases.aCase().application.getType.toLowerCase


      when(dataCacheConnector.fetch(any())).thenReturn(Future.successful(Some(cacheMap)))
      when(dataCacheConnector.remove(any())).thenReturn(Future.successful(true))
      when(cacheMap.getEntry[String](reference + itemType)).thenReturn(None)

      val result: Future[Result] = controller().get(reference, itemType).apply(
        newFakeGETRequestWithCSRF(app))

      await(bodyOf(result)) shouldBe ""
    }

    "not retrieve anchor when no data are retrieved and itemType is Liability" in {
      val reference = Cases.aLiabilityCase().reference
      val itemType = Cases.aLiabilityCase().application.getType.toLowerCase

      when(dataCacheConnector.fetch(any())).thenReturn(Future.successful(None))

      val result: Future[Result] = controller().get(reference, itemType).apply(
        newFakeGETRequestWithCSRF(app))

      await(bodyOf(result)) shouldBe s"#${Tab.C592_TAB}"
    }

    "not retrieve anchor when no data are retrieved and itemType is BTI" in {
      val reference = Cases.aCase().reference
      val itemType = Cases.aCase().application.getType.toLowerCase
      when(dataCacheConnector.fetch(any())).thenReturn(Future.successful(None))

      val result: Future[Result] = controller().get(reference, itemType).apply(
        newFakeGETRequestWithCSRF(app))

      await(bodyOf(result)) shouldBe ""
    }

  }

  "POST" should {

    "save active tab in the database" in {
      val fakeReq = newFakePOSTRequestWithCSRF(app, s"#${Tab.C592_TAB}")
      val reference = Cases.aLiabilityCase().reference
      val itemType = Cases.aLiabilityCase().application.getType.toLowerCase
      when(dataCacheConnector.save(refEq[CacheMap](cacheMap))).thenReturn(Future.successful(cacheMap))

      await(controller().post(reference, itemType).apply(fakeReq))

      verify(dataCacheConnector).save(any[CacheMap]())
    }

    "not save active tab when anchor is empty" in {
      val reference = Cases.aLiabilityCase().reference
      val itemType = Cases.aLiabilityCase().application.getType.toLowerCase

      await(controller().post(reference, itemType).apply(newFakePOSTRequestWithCSRF(app, "")))

      verifyZeroInteractions(dataCacheConnector)
    }

    "not save active tab when tab name does not exist or # is missing" in {
      val reference = Cases.aLiabilityCase().reference
      val itemType = Cases.aLiabilityCase().application.getType.toLowerCase

      await(controller().post(reference, itemType).apply(newFakePOSTRequestWithCSRF(app, Tab.ATTACHMENTS_TAB)))

      verifyZeroInteractions(dataCacheConnector)
    }

    "not save active tab when body is null" in {
      val reference = Cases.aLiabilityCase().reference
      val itemType = Cases.aLiabilityCase().application.getType.toLowerCase

      await(controller().post(reference, itemType).apply(newFakePOSTRequestWithCSRF(app)))

      verifyZeroInteractions(dataCacheConnector)
    }
  }
}
