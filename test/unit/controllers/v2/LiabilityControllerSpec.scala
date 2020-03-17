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

package controllers.v2

import akka.stream.Materializer
import com.google.inject.Provider
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.{never, reset, verify}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.validation.{Constraint, Valid}
import play.api.http.Status
import play.api.mvc.{AnyContent, BodyParser, BodyParsers, MessagesControllerComponents, PlayBodyParsers, Request, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import config.AppConfig
import controllers.{AttachmentsController, ControllerCommons, ExistingCaseActionFactory, HaveRightPermissionsActionFactory, RequestActions, SuccessfulAuthenticatedAction, SuccessfulCasePermissionsAction, SuccessfulRequestActions}
import javax.inject.Inject
import models.forms.{CommodityCodeConstraints, DecisionForm}
import models.{Permission, _}
import models.request.AuthenticatedRequest
import play.api.Application
import play.api.http.MediaType.parse
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import service.{CasesService, FileStoreService}
import utils.Cases
import utils.Cases._
import play.api.inject.bind

import scala.concurrent.Future

class SuccessfulRequestActionsProvider @Inject() (implicit parse: BodyParsers.Default) extends Provider[SuccessfulRequestActions] {

  override def get(): SuccessfulRequestActions = {
    new SuccessfulRequestActions(parse, MockitoSugar.mock[Operator], c = Cases.btiCaseExample)
  }
}

class LiabilityControllerSpec extends UnitSpec with Matchers with BeforeAndAfterEach with GuiceOneAppPerSuite with MockitoSugar with ControllerCommons {

  override lazy val app: Application = new GuiceApplicationBuilder().overrides(
    bind[RequestActions].toProvider[SuccessfulRequestActionsProvider]
  ).build()

  "Calling /manage-tariff-classifications/cases/v2/:reference/liability " should {

    "return a 200 status" in {

      val result: Future[Result] = route(app, FakeRequest("GET", "/manage-tariff-classifications/cases/v2/123456/liability")).get

      status(result) shouldBe OK

      println(contentAsString(result))
    }


  }
}
