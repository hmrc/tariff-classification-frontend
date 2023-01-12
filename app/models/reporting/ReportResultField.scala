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
package reporting

import java.time.Instant

sealed abstract class ReportResultField[A](val fieldName: String, val data: Option[A]) extends Product with Serializable

case class NumberResultField(override val fieldName: String, override val data: Option[Long])
    extends ReportResultField[Long](fieldName, data)

case class StatusResultField(override val fieldName: String, override val data: Option[PseudoCaseStatus.Value])
    extends ReportResultField[PseudoCaseStatus.Value](fieldName, data)

case class CaseTypeResultField(override val fieldName: String, override val data: Option[ApplicationType])
    extends ReportResultField[ApplicationType](fieldName, data)

case class DateResultField(override val fieldName: String, override val data: Option[Instant])
    extends ReportResultField[Instant](fieldName, data)

case class StringResultField(override val fieldName: String, override val data: Option[String])
    extends ReportResultField[String](fieldName, data)

case class LiabilityStatusResultField(override val fieldName: String, override val data: Option[LiabilityStatus.Value])
    extends ReportResultField[LiabilityStatus.Value](fieldName, data)
