/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.mvc.QueryStringBindable

case class CaseReportFilter
(
  decisionStartDate: Option[InstantRange] = None
)

object CaseReportFilter {

  val decisionStartKey = "decision_start"

  implicit def binder(implicit rangeBinder: QueryStringBindable[InstantRange]): QueryStringBindable[CaseReportFilter] = new QueryStringBindable[CaseReportFilter] {

    override def bind(key: String, requestParams: Map[String, Seq[String]]): Option[Either[String, CaseReportFilter]] = {
      implicit val rp: Map[String, Seq[String]] = requestParams

      val decisionStart: Option[InstantRange] = rangeBinder.bind(decisionStartKey, requestParams).filter(_.isRight).map(_.right.get)

      Some(
        Right(
          CaseReportFilter(
            decisionStart
          )
        )
      )
    }

    override def unbind(key: String, filter: CaseReportFilter): String = {
      filter.decisionStartDate.map(r => rangeBinder.unbind(decisionStartKey, r))
        .getOrElse("")
    }
  }
}

