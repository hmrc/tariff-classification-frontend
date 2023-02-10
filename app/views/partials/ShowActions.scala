/*
 * Copyright 2023 HM Revenue & Customs
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

package views.partials

import models.CaseStatus.CaseStatus
import models.Permission
import models.request.AuthenticatedRequest

class ShowActions(caseStatus: CaseStatus)(implicit request: AuthenticatedRequest[_]) {

  def refer: Boolean = request.hasPermission(Permission.REFER_CASE)

  def reject: Boolean = request.hasPermission(Permission.REJECT_CASE)

  def suspend: Boolean = request.hasPermission(Permission.SUSPEND_CASE)

  def release: Boolean = request.hasPermission(Permission.RELEASE_CASE)

  def suppress: Boolean = request.hasPermission(Permission.SUPPRESS_CASE)

  def reopen: Boolean = request.hasPermission(Permission.REOPEN_CASE)

  def any: Boolean = refer || reject || suspend || release || suppress || reopen
}

object ShowActions {
  def apply(caseStatus: CaseStatus)(implicit request: AuthenticatedRequest[_]): ShowActions =
    new ShowActions(caseStatus)
}
