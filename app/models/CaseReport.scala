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

package models

import play.api.mvc.QueryStringBindable
import models.CaseReportField.CaseReportField
import models.CaseReportGroup.CaseReportGroup

case class CaseReport
(
  filter: CaseReportFilter,
  group: Set[CaseReportGroup],
  field: CaseReportField
)

object CaseReport {
  private val reportGroupKey = "report_group"
  private val reportFieldKey = "report_field"

  implicit def bindable(implicit stringBinder: QueryStringBindable[String],
                        filterBinder: QueryStringBindable[CaseReportFilter]
                       ): QueryStringBindable[CaseReport] = new QueryStringBindable[CaseReport] {
    override def bind(key: String, requestParams: Map[String, Seq[String]]): Option[Either[String, CaseReport]] = {
      import BinderUtil._
      implicit val rp: Map[String, Seq[String]] = requestParams

      val filter: CaseReportFilter = filterBinder.bind("", requestParams).get.right.get
      val group: Option[Set[CaseReportGroup]] = params(reportGroupKey).map(_.map(bindCaseReportGroup).filter(_.isDefined).map(_.get))
      val field: Option[CaseReportField] = param(reportFieldKey).flatMap(bindCaseReportField)

      (group, field) match {
        case (Some(g), Some(f)) => Some(Right(CaseReport(filter, g, f)))
        case (None, Some(_)) => Some(Left("Invalid Group"))
        case (Some(_), None) => Some(Left("Invalid Field"))
        case _ => Some(Left("Invalid Field/Group"))
      }
    }

    override def unbind(key: String, report: CaseReport): String = {
      Seq(
        filterBinder.unbind("", report.filter),
        stringBinder.unbind(reportGroupKey, report.group.mkString(",").toString),
        stringBinder.unbind(reportFieldKey, report.field.toString)
      ).filter(_.nonEmpty).mkString("&")
    }
  }
}
