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

  val VIEW_MY_CASES, VIEW_QUEUE_CASES, VIEW_ASSIGNED_CASES, SEARCH, ADVANCED_SEARCH, VIEW_CASES, ASSIGN_CASE,
  RELEASE_CASE, SUPPRESS_CASE, REFER_CASE, REOPEN_CASE, REJECT_CASE, SUSPEND_CASE, COMPLETE_CASE,
  CANCEL_CASE, ADD_NOTE, ADD_ATTACHMENT, KEYWORDS, EDIT_RULING, APPEAL_CASE, REVIEW_CASE, EXTENDED_USE, MOVE_CASE_BACK_TO_QUEUE,
  MANAGE_KEYWORDS, MANAGE_QUEUES, VIEW_REPORTS = Value

  val teamCaseOwnerPermissions: Set[Permission] = Set(VIEW_MY_CASES, VIEW_QUEUE_CASES, SEARCH, ADVANCED_SEARCH, VIEW_CASES, ASSIGN_CASE,
    RELEASE_CASE, SUPPRESS_CASE, REFER_CASE, REOPEN_CASE, REJECT_CASE, SUSPEND_CASE, COMPLETE_CASE,
    CANCEL_CASE, ADD_NOTE, ADD_ATTACHMENT, KEYWORDS, EDIT_RULING, APPEAL_CASE, REVIEW_CASE, EXTENDED_USE, MOVE_CASE_BACK_TO_QUEUE)

  val readOnlyPermissions: Set[Permission] = Set(SEARCH, ADVANCED_SEARCH, VIEW_CASES)

  val systemAdminPermissions: Set[Permission] = Set(MANAGE_KEYWORDS, MANAGE_QUEUES)

  private val managerPermissions: Set[Permission] = Set(VIEW_MY_CASES, VIEW_QUEUE_CASES, VIEW_ASSIGNED_CASES, SEARCH, ADVANCED_SEARCH, VIEW_CASES, ASSIGN_CASE,
    RELEASE_CASE, SUPPRESS_CASE, REFER_CASE, REOPEN_CASE, REJECT_CASE, SUSPEND_CASE, COMPLETE_CASE,
    CANCEL_CASE, ADD_NOTE, ADD_ATTACHMENT, KEYWORDS, EDIT_RULING, APPEAL_CASE, REVIEW_CASE, EXTENDED_USE, MOVE_CASE_BACK_TO_QUEUE, VIEW_REPORTS)

  private val teamBasicPermissions: Set[Permission] = Set(VIEW_MY_CASES, VIEW_QUEUE_CASES, SEARCH, ADVANCED_SEARCH, VIEW_CASES, ASSIGN_CASE,
    RELEASE_CASE, SUPPRESS_CASE, REOPEN_CASE,
    CANCEL_CASE, ADD_NOTE, ADD_ATTACHMENT, APPEAL_CASE, REVIEW_CASE, EXTENDED_USE)

  def roleBasedPermissions(role: Role): Set[Permission] = {
    role match {
      case Role.CLASSIFICATION_OFFICER => teamBasicPermissions
      case Role.CLASSIFICATION_MANAGER => managerPermissions
      case Role.READ_ONLY => readOnlyPermissions
    }
  }
}
