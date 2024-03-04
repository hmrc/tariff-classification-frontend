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

object CaseStatus extends Enumeration {
  type CaseStatus = Value
  val DRAFT, NEW, OPEN, SUPPRESSED, REFERRED, REJECTED, CANCELLED, SUSPENDED, COMPLETED, REVOKED, ANNULLED = Value

  val openStatuses: Set[Value] = Set(OPEN, REFERRED, SUSPENDED)

  def formatCancellation(cse: Case): String = cse.status match {
    case CaseStatus.CANCELLED =>
      val cancellationCode = cse.decision
        .flatMap(_.cancellation)
        .flatMap(c => CancelReason.code(c.reason))
        .map(c => s" - $c")
        .getOrElse("")

      cse.status.toString + cancellationCode

    case _ =>
      cse.status.toString
  }
}
