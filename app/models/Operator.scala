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

package models

import models.Role.Role

case class Operator(
  id: String,
  name: Option[String]         = None,
  email: Option[String]        = None,
  role: Role                   = Role.CLASSIFICATION_OFFICER,
  memberOfTeams: Seq[String]   = Seq.empty,
  managerOfTeams: Seq[String]  = Seq.empty, //being a member is the same as being the manager, don't use this
  permissions: Set[Permission] = Set.empty,
  deleted: Boolean             = false
) {

  def manager: Boolean = role == Role.CLASSIFICATION_MANAGER

  def safeName: String = name.getOrElse(s"PID $id")

  def hasPermissions(p: Set[Permission]): Boolean = p.subsetOf(permissions)

  def addPermissions(addedPermissions: Set[Permission]): Operator =
    this.copy(permissions = permissions ++ addedPermissions)

  def getMemberTeamNames: Seq[String] = memberOfTeams.flatMap(teamId => Queues.queueById(teamId).map(_.name))

  def isGateway: Boolean = memberOfTeams.contains(Queues.gateway.id)
}

object Role extends Enumeration {
  type Role = Value
  val CLASSIFICATION_OFFICER, CLASSIFICATION_MANAGER, READ_ONLY = Value

  def format(roleType: Role): String =
    roleType match {
      case CLASSIFICATION_OFFICER => "Classification officer"
      case CLASSIFICATION_MANAGER => "Manager"
      case READ_ONLY              => "Unknown"

    }
}
