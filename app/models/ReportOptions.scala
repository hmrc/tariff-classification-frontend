/*
 * Copyright 2021 HM Revenue & Customs
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

import models.PseudoGroupingType.PseudoGroupingType
import models.PseudoReportColumns.PseudoReportColumns
import play.api.mvc.PathBindable
import cats.syntax.either._

sealed trait ReportOptions {
  val selectedDateRange: ReportDates
  val `type`: ReportOptionsType

}

case class ReportGroupingOptions(selectedDateRange: ReportDates, grouping: PseudoGroupingType) extends ReportOptions {
  override val `type`: ReportOptionsType = ReportOptionsType.GROUPING_OPTIONS
}
case class ReportColumnsOptions(selectedDateRange: ReportDates, columns: Option[Set[PseudoReportColumns]])
    extends ReportOptions {
  override val `type`: ReportOptionsType = ReportOptionsType.COLUMNS_OPTIONS
}

sealed abstract class ReportOptionsType(val name: String) extends Product with Serializable {
  def prettyName: String = this match {
    case ReportOptionsType.GROUPING_OPTIONS => "Grouping options"
    case ReportOptionsType.COLUMNS_OPTIONS  => "Columns options"
  }
}

object ReportOptionsType {
  val values = Set(GROUPING_OPTIONS, COLUMNS_OPTIONS)

  def withName(name: String) = values.find(_.name.equalsIgnoreCase(name)).getOrElse(throw new NoSuchElementException)

  case object GROUPING_OPTIONS extends ReportOptionsType("GROUPING_OPTIONS")
  case object COLUMNS_OPTIONS extends ReportOptionsType("COLUMNS_OPTIONS")

  implicit def reportOptionsTypePathBindable(
    implicit stringBindable: PathBindable[String]
  ): PathBindable[ReportOptionsType] =
    new PathBindable[ReportOptionsType] {
      def bind(key: String, value: String): Either[String, ReportOptionsType] =
        Either
          .catchOnly[NoSuchElementException] {
            ReportOptionsType.withName(value)
          }
          .leftMap(_ => "Invalid report dates type")
      def unbind(key: String, value: ReportOptionsType): String =
        stringBindable.unbind(key, value.name)
    }
}
