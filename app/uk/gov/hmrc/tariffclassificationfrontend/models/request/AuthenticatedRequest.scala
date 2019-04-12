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

package uk.gov.hmrc.tariffclassificationfrontend.models.request

import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AccessType.{AccessType, READ_WRITE}
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Operator}

class AuthenticatedRequest[A](_operator: Operator, _request: Request[A], _accessType: AccessType = READ_WRITE)
                  extends WrappedRequest[A](_request) {

  val operator : Operator = _operator
  val request  : Request[A] = _request

  val hasWritePermission: Boolean = _accessType == AccessType.READ_WRITE
  val hasReadOnlyPermission: Boolean = _accessType == AccessType.READ_ONLY
}

class AuthenticatedCaseRequest[A](operator: Operator, request: Request[A], accessType: AccessType, _c: Option[Case])
                  extends AuthenticatedRequest[A] (operator , request, accessType){

  val c : Option[Case] = _c
}

object AccessType extends Enumeration {
  type AccessType = Value
  val READ_ONLY, READ_WRITE  = Value
}