/*
 * Copyright 2024 HM Revenue & Customs
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
package reporting

import java.time.Instant

sealed abstract class ReportField[A](val fieldName: String) extends Product with Serializable

case class NumberField(override val fieldName: String) extends ReportField[Long](fieldName)
case class StatusField(override val fieldName: String) extends ReportField[PseudoCaseStatus.Value](fieldName)
case class CaseTypeField(override val fieldName: String) extends ReportField[ApplicationType](fieldName)
case class ChapterField(override val fieldName: String) extends ReportField[String](fieldName)
case class DateField(override val fieldName: String) extends ReportField[Instant](fieldName)
case class StringField(override val fieldName: String) extends ReportField[String](fieldName)
case class DaysSinceField(override val fieldName: String) extends ReportField[Long](fieldName)
case class LiabilityStatusField(override val fieldName: String) extends ReportField[LiabilityStatus.Value](fieldName)

object ReportField {

  val Count: NumberField                    = NumberField("count")
  val Reference: StringField                = StringField("reference")
  val Status: StatusField                   = StatusField("status")
  val CaseType: CaseTypeField               = CaseTypeField("case_type")
  val CaseSource: StringField               = StringField("source")
  val Description: StringField              = StringField("description")
  val Chapter: ChapterField                 = ChapterField("chapter")
  val GoodsName: StringField                = StringField("goods_name")
  val TraderName: StringField               = StringField("trader_name")
  val User: StringField                     = StringField("assigned_user")
  val Team: StringField                     = StringField("assigned_team")
  val DateCreated: DateField                = DateField("date_created")
  private val DateCompleted                 = DateField("date_completed")
  val ElapsedDays: NumberField              = NumberField("elapsed_days")
  val TotalDays: DaysSinceField             = DaysSinceField("total_days")
  private val ReferredDays                  = NumberField("referred_days")
  val LiabilityStatus: LiabilityStatusField = LiabilityStatusField("liability_status")

  val fields: Map[String, ReportField[_]] = List(
    Count,
    Reference,
    Status,
    CaseType,
    CaseSource,
    Description,
    Chapter,
    GoodsName,
    TraderName,
    User,
    Team,
    DateCreated,
    DateCompleted,
    ElapsedDays,
    TotalDays,
    ReferredDays,
    LiabilityStatus
  ).map(field => field.fieldName -> field).toMap
}
