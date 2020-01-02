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

import play.api.mvc.QueryStringBindable

case class CaseReportFilter
(
  decisionStartDate: Option[InstantRange] = None,
  referralDate: Option[InstantRange] = None,
  status: Option[Set[String]] = None,
  applicationType: Option[Set[String]] = None,
  assigneeId: Option[String] = None
)

object CaseReportFilter {

  val decisionStartKey = "decision_start"
  val referralDateKey = "referral_date"
  val statusKey = "status"
  val applicationTypeKey = "application_type"
  val assigneeIdKey = "assignee_id"

  implicit def binder(implicit rangeBinder: QueryStringBindable[InstantRange], stringBinder: QueryStringBindable[String]): QueryStringBindable[CaseReportFilter] = new QueryStringBindable[CaseReportFilter] {

    override def bind(key: String, requestParams: Map[String, Seq[String]]): Option[Either[String, CaseReportFilter]] = {
      implicit val rp: Map[String, Seq[String]] = requestParams

      val decisionStart: Option[InstantRange] = rangeBinder.bind(decisionStartKey, requestParams).filter(_.isRight).map(_.right.get)
      val referralDate: Option[InstantRange] = rangeBinder.bind(referralDateKey, requestParams).filter(_.isRight).map(_.right.get)
      val status: Option[Set[String]] = params(statusKey)
      val applicationType: Option[Set[String]] = params(applicationTypeKey)
      val assigneeId: Option[String] = param(assigneeIdKey)

      Some(
        Right(
          CaseReportFilter(
            decisionStart,
            referralDate,
            status,
            applicationType,
            assigneeId
          )
        )
      )
    }

    override def unbind(key: String, filter: CaseReportFilter): String = {
      Seq(
        filter.decisionStartDate.map(r => rangeBinder.unbind(decisionStartKey, r)),
        filter.referralDate.map(r => rangeBinder.unbind(referralDateKey, r)),
        filter.status.map(_.map(r => stringBinder.unbind(statusKey, r)).mkString("&")),
        filter.applicationType.map(_.map(r => stringBinder.unbind(applicationTypeKey, r)).mkString("&")),
        filter.assigneeId.map(r => stringBinder.unbind(assigneeIdKey, r))
      ).filter(_.isDefined).map(_.get).mkString("&")

    }

    def params(name: String)(implicit requestParams: Map[String, Seq[String]]): Option[Set[String]] = {
      requestParams.get(name).map(_.flatMap(_.split(",")).toSet).filterNot(_.exists(_.isEmpty))
    }

    def param(name: String)(implicit requestParams: Map[String, Seq[String]]): Option[String] = {
      params(name).map(_.head)
    }
  }
}