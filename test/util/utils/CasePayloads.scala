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

package utils

import play.api.libs.json.Json
import models._
import utils.JsonFormatters.{caseFormat, reportResult}
import play.api.libs.json.Writes

object CasePayloads {

  val btiCase: String             = jsonOf(Cases.btiCaseExample)
  val simpleBtiCase: String       = jsonOf(Cases.simpleCaseExample)
  val simpleLiabilityCase: String = jsonOf(Cases.liabilityCaseExample)
  val gatewayCases: String        = jsonOf(Seq(Cases.btiCaseExample))
  val pagedGatewayCases: String   = jsonOf(Paged(Seq(Cases.btiCaseExample), NoPagination(), 1))
  val pagedAssignedCases: String  = jsonOf(Paged(Seq(Cases.caseAssignedExample), NoPagination(), 1))
  val report: String              = jsonOfReport(Seq(ReportResult(Map(CaseReportGroup.QUEUE -> Some("test-report")), Seq(1, 2))))
  val reportEmpty: String         = jsonOfReport(Seq.empty)
  val pagedEmpty: String          = jsonOf(Paged.empty[Case])

  def jsonOfReport(obj: Seq[ReportResult]): String =
    Json.toJson(obj).toString()

  def jsonOf[A: Writes](obj: A): String =
    Json.toJson(obj).toString()
}
