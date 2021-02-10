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

package controllers

import config.AppConfig
import connector.StrideAuthConnector
import models.{Operator, Role}
import models.request.AuthenticatedRequest
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.given
import org.mockito.Mockito.{reset, verify}
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.mvc.{AnyContent, ControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.{ConfigLoader, Configuration, Environment, Mode}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Credentials, Name, Retrieval, ~}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import connector.BindingTariffClassificationConnector

class AuthenticatedActionSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private val appConfig   = mock[AppConfig]
  private val config      = mock[Configuration]
  private val environment = mock[Environment]
  private val connector   = mock[StrideAuthConnector]
  private val userConnector = mock[BindingTariffClassificationConnector]
  private val block: AuthenticatedRequest[AnyContent] => Future[Result] =
    mock[AuthenticatedRequest[AnyContent] => Future[Result]]
  private val result               = mock[Result]
  private val controllerComponents = injector.instanceOf[ControllerComponents]

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(config, environment, connector, userConnector, block, result)
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    given(appConfig.teamEnrolment).willReturn("team-enrolment")
    given(appConfig.managerEnrolment).willReturn("manager-enrolment")
    given(appConfig.readOnlyEnrolment).willReturn("read-only-enrolment")
    given(appConfig.checkEnrolment).willReturn(true)
    given(environment.mode).willReturn(Mode.Test)
    given(config.getOptional(any[String])(any)).willReturn(None)
  }

  "AuthenticatedAction" when {
    "successful and no existing user" should {
      "create new user and invoke block" in {
        givenAuthSuccess()
        givenNoExistingUser()
        givenTheBlockExecutesSuccessfully()

        await(action.invokeBlock(FakeRequest(), block)) shouldBe result

        val createdUser = theUserCreated()
        createdUser.id shouldBe "id"
        createdUser.name shouldBe Some("full name")
        createdUser.email shouldBe Some("test@example.com")
        createdUser.role shouldBe Role.CLASSIFICATION_OFFICER
        createdUser.manager shouldBe false

        val operator = theAuthenticatedRequest().operator
        operator.id      shouldBe "id"
        operator.name    shouldBe Some("full name")
        operator.email shouldBe Some("test@example.com")
        operator.role shouldBe Role.CLASSIFICATION_OFFICER
        operator.manager shouldBe false
      }

      "create new user and invoke block when auth provides no name" in {
        givenAuthSuccess(name = Name(None, None))
        givenNoExistingUser()
        givenTheBlockExecutesSuccessfully()

        await(action.invokeBlock(FakeRequest(), block)) shouldBe result

        val createdUser = theUserCreated()
        createdUser.id shouldBe "id"
        createdUser.name shouldBe None
        createdUser.email shouldBe Some("test@example.com")
        createdUser.role shouldBe Role.CLASSIFICATION_OFFICER
        createdUser.manager shouldBe false

        val operator = theAuthenticatedRequest().operator
        operator.id      shouldBe "id"
        operator.name    shouldBe None
        operator.email shouldBe Some("test@example.com")
        operator.role shouldBe Role.CLASSIFICATION_OFFICER
        operator.manager shouldBe false
      }

      "create new user and invoke block when the user is a manager" in {
        givenAuthSuccess(manager = true)
        givenNoExistingUser()
        givenTheBlockExecutesSuccessfully()

        await(action.invokeBlock(FakeRequest(), block)) shouldBe result

        val createdUser = theUserCreated()
        createdUser.id shouldBe "id"
        createdUser.name shouldBe Some("full name")
        createdUser.email shouldBe Some("test@example.com")
        createdUser.role shouldBe Role.CLASSIFICATION_MANAGER
        createdUser.manager shouldBe true

        val operator = theAuthenticatedRequest().operator
        operator.id      shouldBe "id"
        operator.name    shouldBe Some("full name")
        operator.email shouldBe Some("test@example.com")
        operator.role shouldBe Role.CLASSIFICATION_MANAGER
        operator.manager shouldBe true
      }
    }

    "successful and user exists" should {
      val existingOperator = Operator("id", Some("full name"), Some("test@example.com"), Role.CLASSIFICATION_OFFICER)

      "invoke block" in {
        givenAuthSuccess()
        givenExistingUser(existingOperator)
        givenTheBlockExecutesSuccessfully()

        await(action.invokeBlock(FakeRequest(), block)) shouldBe result

        val operator = theAuthenticatedRequest().operator
        operator.id      shouldBe "id"
        operator.name    shouldBe Some("full name")
        operator.email shouldBe Some("test@example.com")
        operator.role shouldBe Role.CLASSIFICATION_OFFICER
        operator.manager shouldBe false
      }

      "update the user information and invoke block when name is updated" in {
        givenAuthSuccess(name = Name(Some("new name"), Some("name")))
        givenExistingUser(existingOperator.copy(name = None))
        givenTheBlockExecutesSuccessfully()

        await(action.invokeBlock(FakeRequest(), block)) shouldBe result

        val updatedUser = theUserUpdated()
        updatedUser.id shouldBe "id"
        updatedUser.name shouldBe Some("new name")
        updatedUser.email shouldBe Some("test@example.com")
        updatedUser.role shouldBe Role.CLASSIFICATION_OFFICER
        updatedUser.manager shouldBe false

        val operator = theAuthenticatedRequest().operator
        operator.id      shouldBe "id"
        operator.name    shouldBe Some("new name")
        operator.email shouldBe Some("test@example.com")
        operator.role shouldBe Role.CLASSIFICATION_OFFICER
        operator.manager shouldBe false
      }

      "update the user information and invoke block when email is updated" in {
        givenAuthSuccess(email = "foo@bar.com")
        givenExistingUser(existingOperator)
        givenTheBlockExecutesSuccessfully()

        await(action.invokeBlock(FakeRequest(), block)) shouldBe result

        val updatedUser = theUserUpdated()
        updatedUser.id shouldBe "id"
        updatedUser.name shouldBe Some("full name")
        updatedUser.email shouldBe Some("foo@bar.com")
        updatedUser.role shouldBe Role.CLASSIFICATION_OFFICER
        updatedUser.manager shouldBe false

        val operator = theAuthenticatedRequest().operator
        operator.id      shouldBe "id"
        operator.name    shouldBe Some("full name")
        operator.email shouldBe Some("foo@bar.com")
        operator.role shouldBe Role.CLASSIFICATION_OFFICER
        operator.manager shouldBe false
      }

      "update the user information and invoke block when the user's role changes" in {
        givenAuthSuccess(manager = true)
        givenExistingUser(existingOperator)
        givenTheBlockExecutesSuccessfully()

        await(action.invokeBlock(FakeRequest(), block)) shouldBe result

        val updatedUser = theUserUpdated()
        updatedUser.id shouldBe "id"
        updatedUser.name shouldBe Some("full name")
        updatedUser.email shouldBe Some("test@example.com")
        updatedUser.role shouldBe Role.CLASSIFICATION_MANAGER
        updatedUser.manager shouldBe true

        val operator = theAuthenticatedRequest().operator
        operator.id      shouldBe "id"
        operator.name    shouldBe Some("full name")
        operator.email shouldBe Some("test@example.com")
        operator.role shouldBe Role.CLASSIFICATION_MANAGER
        operator.manager shouldBe true
      }

      "preserve the user's assigned teams when updating user info" in {
        givenAuthSuccess(manager = true, name = Name(Some("new name"), Some("name")))
        givenExistingUser(existingOperator.copy(
          role = Role.CLASSIFICATION_MANAGER,
          managerOfTeams = Seq("1"),
          memberOfTeams = Seq("1")
        ))
        givenTheBlockExecutesSuccessfully()

        await(action.invokeBlock(FakeRequest(), block)) shouldBe result

        val updatedUser = theUserUpdated()
        updatedUser.id shouldBe "id"
        updatedUser.name shouldBe Some("new name")
        updatedUser.email shouldBe Some("test@example.com")
        updatedUser.role shouldBe Role.CLASSIFICATION_MANAGER
        updatedUser.manager shouldBe true
        updatedUser.managerOfTeams shouldBe Seq("1")
        updatedUser.memberOfTeams shouldBe Seq("1")

        val operator = theAuthenticatedRequest().operator
        operator.id      shouldBe "id"
        operator.name    shouldBe Some("new name")
        operator.email shouldBe Some("test@example.com")
        operator.role shouldBe Role.CLASSIFICATION_MANAGER
        operator.manager shouldBe true
        operator.managerOfTeams shouldBe Seq("1")
        operator.memberOfTeams shouldBe Seq("1")
      }
    }

    "failed" should {
      "allow invocation exceptions to propagate" in {
        val exception = new RuntimeException("Exception")
        givenAuthSuccess(name = Name(None, None))
        givenNoExistingUser()
        givenTheBlockThrowsAnError(exception)

        intercept[RuntimeException] {
          await(action.invokeBlock(FakeRequest(), block))
        } shouldBe exception
      }

      "allow unknown exceptions to propagate" in {
        val exception = new RuntimeException("Exception")
        given(
          connector.authorise(
            any[Predicate],
            any[Retrieval[Credentials ~ Name]]
          )(any[HeaderCarrier], any[ExecutionContext])
        ).willReturn(Future.failed(exception))

        intercept[RuntimeException] {
          await(action.invokeBlock(FakeRequest(), block))
        } shouldBe exception
      }

      "redirect to Stride Login on NoActiveSession" in {
        given(appConfig.runningAsDev).willReturn(false)
        given(
          connector.authorise(
            any[Predicate],
            any[Retrieval[Credentials ~ Name]]
          )(any[HeaderCarrier], any[ExecutionContext])
        ).willReturn(Future.failed(new NoActiveSession("No Session") {}))

        val result: Result = await(action.invokeBlock(FakeRequest(), block))
        status(result)     shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some("/stride/sign-in?successURL=%2F&origin=undefined")
      }

      "redirect to Stride Login Dev on NoActiveSession" in {
        given(appConfig.runningAsDev).willReturn(true)
        given(config.getOptional[String](ArgumentMatchers.eq("run.mode"))(any[ConfigLoader[String]]))
          .willReturn(Some("Dev"))
        given(
          connector.authorise(
            any[Predicate],
            any[Retrieval[Credentials ~ Name]]
          )(any[HeaderCarrier], any[ExecutionContext])
        ).willReturn(Future.failed(new NoActiveSession("No Session") {}))

        val result: Result = await(action.invokeBlock(FakeRequest(), block))
        status(result)     shouldBe Status.SEE_OTHER
        locationOf(result) shouldBe Some("/stride/sign-in?successURL=http%3A%2F%2Flocalhost%2F&origin=undefined")
      }

      "redirect to Unauthorized on AuthorizationException" in {
        given(
          connector.authorise(
            any[Predicate],
            any[Retrieval[Credentials ~ Name]]
          )(any[HeaderCarrier], any[ExecutionContext])
        ).willReturn(Future.failed(new AuthorisationException("Error") {}))

        val result: Result = await(action.invokeBlock(FakeRequest(), block))
        status(result) shouldBe Status.SEE_OTHER
      }
    }
  }

  private def action: AuthenticatedAction =
    new AuthenticatedAction(appConfig, playBodyParsers, config, environment, connector, userConnector, controllerComponents)

  private def theAuthenticatedRequest(): AuthenticatedRequest[AnyContent] = {
    val captor: ArgumentCaptor[AuthenticatedRequest[AnyContent]] =
      ArgumentCaptor.forClass(classOf[AuthenticatedRequest[AnyContent]])
    verify(block)(captor.capture())
    captor.getValue
  }

  private def theUserCreated(): Operator = {
    val captor = ArgumentCaptor.forClass(classOf[Operator])
    verify(userConnector).createUser(captor.capture())(any[HeaderCarrier])
    captor.getValue
  }

  private def theUserUpdated(): Operator = {
    val captor = ArgumentCaptor.forClass(classOf[Operator])
    verify(userConnector).updateUser(captor.capture())(any[HeaderCarrier])
    captor.getValue
  }

  private def givenAuthSuccess(
    id: String       = "id",
    name: Name       = Name(Some("full name"), Some("surname")),
    email: String    = "test@example.com",
    manager: Boolean = false
  ): Unit = {
    val predicate: Predicate = (Enrolment("team-enrolment") or Enrolment("manager-enrolment") or Enrolment(
      "read-only-enrolment"
    )) and AuthProviders(PrivilegedApplication)
    val retrieval: Retrieval[Option[Credentials] ~ Option[Name] ~ Option[String] ~ Enrolments] =
      Retrievals.credentials and Retrievals.name and Retrievals.email and Retrievals.allEnrolments
    val enrolments: Set[Enrolment] = if (manager) Set(Enrolment("manager-enrolment")) else Set(Enrolment("team-enrolment"))
    val value: Option[Credentials] ~ Option[Name] ~ Option[String] ~ Enrolments =
      new ~(new ~(new ~(Option(Credentials(id, "type")), Option(name)), Option(email)), Enrolments(enrolments))
    given(connector.authorise(refEq(predicate), refEq(retrieval))(any[HeaderCarrier], refEq(global)))
      .willReturn(Future.successful(value))
  }

  private def givenExistingUser(operator: Operator): Unit = {
    given(userConnector.getUserDetails(refEq(operator.id))(any[HeaderCarrier]))
      .willReturn(Future.successful(Some(operator)))
    given(userConnector.updateUser(any[Operator])(any[HeaderCarrier]))
      .will(i => Future.successful(i.getArgument[Operator](0)))
  }

  private def givenNoExistingUser(id: String = "id"): Unit = {
    given(userConnector.getUserDetails(refEq(id))(any[HeaderCarrier]))
      .willReturn(Future.successful(None))
    given(userConnector.createUser(any[Operator])(any[HeaderCarrier]))
      .will(i => Future.successful(i.getArgument[Operator](0)))
  }

  private def givenTheBlockExecutesSuccessfully(): Unit =
    given(block.apply(any[AuthenticatedRequest[AnyContent]])).willReturn(Future.successful(result))

  private def givenTheBlockThrowsAnError(e: RuntimeException): Unit =
    given(block.apply(any[AuthenticatedRequest[AnyContent]])).willThrow(e)

}
