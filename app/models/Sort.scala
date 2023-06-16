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

import models.SortDirection.SortDirection
import models.SortField.SortField
import play.api.mvc.QueryStringBindable

object SortDirection extends Enumeration {
  type SortDirection = Value
  val DESCENDING: models.SortDirection.Value = Value("desc")
  val ASCENDING: models.SortDirection.Value  = Value("asc")

  implicit val bindable: QueryStringBindable.Parsing[SortDirection] = new QueryStringBindable.Parsing[SortDirection](
    value => SortDirection.values.find(_.toString == value).getOrElse(throw new IllegalArgumentException),
    sort => sort.toString,
    (k: String, _: Exception) => s"Parameter [$k] is invalid"
  )
}

object SortField extends Enumeration {
  type SortField = Value
  val COMMODITY_CODE: models.SortField.Value = Value("commodity-code")

  implicit val bindable: QueryStringBindable.Parsing[SortField] = new QueryStringBindable.Parsing[SortField](
    value => SortField.values.find(_.toString == value).getOrElse(throw new IllegalArgumentException),
    sort => sort.toString,
    (k: String, _: Exception) => s"Parameter [$k] is invalid"
  )
}

case class Sort(
  direction: SortDirection = SortDirection.ASCENDING,
  field: SortField         = SortField.COMMODITY_CODE
)

object Sort {
  val sort_direction     = "sort_direction"
  private val sort_field = "sort_by"

  implicit def bindable: QueryStringBindable[Sort] = new QueryStringBindable[Sort] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Sort]] = {
      val direction: Option[SortDirection] =
        SortDirection.bindable.bind(sort_direction, params).filter(_.isRight).map(_.right.get)
      val field: Option[SortField] = SortField.bindable.bind(sort_field, params).filter(_.isRight).map(_.right.get)

      (direction, field) match {
        case (Some(d), Some(f)) => Some(Right(Sort(direction = d, field = f)))
        case (_, Some(f))       => Some(Right(Sort(field = f)))
        case (Some(d), _)       => Some(Right(Sort(direction = d)))
        case (_, _)             => Some(Right(Sort()))
      }
    }

    override def unbind(key: String, sort: Sort): String = {
      val bindings: Seq[String] = Seq(
        SortDirection.bindable.unbind(sort_direction, sort.direction),
        SortField.bindable.unbind(sort_field, sort.field)
      )

      bindings.mkString("&")
    }
  }
}
