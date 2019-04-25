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

package uk.gov.hmrc.tariffclassificationfrontend.views

import uk.gov.hmrc.tariffclassificationfrontend.models.{Queue, ReportResult}

class ReferralReport(results: Seq[ReportResult]) {

  lazy val count: Int = results.map(_.size).sum

  lazy val average: Int = Math.round(results.flatMap(_.value).sum.toDouble / count).toInt

  def countFor(queue: Queue): Int = results.find(_.group.contains(queue.id)).map(_.size).getOrElse(0)

  def averageFor(queue: Queue): Int = Math.round(results.find(_.group.contains(queue.id)).map(_.average).getOrElse(0.0)).toInt
}
