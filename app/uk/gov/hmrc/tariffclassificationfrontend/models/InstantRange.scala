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

package uk.gov.hmrc.tariffclassificationfrontend.models

import java.time.Instant

import play.api.mvc.QueryStringBindable

case class InstantRange
(
  min: Instant,
  max: Instant
)

object InstantRange {

  implicit def bindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[InstantRange] = new QueryStringBindable[InstantRange] {

    private def min(key: String) = s"min_$key"

    private def max(key: String) = s"max_$key"

    override def bind(key: String, requestParams: Map[String, Seq[String]]): Option[Either[String, InstantRange]] = {
      import BinderUtil._
      implicit val rp: Map[String, Seq[String]] = requestParams

      val minValue: Option[Instant] = param(min(key)).flatMap(bindInstant)
      val maxValue: Option[Instant] = param(max(key)).flatMap(bindInstant)

      (minValue, maxValue) match {
        case (Some(mn), Some(mx)) => Some(Right(InstantRange(mn, mx)))
        case (None, None) => None
        case _ => Some(Left(s"Params ${min(key)} and ${max(key)} are both required"))
      }
    }

    override def unbind(key: String, filter: InstantRange): String = {
      Seq(
        stringBinder.unbind(min(key), filter.min.toString),
        stringBinder.unbind(max(key), filter.max.toString)
      ).mkString("&")
    }
  }
}
