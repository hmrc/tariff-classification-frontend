/*
 * Copyright 2024 HM Revenue & Customs
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

package generators

import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.{JsValue, Json}
import models.cache.CacheMap

trait CacheMapGenerator {
  self: Generators =>

  private val strGen: Int => Gen[String] = (n: Int) => Gen.listOfN(n, Gen.alphaChar).map(_.mkString)
  def genStringKey: String               = strGen(10).toString
  def genStringValue: JsValue            = Json.toJson(strGen(15).toString)

  implicit lazy val arbitraryCacheMap: Arbitrary[CacheMap] =
    Arbitrary {
      for {
        cacheId <- nonEmptyString
        data = Map(genStringKey -> genStringValue)
      } yield CacheMap(
        cacheId,
        data
      )
    }
}
