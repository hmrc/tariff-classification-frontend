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

import uk.gov.hmrc.tariffclassificationfrontend.models.Role.Role

object Permission extends Enumeration {
  type Permission = Value

  val CREATE_CASES, VIEW_MY_CASES, VIEW_QUEUE_CASES, VIEW_ASSIGNED_CASES, SEARCH, ADVANCED_SEARCH, VIEW_CASES, VIEW_CASE_ASSIGNEE, ASSIGN_CASE,
  RELEASE_CASE, SUPPRESS_CASE, REFER_CASE, REOPEN_CASE, REJECT_CASE, SUSPEND_CASE, COMPLETE_CASE, REMOVE_ATTACHMENTS,
  CANCEL_CASE, ADD_NOTE, ADD_ATTACHMENT, KEYWORDS, EDIT_LIABILITY, EDIT_RULING, APPEAL_CASE, EXTENDED_USE, MOVE_CASE_BACK_TO_QUEUE,
  MANAGE_KEYWORDS, MANAGE_QUEUES, VIEW_REPORTS, EDIT_SAMPLE = Value

  val teamCaseOwnerPermissions: Set[Permission] = Set(CREATE_CASES, VIEW_MY_CASES, VIEW_QUEUE_CASES, SEARCH, ADVANCED_SEARCH, VIEW_CASES, ASSIGN_CASE,
    RELEASE_CASE, SUPPRESS_CASE, REFER_CASE, REOPEN_CASE, REJECT_CASE, SUSPEND_CASE, COMPLETE_CASE, REMOVE_ATTACHMENTS,
    CANCEL_CASE, ADD_NOTE, ADD_ATTACHMENT, KEYWORDS, EDIT_LIABILITY, EDIT_RULING, APPEAL_CASE, EXTENDED_USE, MOVE_CASE_BACK_TO_QUEUE,EDIT_SAMPLE)

  val readOnlyPermissions: Set[Permission] = Set(SEARCH, ADVANCED_SEARCH, VIEW_CASES)

  val systemAdminPermissions: Set[Permission] = Set(MANAGE_KEYWORDS, MANAGE_QUEUES)

  private val managerPermissions: Set[Permission] = Set(CREATE_CASES, VIEW_MY_CASES, VIEW_QUEUE_CASES, VIEW_ASSIGNED_CASES, SEARCH, ADVANCED_SEARCH, VIEW_CASES, ASSIGN_CASE, VIEW_CASE_ASSIGNEE,
    RELEASE_CASE, SUPPRESS_CASE, REFER_CASE, REOPEN_CASE, REJECT_CASE, SUSPEND_CASE, COMPLETE_CASE, REMOVE_ATTACHMENTS,
    CANCEL_CASE, ADD_NOTE, ADD_ATTACHMENT, KEYWORDS, EDIT_LIABILITY, EDIT_RULING, APPEAL_CASE, EXTENDED_USE, MOVE_CASE_BACK_TO_QUEUE, VIEW_REPORTS, EDIT_SAMPLE)

  private val teamBasicPermissions: Set[Permission] = Set(CREATE_CASES, VIEW_MY_CASES, VIEW_QUEUE_CASES, SEARCH, ADVANCED_SEARCH, VIEW_CASES, ASSIGN_CASE, VIEW_CASE_ASSIGNEE,
    RELEASE_CASE, SUPPRESS_CASE, REOPEN_CASE, CANCEL_CASE, ADD_NOTE, ADD_ATTACHMENT, APPEAL_CASE, EXTENDED_USE, EDIT_SAMPLE)

  def roleBasedPermissions(role: Role): Set[Permission] = {
    role match {
      case Role.CLASSIFICATION_OFFICER => teamBasicPermissions
      case Role.CLASSIFICATION_MANAGER => managerPermissions
      case Role.READ_ONLY => readOnlyPermissions
    }
  }
}
