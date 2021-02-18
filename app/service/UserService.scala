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

package service

import audit.AuditService
import config.AppConfig
import connector.BindingTariffClassificationConnector
import models.{Operator, Paged, Pagination}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import javax.inject.Inject
import models.Role.Role

import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(
  auditService: AuditService,
  connector: BindingTariffClassificationConnector
)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends Logging {

  def getAllUsers(role: Seq[Role], team: String, pagination: Pagination)(
    implicit hc: HeaderCarrier): Future[Paged[Operator]] =
    connector.getAllUsers(role, team, pagination)

  def updateUser(originalOperator: Operator, operatorMakingTheChange: Operator)(
    implicit hc: HeaderCarrier
  ): Future[Operator] =
    for {
      updated <- connector.updateUser(originalOperator)
      _ = auditService.auditUserUpdated(originalOperator, operatorMakingTheChange)
    } yield updated

  def getUser(id: String)(implicit hc: HeaderCarrier): Future[Option[Operator]] =
    connector.getUserDetails(id)
}
