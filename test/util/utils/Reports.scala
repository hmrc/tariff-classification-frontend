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

package utils

import models._
import models.reporting._

object Reports {
  val queueReportResults: Seq[QueueResultGroup] = Seq(
    QueueResultGroup(7, None, ApplicationType.ATAR),
    QueueResultGroup(3, None, ApplicationType.LIABILITY),
    QueueResultGroup(4, Some("2"), ApplicationType.ATAR),
    QueueResultGroup(6, Some("2"), ApplicationType.LIABILITY),
    QueueResultGroup(2, Some("3"), ApplicationType.ATAR),
    QueueResultGroup(1, Some("4"), ApplicationType.LIABILITY)
  )

  val pagedQueueReport: Paged[QueueResultGroup] = Paged(queueReportResults)
}
