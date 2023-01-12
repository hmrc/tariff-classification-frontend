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

import models.AppealStatus.AppealStatus
import models.AppealType.AppealType

case class Appeal(
  id: String,
  status: AppealStatus,
  `type`: AppealType
)

object Appeal {

  def highestAppealFromDecision(decision: Option[Decision]): Option[Appeal] = {
    val appeals = decision.map(_.appeal).getOrElse(Seq.empty)

    if (appeals.nonEmpty) {
      Some(appeals.maxBy(_.`type`.id))
    } else {
      None
    }
  }
}
