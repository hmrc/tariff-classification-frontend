/*
 * Copyright 2021 HM Revenue & Customs
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

package models.viewmodels.managementtools
import models.{Queue, Queues}

case class User(
  PID: String,
  name: String,
  role: String,
  status: String,
  teams: List[Queue],
  numberOfCases: Int
) {
  def isAssigned: Boolean = teams.nonEmpty

}

//todo replace the dummy stub with queries
object Users {
  val user1 = User("1", "One", "Classification", "Active", Seq(Queues.act, Queues.cap).toList, 7)
  val user2 = User("2", "Two", "Classification", "Inactive", Seq(Queues.act).toList, 0)
  val user3 = User("3", "Three", "Classification", "Active", Seq(Queues.cap).toList, 1)
  val user4 = User("4", "Four", "Classification", "Active", Seq(Queues.car).toList, 1)
  val user5 = User("5", "Five", "Classification", "Active", List.empty, 0)
  val user6 = User("6", "Six", "Read only", "Active", List.empty, 0)
  val user7 = User("7", "Seven", "Gateway", "Active", Seq(Queues.gateway).toList, 1)
  val user8 = User("8", "Eight", "Manager", "Active", List.empty, 0)

  def allUsers: List[User] = List(user1, user2, user3, user4, user5, user6, user7, user8)

}
