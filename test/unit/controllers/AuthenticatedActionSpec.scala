/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import config.AppConfig
import connector.StrideAuthConnector
import models.request.AuthenticatedRequest
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.given
import org.mockito.Mockito.reset
import org.mockito.{ArgumentCaptor, ArgumentMatchers, Mockito}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.mvc.{AnyContent, ControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.{ConfigLoader, Configuration, Environment, Mode}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{~, _}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedActionSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val appConfig = mock[AppConfig]
  private val config = mock[Configuration]
  private val environment = mock[Environment]
  private val connector = mock[StrideAuthConnector]
  private val block: AuthenticatedRequest[AnyContent] => Future[Result] = mock[AuthenticatedRequest[AnyContent] => Future[Result]]
  private val result = mock[Result]
  private val controllerComponents = injector.instanceOf[ControllerComponents]

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(config, environment, connector, block, result)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    given(appConfig.teamEnrolment).willReturn("team-enrolment")
    given(appConfig.managerEnrolment).willReturn("manager-enrolment")
    given(appConfig.readOnlyEnrolment).willReturn("read-only-enrolment")
    given(appConfig.checkEnrolment).willReturn(true)
    given(environment.mode).willReturn(Mode.Test)
    given(config.getString(any[String], any[Option[Set[String]]])).willReturn(None)
    given(config.getOptional(any[String])(any)).willReturn(None)
  }

  "Invoke Block" should {

    "Invoke block on success" in {
      givenAuthSuccess()
      givenTheBlockExecutesSuccessfully()

      await(action.invokeBlock(FakeRequest(), block)) shouldBe result

      val operator = theAuthenticatedRequest().operator
      operator.id shouldBe "id"
      operator.name shouldBe Some("full name")
      operator.manager shouldBe false
    }

    "Invoke block on success with missing name" in {
      givenAuthSuccess(name = Name(None, None))
      givenTheBlockExecutesSuccessfully()

      await(action.invokeBlock(FakeRequest(), block)) shouldBe result

      val operator = theAuthenticatedRequest().operator
      operator.id shouldBe "id"
      operator.name shouldBe None
      operator.manager shouldBe false
    }

    "Invoke block on success as manager" in {
      givenAuthSuccess(manager = true)
      givenTheBlockExecutesSuccessfully()

      await(action.invokeBlock(FakeRequest(), block)) shouldBe result

      val operator = theAuthenticatedRequest().operator
      operator.id shouldBe "id"
      operator.name shouldBe Some("full name")
      operator.manager shouldBe true
    }

    "Allow invocation exceptions to propagate" in {
      val exception = new RuntimeException("Exception")
      givenAuthSuccess(name = Name(None, None))
      givenTheBlockThrowsAnError(exception)

      intercept[RuntimeException] {
        await(action.invokeBlock(FakeRequest(), block))
      } shouldBe exception
    }

    "Allow unknown exceptions to propagate" in {
      val exception = new RuntimeException("Exception")
      given(connector.authorise(
        any[Predicate],
        any[Retrieval[Credentials ~ Name]]
      )(any[HeaderCarrier], any[ExecutionContext])).willReturn(Future.failed(exception))

      intercept[RuntimeException] {
        await(action.invokeBlock(FakeRequest(), block))
      } shouldBe exception
    }

    "Redirect to Stride Login on NoActiveSession" in {
      given(appConfig.runningAsDev).willReturn(false)
      given(connector.authorise(
        any[Predicate],
        any[Retrieval[Credentials ~ Name]]
      )(any[HeaderCarrier], any[ExecutionContext])).willReturn(Future.failed(new NoActiveSession("No Session") {}))

      val result: Result = await(action.invokeBlock(FakeRequest(), block))
      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/stride/sign-in?successURL=%2F&origin=undefined")
    }

    "Redirect to Stride Login Dev on NoActiveSession" in {
      given(appConfig.runningAsDev).willReturn(true)
      given(config.getOptional[String](ArgumentMatchers.eq("run.mode"))(any[ConfigLoader[String]])).willReturn(Some("Dev"))
      given(connector.authorise(
        any[Predicate],
        any[Retrieval[Credentials ~ Name]]
      )(any[HeaderCarrier], any[ExecutionContext])).willReturn(Future.failed(new NoActiveSession("No Session") {}))

      val result: Result = await(action.invokeBlock(FakeRequest(), block))
      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some("/stride/sign-in?successURL=http%3A%2F%2Flocalhost%2F&origin=undefined")
    }

    "Redirect to Unauthorized on AuthorizationException" in {
      given(connector.authorise(
        any[Predicate],
        any[Retrieval[Credentials ~ Name]]
      )(any[HeaderCarrier], any[ExecutionContext])).willReturn(Future.failed(new AuthorisationException("Error"){}))

      val result: Result = await(action.invokeBlock(FakeRequest(), block))
      status(result) shouldBe Status.SEE_OTHER
    }
  }

  private def action: AuthenticatedAction = {
    new AuthenticatedAction(appConfig, defaultPlayBodyParsers, config, environment, connector, controllerComponents)
  }

  private def theAuthenticatedRequest(): AuthenticatedRequest[AnyContent] = {
    val captor: ArgumentCaptor[AuthenticatedRequest[AnyContent]] = ArgumentCaptor.forClass(classOf[AuthenticatedRequest[AnyContent]])
    Mockito.verify(block).apply(captor.capture())
    captor.getValue
  }

  private def givenAuthSuccess(id: String = "id", name: Name = Name(Some("full name"), Some("surname")), manager: Boolean = false): Unit = {
    val predicate: Predicate = (Enrolment("team-enrolment") or Enrolment("manager-enrolment")or Enrolment("read-only-enrolment")) and AuthProviders(PrivilegedApplication)
    val retrieval: Retrieval[Credentials ~ Name ~ Enrolments] = Retrievals.credentials and Retrievals.name and Retrievals.allEnrolments
    val enrolments: Set[Enrolment] = if(manager) Set(Enrolment("manager-enrolment")) else Set.empty
    val value: Credentials ~ Name ~ Enrolments = new ~(new ~(Credentials(id, "type"), name), Enrolments(enrolments))
    given(connector.authorise(refEq(predicate), refEq(retrieval))(any[HeaderCarrier], refEq(global))).willReturn(Future.successful(value))
  }

  private def givenTheBlockExecutesSuccessfully(): Unit = {
    given(block.apply(any[AuthenticatedRequest[AnyContent]])).willReturn(Future.successful(result))
  }

  private def givenTheBlockThrowsAnError(e: RuntimeException): Unit = {
    given(block.apply(any[AuthenticatedRequest[AnyContent]])).willThrow(e)
  }


}
