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

import com.google.inject.Provider
import controllers.{ControllerCommons, RequestActions, RequestActionsWithPermissions, SuccessfulRequestActions}
import javax.inject.Inject
import models.{Paged, Queue}
import models.forms.{ActivityForm, ActivityFormData}
import models.viewmodels.LiabilityViewModel
import org.scalatest.{BeforeAndAfterEach, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{BodyParsers, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec
import utils.{Cases, Events}
import views.html.v2.{case_heading, liability_view}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.{any, eq => meq}
import play.api.data.Form
import play.twirl.api.Html

import scala.concurrent.Future

class RequestActionsWithPermissionsProvider @Inject()(implicit parse: BodyParsers.Default) extends Provider[RequestActionsWithPermissions] {

  override def get(): RequestActionsWithPermissions = {
    new RequestActionsWithPermissions(parse, Cases.operatorWithoutPermissions.permissions, c = Cases.liabilityCaseExample)
  }
}

class LiabilityControllerSpec extends UnitSpec with Matchers with BeforeAndAfterEach with GuiceOneAppPerSuite with MockitoSugar with ControllerCommons {
  private val activityForm: Form[ActivityFormData] = ActivityForm.form
  private val pagedEvent = Paged(Seq(Events.event), 1, 1, 1)
  private val queues =  Seq(Queue("", "", ""))

  override lazy val app: Application = new GuiceApplicationBuilder().overrides(
    bind[RequestActions].toProvider[RequestActionsWithPermissionsProvider],
    bind[liability_view].toInstance(mock[liability_view]),
    bind[case_heading].toInstance(mock[case_heading])
  ).build()

  override def beforeEach(): Unit = reset(inject[liability_view])

  "Calling /manage-tariff-classifications/cases/v2/:reference/liability " should {

    "return a 200 status" in {
      val expected = LiabilityViewModel.fromCase(Cases.liabilityCaseExample, Cases.operatorWithoutPermissions)

      when(inject[liability_view].apply(meq(expected), meq(pagedEvent), meq(activityForm), meq(queues))(any(), any(), any())) thenReturn Html("body")

      val result: Future[Result] = route(app, FakeRequest("GET", "/manage-tariff-classifications/cases/v2/123456/liability").withFormUrlEncodedBody()).get

      status(result) shouldBe OK

      verify(inject[liability_view], times(1)).apply(meq(expected), meq(pagedEvent), meq(activityForm), meq(queues))(any(), any(), any())
    }
  }
}
