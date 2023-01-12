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

package models

import java.time.Instant
import models.BinderUtil._
import play.api.mvc.QueryStringBindable

case class InstantRange(
  min: Instant,
  max: Instant
)

object InstantRange {
  val allTime = InstantRange(Instant.MIN, Instant.MAX)

  implicit def bindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[InstantRange] =
    new QueryStringBindable[InstantRange] {

      private def min(key: String) = s"min_$key"

      private def max(key: String) = s"max_$key"

      override def bind(key: String, requestParams: Map[String, Seq[String]]): Option[Either[String, InstantRange]] = {
        val minValue: Instant = param(min(key))(requestParams).flatMap(bindInstant).getOrElse(Instant.MIN)
        val maxValue: Instant = param(max(key))(requestParams).flatMap(bindInstant).getOrElse(Instant.MAX)
        val range             = InstantRange(minValue, maxValue)

        if (range == InstantRange.allTime) {
          None
        } else {
          Some(Right(range))
        }
      }

      override def unbind(key: String, filter: InstantRange): String = {
        val minFilter =
          if (filter.min == Instant.MIN) Seq.empty else Seq(stringBinder.unbind(min(key), filter.min.toString))
        val maxFilter =
          if (filter.max == Instant.MAX) Seq.empty else Seq(stringBinder.unbind(max(key), filter.max.toString))

        (minFilter ++ maxFilter).filterNot(_.isEmpty).mkString("&")
      }
    }
}
