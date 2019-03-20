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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.StrideAuthConnector
import uk.gov.hmrc.tariffclassificationfrontend.models.Operator
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest

import scala.concurrent.Future

class AuthenticatedManagerSpec extends UnitSpec with MockitoSugar with ControllerCommons with BeforeAndAfterEach {

  private val appConfig = mock[AppConfig]
  private val config = mock[Configuration]
  private val environment = mock[Environment]
  private val connector = mock[StrideAuthConnector]
  private val block: AuthenticatedRequest[_] => Future[Result] = mock[AuthenticatedRequest[_] => Future[Result]]
  private val result = mock[Result]
  private val action = new AuthenticatedManagerAction()

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(config, environment, connector, block, result)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    given(appConfig.teamEnrolment).willReturn("team-enrolment")
    given(appConfig.managerEnrolment).willReturn("manager-enrolment")
    given(environment.mode).willReturn(Mode.Dev)
    given(config.getString(any[String], any[Option[Set[String]]])).willReturn(None)
  }

  "Invoke Block" should {

    "Invoke block when manager" in {
      given(block.apply(any[AuthenticatedRequest[_]])) willReturn Future.successful(result)
      val request = AuthenticatedRequest(Operator("id", None, manager = true), FakeRequest())
      await(action.invokeBlock(request, block)) shouldBe result
    }

    "Redirect when not manager" in {
      val request = AuthenticatedRequest(Operator("id", None), FakeRequest())
      await(action.invokeBlock(request, block)) shouldBe Results.SeeOther(routes.SecurityController.unauthorized().url)
    }



  }


}
