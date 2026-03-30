/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.json.{Json, Reads, Writes}
import models.cache.CacheMap

case class UserAnswers(cacheMap: CacheMap) {
  def get[A](key: String)(implicit rds: Reads[A]): Option[A] =
    cacheMap.getEntry[A](key)

  def set[A](key: String, value: A)(implicit writes: Writes[A]): UserAnswers =
    UserAnswers(cacheMap.copy(data = cacheMap.data + (key -> Json.toJson(value))))

  def remove[A](key: String): UserAnswers =
    UserAnswers(cacheMap.copy(data = cacheMap.data - key))
}

object UserAnswers {
  def apply(cacheId: String): UserAnswers =
    UserAnswers(new CacheMap(cacheId, Map()))
}
