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

  def getAll: Seq[Queue] = Queues.all

  def getNonGateway: Seq[Queue] = Queues.non_gateway

  def getGateway: Queue = Queues.gateway

  def getOneBySlug(slug: String): Option[Queue] = {
    Queues.all.find(_.slug == slug)
  }

  def getOneById(id: String): Option[Queue] = {
    Queues.all.find(_.id == id)
  }

}

private object Queues {
  val gateway = Queue("1", "gateway", "Gateway")
  val act = Queue("2", "act", "ACT")
  val cap = Queue("3", "cap", "CAP")
  val cars = Queue("4", "cars", "Cars")
  val elm = Queue("5", "elm", "ELM")

  def all: Seq[Queue] = Seq(gateway, act, cap, cars, elm)
  def non_gateway: Seq[Queue] = Seq(act, cap, cars, elm)
}