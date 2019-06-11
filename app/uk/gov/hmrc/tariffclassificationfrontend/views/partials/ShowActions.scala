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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials

import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.CaseStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest

class ShowActions(caseStatus: CaseStatus)(implicit request: AuthenticatedRequest[_]) {

  def refer: Boolean = caseStatus == CaseStatus.OPEN && request.hasPermission(Permission.REFER_CASE)
  def reject: Boolean = caseStatus == CaseStatus.OPEN && request.hasPermission(Permission.REJECT_CASE)
  def suspend: Boolean = caseStatus == CaseStatus.OPEN && request.hasPermission(Permission.SUSPEND_CASE)
  def release: Boolean = caseStatus == CaseStatus.NEW && request.hasPermission(Permission.RELEASE_CASE)
  def suppress: Boolean = caseStatus == CaseStatus.NEW && request.hasPermission(Permission.SUPPRESS_CASE)
  def reopen: Boolean = (caseStatus == CaseStatus.SUSPENDED || caseStatus == CaseStatus.REFERRED) && request.hasPermission(Permission.REOPEN_CASE)

  def any: Boolean = refer || reject || suspend || release || suppress || reopen
}
