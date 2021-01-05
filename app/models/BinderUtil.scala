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

import java.time.Instant

import models.CaseReportField.CaseReportField
import models.CaseReportGroup.CaseReportGroup

import scala.util.Try

object BinderUtil {

  def bindCaseReportGroup(value: String): Option[CaseReportGroup] =
    CaseReportGroup.values.find(_.toString == value)

  def bindCaseReportField(value: String): Option[CaseReportField] =
    CaseReportField.values.find(_.toString == value)

  def bindInstant(value: String): Option[Instant] = Try(Instant.parse(value)).toOption

  def params(name: String)(implicit requestParams: Map[String, Seq[String]]): Option[Set[String]] =
    requestParams.get(name).map(_.flatMap(_.split(",")).toSet).filterNot(_.exists(_.isEmpty))

  def param(name: String)(implicit requestParams: Map[String, Seq[String]]): Option[String] =
    params(name).map(_.head)

}
