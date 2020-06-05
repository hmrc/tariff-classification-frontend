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

package connector

import generators.Generators
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.libs.json.JsString
import repositories.{ReactiveMongoRepository, SessionRepository}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class MongoCacheConnectorSpec extends ScalaCheckDrivenPropertyChecks
  with Generators with ConnectorTest with ScalaFutures with OptionValues {

  ".save" should {

    "save the cache map to the Mongo repository" in {

      val mockReactiveMongoRepository = mock[ReactiveMongoRepository]
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.apply()) thenReturn mockReactiveMongoRepository
      when(mockReactiveMongoRepository.upsert(any[CacheMap])) thenReturn Future.successful(true)

      val mongoCacheConnector = new MongoCacheConnector(mockSessionRepository)

      forAll(arbitrary[CacheMap]) { cacheMap =>

        val result = mongoCacheConnector.save(cacheMap)

        whenReady(result) { savedCacheMap =>
          savedCacheMap shouldBe cacheMap
          verify(mockReactiveMongoRepository).upsert(cacheMap)
        }

      }

    }

  }

  ".remove" should {

    "remove the cache map to the Mongo repository" in {

      val mockReactiveMongoRepository = mock[ReactiveMongoRepository]
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.apply()) thenReturn mockReactiveMongoRepository
      when(mockReactiveMongoRepository.remove(any[CacheMap])) thenReturn Future.successful(true)

      val mongoCacheConnector = new MongoCacheConnector(mockSessionRepository)

      forAll(arbitrary[CacheMap]) { cacheMap =>

        val result = mongoCacheConnector.remove(cacheMap)

        whenReady(result) { savedCacheMap =>
          savedCacheMap shouldBe true
          verify(mockReactiveMongoRepository).remove(cacheMap)
        }
      }

    }

  }

  ".fetch" when {

    "there isn't a record for this key in Mongo" should {

      "return None" in {

        val mockReactiveMongoRepository = mock[ReactiveMongoRepository]
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.apply()) thenReturn mockReactiveMongoRepository
        when(mockReactiveMongoRepository.get(any())) thenReturn Future.successful(None)

        val mongoCacheConnector = new MongoCacheConnector(mockSessionRepository)

        forAll(nonEmptyString) { cacheId =>

          val result = mongoCacheConnector.fetch(cacheId)

          whenReady(result) { optionalCacheMap =>
            optionalCacheMap should be(empty)
          }
        }

      }

    }

    "a record exists for this key" should {

      "return the record" in {

        val mockReactiveMongoRepository = mock[ReactiveMongoRepository]
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.apply()) thenReturn mockReactiveMongoRepository

        val mongoCacheConnector = new MongoCacheConnector(mockSessionRepository)

        forAll(arbitrary[CacheMap]) { cacheMap =>

          when(mockReactiveMongoRepository.get(refEq(cacheMap.id))) thenReturn Future.successful(Some(cacheMap))

          val result = mongoCacheConnector.fetch(cacheMap.id)

          whenReady(result) { optionalCacheMap =>
              optionalCacheMap.get shouldBe cacheMap
          }
        }

      }

    }

  }

  ".getEntry" when {

    "there isn't a record for this key in Mongo" should {

      "return None" in {

        val mockReactiveMongoRepository = mock[ReactiveMongoRepository]
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.apply()) thenReturn mockReactiveMongoRepository
        when(mockReactiveMongoRepository.get(any())) thenReturn Future.successful(None)

        val mongoCacheConnector = new MongoCacheConnector(mockSessionRepository)

        forAll(nonEmptyString, nonEmptyString) { (cacheId, key) =>

            val result = mongoCacheConnector.getEntry[String](cacheId, key)

            whenReady(result) { optionalValue =>
              optionalValue should be(empty)
            }
        }

      }

    }

    "a record exists in Mongo but this key is not present" should {

      "return None" in {

        val mockReactiveMongoRepository = mock[ReactiveMongoRepository]
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.apply()) thenReturn mockReactiveMongoRepository

        val mongoCacheConnector = new MongoCacheConnector(mockSessionRepository)

        val gen = for {
          key      <- nonEmptyString
          cacheMap <- arbitrary[CacheMap]
        } yield (key, cacheMap copy (data = cacheMap.data - key))

        forAll(gen) { case (k, cacheMap) =>

          when(mockReactiveMongoRepository.get(refEq(cacheMap.id))) thenReturn Future.successful(Some(cacheMap))

          val result = mongoCacheConnector.getEntry[String](cacheMap.id, k)

          whenReady(result) { optionalValue =>
            optionalValue should be(empty)
          }
        }

      }

    }

    "a record exists in Mongo with this key" should {

      "return the key's value" in {

        val mockReactiveMongoRepository = mock[ReactiveMongoRepository]
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.apply()) thenReturn mockReactiveMongoRepository

        val mongoCacheConnector = new MongoCacheConnector(mockSessionRepository)

        val gen = for {
          key      <- nonEmptyString
          value    <- nonEmptyString
          cacheMap <- arbitrary[CacheMap]
        } yield (key, value, cacheMap copy (data = cacheMap.data + (key -> JsString(value))))

        forAll(gen) { case (k, v, cacheMap) =>
          when(mockReactiveMongoRepository.get(refEq(cacheMap.id))) thenReturn Future.successful(Some(cacheMap))

          val result = mongoCacheConnector.getEntry[String](cacheMap.id, k)

          whenReady(result) { optionalValue =>
            optionalValue.get shouldBe v
          }
        }

      }

    }

  }

}
