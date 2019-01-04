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

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito.given
import org.mockito.Mockito.reset
import org.mockito.{ArgumentCaptor, Mockito}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.http.Status
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.auth.core.AuthProvider.PrivilegedApplication
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{~, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.StrideAuthConnector
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedActionSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  private val appConfig = mock[AppConfig]
  private val config = mock[Configuration]
  private val environment = mock[Environment]
  private val connector = mock[StrideAuthConnector]
  private val block: AuthenticatedRequest[AnyContent] => Future[Result] = mock[AuthenticatedRequest[AnyContent] => Future[Result]]
  private val result = mock[Result]

  private val action = new AuthenticatedAction(appConfig, config, environment, connector)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(config, environment, connector, block, result)
    given(appConfig.authEnrolment).willReturn("enrolment")
    given(environment.mode).willReturn(Mode.Dev)
    given(config.getString(any[String], any[Option[Set[String]]])).willReturn(None)
  }

  "Invoke Block" should {

    "Invoke block on success" in {
      givenAuthSuccess()
      givenTheBlockExecutesSuccessfully()

      await(action.invokeBlock(FakeRequest(), block)) shouldBe result

      val operator = theAuthenticatedRequest().operator
      operator.id shouldBe "id"
      operator.name shouldBe Some("full name")
    }

    "Invoke block on success with missing name" in {
      givenAuthSuccess(name = Name(None, None))
      givenTheBlockExecutesSuccessfully()

      await(action.invokeBlock(FakeRequest(), block)) shouldBe result

      val operator = theAuthenticatedRequest().operator
      operator.id shouldBe "id"
      operator.name shouldBe None
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
      given(connector.authorise(
        any[Predicate],
        any[Retrieval[Credentials ~ Name]]
      )(any[HeaderCarrier], any[ExecutionContext])).willReturn(Future.failed(new NoActiveSession("No Session") {}))

      val result: Result = await(action.invokeBlock(FakeRequest(), block))
      status(result) shouldBe Status.SEE_OTHER
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

  private def theAuthenticatedRequest(): AuthenticatedRequest[AnyContent] = {
    val captor: ArgumentCaptor[AuthenticatedRequest[AnyContent]] = ArgumentCaptor.forClass(classOf[AuthenticatedRequest[AnyContent]])
    Mockito.verify(block).apply(captor.capture())
    captor.getValue
  }

  private def givenAuthSuccess(id: String = "id", name: Name = Name(Some("full name"), Some("surname"))): Unit = {
    val predicate: Predicate = Enrolment("enrolment") and AuthProviders(PrivilegedApplication)
    val retrieval: Retrieval[Credentials ~ Name] = Retrievals.credentials and Retrievals.name
    val value: Credentials ~ Name = new ~(Credentials(id, "type"), name)
    given(connector.authorise(refEq(predicate), refEq(retrieval))(any[HeaderCarrier], refEq(global))).willReturn(Future.successful(value))
  }

  private def givenTheBlockExecutesSuccessfully(): Unit = {
    given(block.apply(any[AuthenticatedRequest[AnyContent]])).willReturn(Future.successful(result))
  }

  private def givenTheBlockThrowsAnError(e: RuntimeException): Unit = {
    given(block.apply(any[AuthenticatedRequest[AnyContent]])).willThrow(e)
  }

}
