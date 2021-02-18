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
import connector.BindingTariffClassificationConnector
import models.Operator
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

class UserServiceSpec extends ServiceSpecBase with BeforeAndAfterEach {

  private val operator  = mock[Operator]
  private val connector = mock[BindingTariffClassificationConnector]
  private val audit     = mock[AuditService]

  private val service = new UserService(audit, connector)(global, realAppConfig)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(connector, audit, operator)
  }

  "Update User" should {
    val oldUser     = mock[Operator]
    val updatedUser = mock[Operator]
    val manager     = mock[Operator]

    "delegate to connector" in {
      given(connector.updateUser(refEq(oldUser))(any[HeaderCarrier])) willReturn successful(updatedUser)

      await(service.updateUser(oldUser, manager)) shouldBe updatedUser
    }
  }

  "Get User" should {

    "delegate to the connector" in {
      given(connector.getUserDetails("PID")) willReturn successful(Some(operator))

      await(service.getUser("PID")) shouldBe Some(operator)

    }
  }

  "Delete User" should {
    val oldUser     = mock[Operator]
    val updatedUser = mock[Operator]
    val manager     = mock[Operator]

    "delegate to connector" in {
      given(connector.markDeleted(refEq(oldUser))(any[HeaderCarrier])) willReturn successful(updatedUser)

      await(service.markDeleted(oldUser, manager)) shouldBe updatedUser
    }
  }
}
