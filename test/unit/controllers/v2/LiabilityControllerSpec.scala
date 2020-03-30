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

import java.time.Instant

import com.google.inject.Provider
import controllers.{ControllerCommons, RequestActions, RequestActionsWithPermissions}
import javax.inject.Inject
import models.Case
import models.viewmodels.{AttachmentsTabViewModel, LiabilityViewModel}
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
import utils.{Cases, Dates}
import views.html.v2.{case_heading, liability_view}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.{any, eq => meq}
import play.twirl.api.Html
import service.FileStoreService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RequestActionsWithPermissionsProvider @Inject()(implicit parse: BodyParsers.Default) extends Provider[RequestActionsWithPermissions] {

  override def get(): RequestActionsWithPermissions = {
    new RequestActionsWithPermissions(parse, Cases.operatorWithoutPermissions.permissions, c = Cases.liabilityCaseExample)
  }
}

class LiabilityControllerSpec extends UnitSpec with Matchers with BeforeAndAfterEach with GuiceOneAppPerSuite with MockitoSugar with ControllerCommons {

  override lazy val app: Application = new GuiceApplicationBuilder().overrides(
    bind[RequestActions].toProvider[RequestActionsWithPermissionsProvider],
    bind[liability_view].toInstance(mock[liability_view]),
    bind[case_heading].toInstance(mock[case_heading]),
    bind[FileStoreService].toInstance(mock[FileStoreService])
  ).build()

  private implicit val hc: HeaderCarrier = HeaderCarrier()


  override def beforeEach(): Unit = reset(inject[liability_view])

  "Calling /manage-tariff-classifications/cases/v2/:reference/liability " should {

    "return a 200 status" in {
      val expectedLiabilityViewModel = LiabilityViewModel.fromCase(Cases.liabilityCaseExample, Cases.operatorWithoutPermissions)
      val expectedC592TabViewModel = Cases.c592ViewModel.map(vm => vm.copy(entryDate = Dates.format(Instant.now())))
      val expectedAttachmentsTabViewModel = Cases.attachmentsTabViewModel.map(vm => vm.copy(applicantFiles = Seq(Cases.storedAttachment),letter = Some(Cases.letterOfAuthority), nonApplicantFiles = Nil))

      when(inject[FileStoreService].getAttachments(any[Case]())(any())) thenReturn(Future.successful(Seq(Cases.storedAttachment)))
      when(inject[FileStoreService].getLetterOfAuthority(any())(any())) thenReturn(Future.successful(Some(Cases.letterOfAuthority)))

      when(inject[liability_view].apply(meq(expectedLiabilityViewModel), meq(expectedC592TabViewModel), meq(expectedAttachmentsTabViewModel))(any(), any(), any())) thenReturn Html("body")

      val result: Future[Result] = route(app, FakeRequest("GET", "/manage-tariff-classifications/cases/v2/123456/liability").withFormUrlEncodedBody()).get

      status(result) shouldBe OK

      verify(inject[liability_view], times(1)).apply(meq(expectedLiabilityViewModel), meq(expectedC592TabViewModel), meq(expectedAttachmentsTabViewModel))(any(), any(), any())
    }
  }
}
