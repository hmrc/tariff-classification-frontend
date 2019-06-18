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

trait Permission {
  def name: String
}

trait GlobalPermission extends Permission {
  def appliesTo(operator: Operator): Boolean
}

trait CasePermission extends Permission {
  def appliesTo(`case`: Case, operator: Operator): Boolean
}

object Permission {
  private lazy val values: Set[Permission] = Set(
    VIEW_MY_CASES,
    VIEW_QUEUE_CASES,
    VIEW_ASSIGNED_CASES,
    SEARCH,
    ADVANCED_SEARCH,
    VIEW_CASES,
    VIEW_CASE_ASSIGNEE,
    VIEW_REPORTS,
    CREATE_CASES,
    ASSIGN_CASE,
    RELEASE_CASE,
    SUPPRESS_CASE,
    REFER_CASE,
    REOPEN_CASE,
    REJECT_CASE,
    SUSPEND_CASE,
    COMPLETE_CASE,
    REMOVE_ATTACHMENTS,
    CANCEL_CASE,
    ADD_NOTE,
    ADD_ATTACHMENT,
    KEYWORDS,
    EDIT_LIABILITY,
    EDIT_RULING,
    APPEAL_CASE,
    EXTENDED_USE,
    MOVE_CASE_BACK_TO_QUEUE,
    MANAGE_KEYWORDS,
    MANAGE_QUEUES,
    EDIT_SAMPLE
  )

  def from(string: String): Option[Permission] = values.find(_.name == string)
  def applyingTo(operator: Operator): Set[Permission] = values
    .filter(_.isInstanceOf[GlobalPermission])
    .map(_.asInstanceOf[GlobalPermission])
    .filter(_.appliesTo(operator))
    .map(_.asInstanceOf[Permission])
  def applyingTo(`case`: Case, operator: Operator): Set[Permission] = values
    .filter(_.isInstanceOf[CasePermission])
    .map(_.asInstanceOf[CasePermission])
    .filter(_.appliesTo(`case`, operator))
    .map(_.asInstanceOf[Permission])

  private def anyone(): Boolean = true

  private def managersOrAssignedTeamMembersOnly(`case`: Case, operator: Operator): Boolean = operator.role match {
    case Role.CLASSIFICATION_MANAGER => true
    case Role.CLASSIFICATION_OFFICER if `case`.isAssignedTo(operator) => true
    case _ => false
  }

  private def managersOrTeamMembersOnly(operator: Operator): Boolean = operator.role match {
    case Role.CLASSIFICATION_MANAGER | Role.CLASSIFICATION_OFFICER => true
    case _ => false
  }

  private def managersOnly(operator: Operator): Boolean = operator.role == Role.CLASSIFICATION_MANAGER

  private def adminOnly(operator: Operator): Boolean = false

  // **************************************** Permissions **************************************************************

  case object VIEW_MY_CASES extends GlobalPermission {
    override def name: String = this.getClass.getName
    override def appliesTo(operator: Operator): Boolean = managersOrTeamMembersOnly(operator)
  }

  case object VIEW_QUEUE_CASES extends GlobalPermission {
    override def name: String = this.getClass.getName
    override def appliesTo(operator: Operator): Boolean = managersOrTeamMembersOnly(operator)
  }

  case object VIEW_ASSIGNED_CASES extends GlobalPermission {
    override def name: String = this.getClass.getName
    override def appliesTo(operator: Operator): Boolean = managersOnly(operator)
  }

  case object SEARCH extends GlobalPermission {
    override def name: String = this.getClass.getName
    override def appliesTo(operator: Operator): Boolean = anyone()
  }

  case object ADVANCED_SEARCH extends GlobalPermission {
    override def name: String = this.getClass.getName
    override def appliesTo(operator: Operator): Boolean = anyone()
  }

  case object VIEW_CASES extends GlobalPermission {
    override def name: String = this.getClass.getName
    override def appliesTo(operator: Operator): Boolean = anyone()
  }

  case object VIEW_CASE_ASSIGNEE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrTeamMembersOnly(operator)
  }

  case object VIEW_REPORTS extends GlobalPermission {
    override def name: String = this.getClass.getName
    override def appliesTo(operator: Operator): Boolean = managersOnly(operator)
  }

  case object CREATE_CASES extends GlobalPermission {
    override def name: String = this.getClass.getName
    override def appliesTo(operator: Operator): Boolean = managersOrTeamMembersOnly(operator)
  }

  case object ASSIGN_CASE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrTeamMembersOnly(operator)
  }

  case object RELEASE_CASE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrTeamMembersOnly(operator)
  }

  case object SUPPRESS_CASE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrAssignedTeamMembersOnly(`case`, operator)
  }

  case object REFER_CASE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrAssignedTeamMembersOnly(`case`, operator)
  }

  case object REOPEN_CASE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrAssignedTeamMembersOnly(`case`, operator)
  }

  case object REJECT_CASE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrAssignedTeamMembersOnly(`case`, operator)
  }

  case object SUSPEND_CASE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrAssignedTeamMembersOnly(`case`, operator)
  }

  case object COMPLETE_CASE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrAssignedTeamMembersOnly(`case`, operator)
  }

  case object REMOVE_ATTACHMENTS extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrAssignedTeamMembersOnly(`case`, operator)
  }

  case object CANCEL_CASE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrAssignedTeamMembersOnly(`case`, operator)
  }

  case object ADD_NOTE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrTeamMembersOnly(operator)
  }

  case object ADD_ATTACHMENT extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrTeamMembersOnly(operator)
  }

  case object KEYWORDS extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrAssignedTeamMembersOnly(`case`, operator)
  }

  case object EDIT_LIABILITY extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrAssignedTeamMembersOnly(`case`, operator)
  }

  case object EDIT_RULING extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrAssignedTeamMembersOnly(`case`, operator)
  }

  case object APPEAL_CASE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrTeamMembersOnly(operator)
  }

  case object EXTENDED_USE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrTeamMembersOnly(operator)
  }

  case object MOVE_CASE_BACK_TO_QUEUE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrAssignedTeamMembersOnly(`case`, operator)
  }

  case object MANAGE_KEYWORDS extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = adminOnly(operator)
  }

  case object MANAGE_QUEUES extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = adminOnly(operator)
  }

  case object EDIT_SAMPLE extends CasePermission {
    override def name: String = this.getClass.getName
    override def appliesTo(`case`: Case, operator: Operator): Boolean = managersOrTeamMembersOnly(operator)
  }
}



//object Permission extends Enumeration {
//  type Permission = Value
//  val
//    // READ
//  VIEW_MY_CASES, VIEW_QUEUE_CASES, VIEW_ASSIGNED_CASES, SEARCH, ADVANCED_SEARCH, VIEW_CASES, VIEW_CASE_ASSIGNEE, VIEW_REPORTS,
//    // WRITE
//  CREATE_CASES, ASSIGN_CASE, RELEASE_CASE, SUPPRESS_CASE, REFER_CASE, REOPEN_CASE, REJECT_CASE, SUSPEND_CASE, COMPLETE_CASE, REMOVE_ATTACHMENTS,
//  CANCEL_CASE, ADD_NOTE, ADD_ATTACHMENT, KEYWORDS, EDIT_LIABILITY, EDIT_RULING, APPEAL_CASE, EXTENDED_USE, MOVE_CASE_BACK_TO_QUEUE,
//  MANAGE_KEYWORDS, MANAGE_QUEUES, EDIT_SAMPLE = Value
//
//  lazy val teamCaseOwnerPermissions: Set[Permission] = Set(
//    CREATE_CASES, VIEW_MY_CASES, VIEW_QUEUE_CASES, SEARCH, ADVANCED_SEARCH, VIEW_CASES, ASSIGN_CASE,
//    RELEASE_CASE, SUPPRESS_CASE, REFER_CASE, REOPEN_CASE, REJECT_CASE, SUSPEND_CASE, COMPLETE_CASE, REMOVE_ATTACHMENTS,
//    CANCEL_CASE, ADD_NOTE, ADD_ATTACHMENT, KEYWORDS, EDIT_LIABILITY, EDIT_RULING, APPEAL_CASE, EXTENDED_USE, MOVE_CASE_BACK_TO_QUEUE, EDIT_SAMPLE
//  )

//  lazy val readOnlyPermissions: Set[Permission] = Set(
//    SEARCH, ADVANCED_SEARCH, VIEW_CASES
//  )
//
//  lazy val systemAdminPermissions: Set[Permission] = Set(
//    MANAGE_KEYWORDS, MANAGE_QUEUES
//  )
//
//  private val managerPermissions: Set[Permission] = Set(
//    CREATE_CASES, VIEW_MY_CASES, VIEW_QUEUE_CASES, VIEW_ASSIGNED_CASES, SEARCH, ADVANCED_SEARCH, VIEW_CASES, ASSIGN_CASE, VIEW_CASE_ASSIGNEE,
//    RELEASE_CASE, SUPPRESS_CASE, REFER_CASE, REOPEN_CASE, REJECT_CASE, SUSPEND_CASE, COMPLETE_CASE, REMOVE_ATTACHMENTS,
//    CANCEL_CASE, ADD_NOTE, ADD_ATTACHMENT, KEYWORDS, EDIT_LIABILITY, EDIT_RULING, APPEAL_CASE, EXTENDED_USE, MOVE_CASE_BACK_TO_QUEUE, VIEW_REPORTS, EDIT_SAMPLE)
//
//  private val teamBasicPermissions: Set[Permission] = Set(CREATE_CASES, VIEW_MY_CASES, VIEW_QUEUE_CASES, SEARCH, ADVANCED_SEARCH, VIEW_CASES, ASSIGN_CASE, VIEW_CASE_ASSIGNEE,
//    RELEASE_CASE, SUPPRESS_CASE, REOPEN_CASE, CANCEL_CASE, ADD_NOTE, ADD_ATTACHMENT, APPEAL_CASE, EXTENDED_USE, EDIT_SAMPLE)
//
//  def roleBasedPermissions(role: Role): Set[Permission] = {
//    role match {
//      case Role.CLASSIFICATION_OFFICER => teamBasicPermissions
//      case Role.CLASSIFICATION_MANAGER => managerPermissions
//      case Role.READ_ONLY => readOnlyPermissions
//    }
//  }
//}
