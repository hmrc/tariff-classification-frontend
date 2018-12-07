/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import org.mockito.Mockito.mock
import play.api.mvc.{Request, Result}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.StrideAuthConnector
import uk.gov.hmrc.tariffclassificationfrontend.models.{AuthenticatedRequest, Operator}

import scala.concurrent.Future

class SuccessfulAuthenticatedAction extends AuthenticatedAction(
  appConfig = mock(classOf[AppConfig]),
  config = mock(classOf[Configuration]),
  env = mock(classOf[Environment]),
  authConnector = mock(classOf[StrideAuthConnector])) {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    block(AuthenticatedRequest(Operator("0", Some("name")), request))
  }
}
