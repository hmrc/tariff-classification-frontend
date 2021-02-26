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

package controllers.v2

import controllers.{ControllerBaseSpec, RequestActionsWithPermissions}
import models._
import models.forms.v2.ChangeKeywordStatusForm
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.http.Status
import play.api.test.Helpers._
import service.CasesService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Cases
import views.html.managementtools.{change_keyword_status_view, manage_keywords_view}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.successful

class ManageKeywordsControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

  private lazy val manage_keywords_view    = injector.instanceOf[manage_keywords_view]
  private lazy val changeKeywordStatusView = injector.instanceOf[change_keyword_status_view]
  private val casesService                 = mock[CasesService]
  val form: Form[String]                   = ChangeKeywordStatusForm.form

  override protected def beforeEach(): Unit =
    reset(
      casesService
    )

  private def controller(permission: Set[Permission]) = new ManageKeywordsController(
    new RequestActionsWithPermissions(playBodyParsers, permission, addViewCasePermission = false),
    casesService,
    mcc,
    manage_keywords_view,
    changeKeywordStatusView,
    realAppConfig
  )

  "Manage keywords" should {

    "return 200 OK and HTML content type" in {
      val result = await(controller(Set(Permission.MANAGE_USERS)).displayManageKeywords()(fakeRequest))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {
      val result = await(controller(Set()).displayManageKeywords()(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }

  }

  "Change keyword status" should {
    val aCase = Cases.btiCaseExample.copy(reference = "reference")

    "return 200 OK and load the changeKeywordStatus form" in {
      when(casesService.getOne(any[String])(any[HeaderCarrier])).thenReturn(successful(Some(aCase)))

      val result =
        await(controller(Set(Permission.MANAGE_USERS)).changeKeywordStatus("reference")(newFakeGETRequestWithCSRF(app)))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")

    }

    "return unauthorised with no permissions" in {

      when(casesService.getOne(any[String])(any[HeaderCarrier])).thenReturn(successful(Some(aCase)))

      val result = await(controller(Set()).changeKeywordStatus("reference")(fakeRequest))
      status(result)           shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(controllers.routes.SecurityController.unauthorized.url)

    }
  }

}
