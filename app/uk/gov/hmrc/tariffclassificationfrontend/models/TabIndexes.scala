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

import uk.gov.hmrc.tariffclassificationfrontend.views.CaseDetailPage.{CaseDetailPage, _}

object TabIndexes {

  private val indexByPage: Map[CaseDetailPage, Int] = Map(TRADER -> 1000, LIABILITY -> 2000 , APPLICATION_DETAILS -> 2000, SAMPLE_DETAILS -> 3000,
    ATTACHMENTS -> 4000, ACTIVITY -> 5000, KEYWORDS -> 6000, RULING -> 7000, APPEAL -> 8000)

  def tabIndexFor: CaseDetailPage => Int = { page => indexByPage.getOrElse(page, 0) }

  private val indexByQueues: Map[String, Int] = Map("my-cases" -> 1000, Queues.gateway.slug -> 2000 , Queues.act.slug -> 3000,  Queues.cap.slug -> 4000,
    Queues.cars.slug -> 5000,  Queues.elm.slug -> 6000, s"${Queues.gateway.slug}-liab" -> 7000 , s"${Queues.act.slug}-liab" -> 8000,  s"${Queues.cap.slug}-liab" -> 9000,
      s"${Queues.cars.slug}-liab" -> 10000,  s"${Queues.elm.slug}-liab" -> 11000, "assigned-cases" -> 12000, "reports" -> 13000)

  def tabIndexForQueue: String => Int = { page => indexByQueues.getOrElse(page, 0) }

}
