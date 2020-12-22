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

import controllers.routes._
import models._
import models.forms.v2.MiscellaneousForm
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.http.Status
import play.api.test.Helpers._
import service.{CasesService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

class CreateMiscellenaousControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  val form : Form[MiscApplication]= MiscellaneousForm.newMiscForm

  private val casesService = mock[CasesService]
  private val queuesService = mock[QueuesService]
  private val operator     = mock[Operator]
  private val releaseCaseView = injector.instanceOf[views.html.release_case]
  private val confirmation_case_creation = injector.instanceOf[views.html.v2.confirmation_case_creation]

  private val caseWithStatusOPEN = Cases.btiCaseExample.copy(reference = "reference", status = CaseStatus.OPEN)

  private def controller(c: Case) =
    new CreateMiscellaneousController(
      new SuccessfulRequestActions(playBodyParsers, operator, c = c),
      casesService,
      queuesService,
      mcc,
      releaseCaseView,
      confirmation_case_creation,
      realAppConfig
    )

  private def controller(requestCase: Case, permission: Set[Permission]) =
    new CreateMiscellaneousController(
      new RequestActionsWithPermissions(playBodyParsers, permission, c = requestCase),
      casesService,
      queuesService,
      mcc,
      releaseCaseView,
      confirmation_case_creation,
      realAppConfig
    )


  "CreateMiscellaneousController" should {

    "return OK with correct HTML" in {
      val result = await(controller(caseWithStatusOPEN).get()(newFakeGETRequestWithCSRF(app)))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
    }

    "return OK when the user has the right permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .get()(newFakeGETRequestWithCSRF(app))
      )

      status(result)          shouldBe Status.OK
      contentType(result)     shouldBe Some("text/html")
      charset(result)         shouldBe Some("utf-8")
      contentAsString(result) should include(messages("page.title.create_misc.h1"))
    }

    "return unauthorised when user does not have the necessary permissions" in {
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.VIEW_ASSIGNED_CASES))
          .get()(newFakeGETRequestWithCSRF(app))
      )

      status(result)               shouldBe Status.SEE_OTHER
      redirectLocation(result).get should include("unauthorized")
    }

    "display Misc details page if form has errors POST" in {
      given(casesService.createCase(any[CorrespondenceApplication], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(Cases.miscCaseExample))
      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .post()(
            newFakePOSTRequestWithCSRF(app)
              .withFormUrlEncodedBody(
                "detailedDescription" -> "",
                "contactName"  -> "",
                "caseType" -> "Other"
              )
          )
      )

      contentAsString(result) should include(messages("page.title.create_misc.h1"))

    }


    "display no results found when a queue is not found GET" in {
      given(casesService.createCase(any[MiscApplication], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(Cases.miscCaseExample.copy(queueId = Some("queue"))))
      given(casesService.getOne(any[String])(any[HeaderCarrier]))
        .willReturn(successful(Some(Cases.miscCaseExample.copy(queueId = Some("queue")))))

      given(queuesService.getOneById(any[String]))
        .willReturn(successful(None))

      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .displayConfirmation("reference")(newFakePOSTRequestWithCSRF(app))
      )

      status(result)               shouldBe Status.OK
      contentAsString(result) should include("Case Queue not found.")
    }

    "display no results found when a case is not found GET" in {
      given(casesService.createCase(any[MiscApplication], any[Operator])(any[HeaderCarrier]))
        .willReturn(successful(Cases.miscCaseExample.copy(queueId = Some("queue"))))
      given(casesService.getOne(any[String])(any[HeaderCarrier]))
        .willReturn(successful(None))

      given(queuesService.getOneById(any[String]))
        .willReturn(successful(None))

      val result = await(
        controller(caseWithStatusOPEN, Set(Permission.CREATE_CASES))
          .displayConfirmation("reference")(newFakePOSTRequestWithCSRF(app))
      )

      status(result)               shouldBe Status.OK
      contentAsString(result) should include("We could not find a Case with reference: reference")
    }

    def asFormParams(cc: Product): List[(String, String)] =
  cc.getClass.getDeclaredFields.toList
    .map { f =>
      f.setAccessible(true)
      (f.getName, f.get(cc))
    }
    .filterNot(_._1 == "serialVersionUID")
    .filterNot(_._1 == "MODULE$")
    .flatMap {
      case (n, l: List[_]) if l.headOption.exists(_.isInstanceOf[Product]) =>
        l.zipWithIndex.flatMap {
          case (x, i) => asFormParams(x.asInstanceOf[Product]).map { case (k, v) => (s"$n[$i].$k", v) }
        }
      case (n, Some(p: Product)) => asFormParams(p).map { case (k, v) => (s"$n.$k", v) }
      case (n, Some(a))          => List((n, a.toString))
      case (n, None)             => List((n, ""))
      case (n, p: Product)       => asFormParams(p).map { case (k, v) => (s"$n.$k", v) }
      case (n, a)                => List((n, a.toString))
    }
  }
}
