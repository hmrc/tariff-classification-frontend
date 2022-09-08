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

package repositories

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.libs.json.JsString
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.MongoComponent

import scala.concurrent.ExecutionContext

class SessionRepositorySpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val emptyCacheMap: CacheMap = CacheMap("id1", Map.empty)
  private val cacheMap: CacheMap      = CacheMap("id2", Map("run" -> JsString("walk")))

  private val config            = app.injector.instanceOf[Configuration]
  private val mongoComponent    = app.injector.instanceOf[MongoComponent]
  private val sessionRepository = new SessionRepository(config, mongoComponent)

  "SessionRepository" when {
    "upsert" should {
      "return true to acknowledge an upsert of emptyCacheMap with no data" in {
        val result = sessionRepository.upsert(emptyCacheMap)

        await(result) mustBe true
      }

      "return true to acknowledge an upsert of cacheMap with data" in {
        val result = sessionRepository.upsert(cacheMap)

        await(result) mustBe true
      }
    }

    "get" should {
      "return the emptyCacheMap when id1 is found" in {
        val result = sessionRepository.get("id1")

        await(result) mustBe Some(emptyCacheMap)
        await(result).get.data mustBe Map.empty
      }

      "return the cacheMap with data when id2 is found" in {
        val result = sessionRepository.get("id2")

        await(result) mustBe Some(cacheMap)
        await(result).get.data mustBe Map("run" -> JsString("walk"))
      }
    }

    "remove" should {
      "return true to acknowledge a deletion of emptyCacheMap with no data" in {
        val result = sessionRepository.remove(emptyCacheMap)

        await(result) mustBe true
      }

      "return true to acknowledge a deletion of cacheMap with data" in {
        val result = sessionRepository.remove(cacheMap)

        await(result) mustBe true
      }
    }
  }
}
