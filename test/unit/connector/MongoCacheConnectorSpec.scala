/*
 * Copyright 2023 HM Revenue & Customs
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
import repositories.SessionRepository
import scala.concurrent.Future
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global

class MongoCacheConnectorSpec
    extends ScalaCheckDrivenPropertyChecks
    with Generators
    with ConnectorTest
    with ScalaFutures
    with OptionValues {

  private trait Test {
    val mockSessionRepository: SessionRepository = mock[SessionRepository]
    val mongoCacheConnector: MongoCacheConnector = new MongoCacheConnector(mockSessionRepository, metrics)
  }

  ".save" should {
    "save the cache map to the Mongo repository" in new Test {
      when(mockSessionRepository.upsert(any[CacheMap])) thenReturn Future.successful(true)

      forAll(arbitrary[CacheMap]) { cacheMap =>
        val result = mongoCacheConnector.save(cacheMap)

        whenReady(result) { savedCacheMap =>
          savedCacheMap shouldBe cacheMap
          verify(mockSessionRepository).upsert(cacheMap)
        }
      }
    }
  }

  ".remove" should {
    "remove the cache map to the Mongo repository" in new Test {
      when(mockSessionRepository.remove(any[CacheMap])) thenReturn Future.successful(true)

      forAll(arbitrary[CacheMap]) { cacheMap =>
        val result = mongoCacheConnector.remove(cacheMap)

        whenReady(result) { savedCacheMap =>
          savedCacheMap shouldBe true
          verify(mockSessionRepository).remove(cacheMap)
        }
      }
    }
  }

  ".fetch" when {
    "there isn't a record for this key in Mongo" should {
      "return None" in new Test {
        when(mockSessionRepository.get(any[String])) thenReturn Future.successful(None)

        forAll(nonEmptyString) { cacheId =>
          val result = mongoCacheConnector.fetch(cacheId)

          whenReady(result)(optionalCacheMap => optionalCacheMap should be(empty))
        }
      }
    }

    "a record exists for this key" should {
      "return the record" in new Test {

        forAll(arbitrary[CacheMap]) { cacheMap =>
          when(mockSessionRepository.get(refEq(cacheMap.id))) thenReturn Future.successful(Some(cacheMap))

          val result = mongoCacheConnector.fetch(cacheMap.id)

          whenReady(result)(optionalCacheMap => optionalCacheMap.get shouldBe cacheMap)
        }
      }
    }
  }

  ".getEntry" when {
    "there isn't a record for this key in Mongo" should {
      "return None" in new Test {
        when(mockSessionRepository.get(any[String])) thenReturn Future.successful(None)

        forAll(nonEmptyString, nonEmptyString) { (cacheId, key) =>
          val result = mongoCacheConnector.getEntry[String](cacheId, key)

          whenReady(result)(optionalValue => optionalValue should be(empty))
        }
      }
    }

    "a record exists in Mongo but this key is not present" should {
      "return None" in new Test {

        private val gen = for {
          key      <- nonEmptyString
          cacheMap <- arbitrary[CacheMap]
        } yield (key, cacheMap copy (data = cacheMap.data - key))

        forAll(gen) {
          case (k, cacheMap) =>
            when(mockSessionRepository.get(refEq(cacheMap.id))) thenReturn Future.successful(Some(cacheMap))

            val result = mongoCacheConnector.getEntry[String](cacheMap.id, k)

            whenReady(result)(optionalValue => optionalValue should be(empty))
        }
      }
    }

    "a record exists in Mongo with this key" should {
      "return the key's value" in new Test {

        private val gen = for {
          key      <- nonEmptyString
          value    <- nonEmptyString
          cacheMap <- arbitrary[CacheMap]
        } yield (key, value, cacheMap copy (data = cacheMap.data + (key -> JsString(value))))

        forAll(gen) {
          case (k, v, cacheMap) =>
            when(mockSessionRepository.get(refEq(cacheMap.id))) thenReturn Future.successful(Some(cacheMap))

            val result = mongoCacheConnector.getEntry[String](cacheMap.id, k)

            whenReady(result)(optionalValue => optionalValue.get shouldBe v)
        }
      }
    }
  }
}
