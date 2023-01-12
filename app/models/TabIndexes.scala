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

import views.CaseDetailPage.{CaseDetailPage, _}

object TabIndexes {

  private val tabIndexIncrement = 1000;

  private val pageTabIndexer: ReusableTabIndexer =
    ReusableTabIndexer(startTabIndex = tabIndexIncrement, indexIncrement = tabIndexIncrement)

  private val indexByPage: Map[CaseDetailPage, Int] = Map(
    TRADER              -> pageTabIndexer.nextTabIndex(),
    APPLICATION_DETAILS -> pageTabIndexer.currentTabIndex(),
    SAMPLE_DETAILS      -> pageTabIndexer.nextTabIndex(),
    ATTACHMENTS         -> pageTabIndexer.nextTabIndex(),
    ACTIVITY            -> pageTabIndexer.nextTabIndex(),
    KEYWORDS            -> pageTabIndexer.nextTabIndex(),
    RULING              -> pageTabIndexer.nextTabIndex(),
    APPEAL              -> pageTabIndexer.nextTabIndex()
  )

  def tabIndexFor: CaseDetailPage => Int = { page => indexByPage.getOrElse(page, 0) }

  private val queueTabIndexer: ReusableTabIndexer =
    ReusableTabIndexer(startTabIndex = tabIndexIncrement, indexIncrement = tabIndexIncrement)

  private val fixedQueues: Map[String, Int] =
    Map("my-cases" -> queueTabIndexer.nextTabIndex(), Queues.gateway.slug -> queueTabIndexer.nextTabIndex())

  private val dynamicQueues: Map[String, Int] =
    Queues.allDynamicQueues.map(q => q.slug                   -> queueTabIndexer.nextTabIndex()).toMap ++
      Queues.allDynamicQueues.map(q => s"${q.slug}-liability" -> queueTabIndexer.nextTabIndex()).toMap

  private val reportingQueues: Map[String, Int] =
    Map("assigned-cases" -> queueTabIndexer.nextTabIndex(), "reports" -> queueTabIndexer.nextTabIndex())

  private val indexByQueues: Map[String, Int] = fixedQueues ++ dynamicQueues ++ reportingQueues

  def tabIndexForQueue: String => Int = { page => indexByQueues.getOrElse(page, 0) }

  def tabIndexForQueue(slug: String, filteredBy: String): Int =
    filteredBy match {
      case "LIABILITY_ORDER" => indexByQueues.getOrElse(s"$slug-liability", 0)
      case _                 => indexByQueues.getOrElse(slug, 0)
    }

}
