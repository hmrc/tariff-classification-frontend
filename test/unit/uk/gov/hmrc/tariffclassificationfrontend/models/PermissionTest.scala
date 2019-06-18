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

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.tariffclassificationfrontend.utils.Cases._

class PermissionTest extends UnitSpec {

  private val readOnly = Operator(id = "team", role = Role.READ_ONLY)
  private val teamMember = Operator(id = "team", role = Role.CLASSIFICATION_OFFICER)
  private val manager = Operator(id = "team", role = Role.CLASSIFICATION_MANAGER)
  private def caseUnassigned = aCase(withoutAssignee())
  private def caseAssignedTo(operator: Operator) = aCase(withAssignee(Some(operator)))

  "Permissions" should {
    "contain 'Create Cases'" in {
      val permission = Permission.CREATE_CASES
      val name = "CREATE_CASES"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(readOnly) shouldBe false
      permission.appliesTo(teamMember) shouldBe true
      permission.appliesTo(manager) shouldBe true
    }

    "contain 'View My Cases'" in {
      val permission = Permission.VIEW_MY_CASES
      val name = "VIEW_MY_CASES"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(readOnly) shouldBe false
      permission.appliesTo(teamMember) shouldBe true
      permission.appliesTo(manager) shouldBe true
    }

    "contain 'View Case Queues'" in {
      val permission = Permission.VIEW_QUEUE_CASES
      val name = "VIEW_QUEUE_CASES"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(readOnly) shouldBe false
      permission.appliesTo(teamMember) shouldBe true
      permission.appliesTo(manager) shouldBe true
    }

    "contain 'View Assigned Cases'" in {
      val permission = Permission.VIEW_ASSIGNED_CASES
      val name = "VIEW_ASSIGNED_CASES"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(readOnly) shouldBe false
      permission.appliesTo(teamMember) shouldBe false
      permission.appliesTo(manager) shouldBe true
    }

    "contain 'View Case Assignee'" in {
      val permission = Permission.VIEW_CASE_ASSIGNEE
      val name = "VIEW_CASE_ASSIGNEE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Search By Case Reference'" in {
      val permission = Permission.SEARCH
      val name = "SEARCH"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(readOnly) shouldBe true
      permission.appliesTo(teamMember) shouldBe true
      permission.appliesTo(manager) shouldBe true
    }

    "contain 'Advanced Search'" in {
      val permission = Permission.ADVANCED_SEARCH
      val name = "ADVANCED_SEARCH"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(readOnly) shouldBe true
      permission.appliesTo(teamMember) shouldBe true
      permission.appliesTo(manager) shouldBe true
    }

    "contain 'View Cases'" in {
      val permission = Permission.VIEW_CASES
      val name = "VIEW_CASES"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(readOnly) shouldBe true
      permission.appliesTo(teamMember) shouldBe true
      permission.appliesTo(manager) shouldBe true
    }

    "contain 'Take Ownership of a Case'" in {
      val permission = Permission.ASSIGN_CASE
      val name = "ASSIGN_CASE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(readOnly) shouldBe false
      permission.appliesTo(teamMember) shouldBe true
      permission.appliesTo(manager) shouldBe true
    }

    "contain 'Release a Case'" in {
      val permission = Permission.RELEASE_CASE
      val name = "RELEASE_CASE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Suppress a Case'" in {
      val permission = Permission.SUPPRESS_CASE
      val name = "SUPPRESS_CASE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Refer a Case'" in {
      val permission = Permission.REFER_CASE
      val name = "REFER_CASE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe false
      permission.appliesTo(caseAssignedTo(teamMember), teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Reopen a Case'" in {
      val permission = Permission.REOPEN_CASE
      val name = "REOPEN_CASE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Reject a Case'" in {
      val permission = Permission.REJECT_CASE
      val name = "REJECT_CASE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe false
      permission.appliesTo(caseAssignedTo(teamMember), teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Suspend a Case'" in {
      val permission = Permission.SUSPEND_CASE
      val name = "SUSPEND_CASE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe false
      permission.appliesTo(caseAssignedTo(teamMember), teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Complete a Case'" in {
      val permission = Permission.COMPLETE_CASE
      val name = "COMPLETE_CASE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe false
      permission.appliesTo(caseAssignedTo(teamMember), teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Cancel a Case'" in {
      val permission = Permission.CANCEL_CASE
      val name = "CANCEL_CASE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Add a Note'" in {
      val permission = Permission.ADD_NOTE
      val name = "ADD_NOTE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Add an Attachment'" in {
      val permission = Permission.ADD_ATTACHMENT
      val name = "ADD_ATTACHMENT"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Remove an Attachment'" in {
      val permission = Permission.REMOVE_ATTACHMENTS
      val name = "REMOVE_ATTACHMENTS"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe false
      permission.appliesTo(caseAssignedTo(teamMember), teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Add/Remove Keywords'" in {
      val permission = Permission.KEYWORDS
      val name = "KEYWORDS"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe false
      permission.appliesTo(caseAssignedTo(teamMember), teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Edit a Ruling'" in {
      val permission = Permission.EDIT_RULING
      val name = "EDIT_RULING"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe false
      permission.appliesTo(caseAssignedTo(teamMember), teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Edit a Liability'" in {
      val permission = Permission.EDIT_LIABILITY
      val name = "EDIT_LIABILITY"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe false
      permission.appliesTo(caseAssignedTo(teamMember), teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Edit Appeal Status'" in {
      val permission = Permission.APPEAL_CASE
      val name = "APPEAL_CASE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Edit Extended Use Status'" in {
      val permission = Permission.EXTENDED_USE
      val name = "EXTENDED_USE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'Move Case Back To Queue'" in {
      val permission = Permission.MOVE_CASE_BACK_TO_QUEUE
      val name = "MOVE_CASE_BACK_TO_QUEUE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe false
      permission.appliesTo(caseAssignedTo(teamMember), teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

    "contain 'View Reports'" in {
      val permission = Permission.VIEW_REPORTS
      val name = "VIEW_REPORTS"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(readOnly) shouldBe false
      permission.appliesTo(teamMember) shouldBe false
      permission.appliesTo(manager) shouldBe true
    }

    "contain 'Edit a Sample'" in {
      val permission = Permission.EDIT_SAMPLE
      val name = "EDIT_SAMPLE"

      permission.name shouldBe name
      Permission.from(name) shouldBe Some(permission)

      permission.appliesTo(caseUnassigned, readOnly) shouldBe false
      permission.appliesTo(caseUnassigned, teamMember) shouldBe true
      permission.appliesTo(caseUnassigned, manager) shouldBe true
    }

  }

}
