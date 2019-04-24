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
  val NOTE, ATTACHMENT, APPEAL, REVIEW, CANCEL, EDIT_RULING, OPEN, COMPLETE, REFER, REJECT, SUSPEND, REASSIGN, KEYWORDS, MANAGE_KEYWORDS = Value

  val managerPermissions: Set[Permission] = Set(
    NOTE,
    ATTACHMENT,
    APPEAL,
    REVIEW,
    CANCEL,
    EDIT_RULING,
    COMPLETE, REFER, REJECT, SUSPEND, REASSIGN, KEYWORDS)
  val teamCaseOwnerPermissions: Set[Permission] = Set(NOTE, ATTACHMENT, APPEAL, REVIEW, CANCEL, EDIT_RULING, COMPLETE, REFER, REJECT, SUSPEND, REASSIGN, KEYWORDS)
  val teamBasicPermisions: Set[Permission] = Set(NOTE, ATTACHMENT, APPEAL, REVIEW, CANCEL)
  val readOnlyPermissions: Set[Nothing] = Set.empty

  def roleBasedPermissions(role: Role): Set[Permission] = {
    role match {
      case Role.CLASSIFICATION_OFFICER => teamBasicPermisions
      case Role.CLASSIFICATION_MANAGER => managerPermissions
    }
  }
}
