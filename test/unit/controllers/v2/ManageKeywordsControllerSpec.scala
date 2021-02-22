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
import play.api.http.Status
import play.api.test.Helpers._
import service.ManageKeywordsService
import views.html.managementtools.{confirm_keyword_created, manage_keywords_view, new_keyword_view}

import scala.concurrent.ExecutionContext.Implicits.global

class ManageKeywordsControllerSpec extends ControllerBaseSpec {

  private lazy val manage_keywords_view = injector.instanceOf[manage_keywords_view]
  private lazy val confirm_keyword_view = injector.instanceOf[confirm_keyword_created]
  private lazy val new_keyword_view = injector.instanceOf[new_keyword_view]
  private lazy val keywordService = injector.instanceOf[ManageKeywordsService]

  private def controller(permission: Set[Permission]) = new ManageKeywordsController(
    new RequestActionsWithPermissions(playBodyParsers, permission, addViewCasePermission = false),
    mcc,
    keywordService,
    manage_keywords_view,
    confirm_keyword_view,
    new_keyword_view,
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

}
