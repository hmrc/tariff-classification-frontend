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

object Queues {
  val gateway = Queue("1", "gateway", "Gateway", "gateway")
  val act = Queue("2", "act", "ACT", "ACT")
  val cap = Queue("3", "cap", "CAP", "CAP")
  val cars = Queue("4", "cars", "Cars", "cars")
  val elm = Queue("5", "elm", "ELM", "ELM")
}
