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

package models

object Queues {
  val gateway = Queue("1", "gateway", "Gateway")
  val act     = Queue("2", "act", "ACT")
  val cap     = Queue("3", "cap", "CAP")
  val car     = Queue("4", "car", "CAR")
  val elm     = Queue("5", "elm", "ELM")
  val flex    = Queue("6", "flex", "FLEX")
  val tta     = Queue("7", "tta", "TT-A")
  val ttb     = Queue("8", "ttb", "TT-B")
  val ttc     = Queue("9", "ttc", "TT-C")

  def allAtarQueues: List[Queue]       = List(act, car, elm, flex, tta, ttb, ttc)
  def allLiabilityQueues: List[Queue]  = List(act, cap, elm, flex, tta, ttb, ttc)
  def allCorresMiscQueues: List[Queue] = List(act, elm, flex, tta, ttb, ttc)

  def allDynamicQueues: List[Queue] = List(act, cap, car, elm, flex, tta, ttb, ttc)

  def allQueues: List[Queue] = List(gateway, act, cap, car, elm, flex, tta, ttb, ttc)

  def allQueuesById: Map[String, Queue]   = allQueues.map(q => q.id   -> q).toMap
  def allQueuesBySlug: Map[String, Queue] = allQueues.map(q => q.slug -> q).toMap

  private val queuesByType = Map(
    ApplicationType.ATAR           -> Queues.allAtarQueues,
    ApplicationType.LIABILITY      -> Queues.allLiabilityQueues,
    ApplicationType.CORRESPONDENCE -> Queues.allCorresMiscQueues,
    ApplicationType.MISCELLANEOUS  -> Queues.allCorresMiscQueues
  )

  def queuesForType(applicationType: ApplicationType): List[Queue] =
    queuesByType.get(applicationType).getOrElse(List.empty)
}
