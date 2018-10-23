/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.service

import javax.inject.Singleton
import uk.gov.hmrc.tariffclassificationfrontend.models.Queue

@Singleton
class QueuesService {

  private val queues = Seq(
    Queue(1, "gateway", "Gateway"),
    Queue(2, "act", "ACT"),
    Queue(3, "cap", "CAP"),
    Queue(4, "cars", "Cars"),
    Queue(5, "elm", "ELM")
  )

  def getQueues: Seq[Queue] = {
    queues
  }

  def getOneBySlug(slug: String): Option[Queue] = {
    queues.find(_.slug == slug)
  }

  def getOneById(id: Int): Option[Queue] = {
    queues.find(_.id == id)
  }

}
