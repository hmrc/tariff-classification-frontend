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

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import com.google.inject.Provider
import controllers.{ControllerCommons, RequestActions, RequestActionsWithPermissions}
import javax.inject.Inject
import models.Case
import models.viewmodels.LiabilityViewModel
import org.mockito.ArgumentMatchers.{any, refEq, eq => meq}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Action, AnyContent, BodyParsers, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html
import service.FileStoreService
import utils.{Cases, Dates}
import views.html.v2.{case_heading, liability_view}

import scala.concurrent.Future

class RequestActionsWithPermissionsProvider @Inject()(implicit parse: BodyParsers.Default) extends Provider[RequestActionsWithPermissions] {

  override def get(): RequestActionsWithPermissions = {
    new RequestActionsWithPermissions(
      parse = parse,
      permissions = Cases.operatorWithoutPermissions.permissions,
      c = Cases.liabilityCaseExample
    )
  }
}

class LiabilityControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {

//  override lazy val app: Application = new GuiceApplicationBuilder().overrides(
//    bind[RequestActions].toProvider[RequestActionsWithPermissionsProvider],
//    bind[liability_view].toInstance(mock[liability_view]),
//    bind[case_heading].toInstance(mock[case_heading]),
//    bind[FileStoreService].toInstance(mock[FileStoreService])
//  ).configure(
//    "new-liability-details" -> true
//  ).build()

  override def beforeEach(): Unit = reset(inject[liability_view])

  "Calling /manage-tariff-classifications/cases/v2/:reference/liability " should {

    "return a 200 status" in {
      val expectedLiabilityViewModel = LiabilityViewModel.fromCase(Cases.liabilityCaseExample, Cases.operatorWithoutPermissions)
      val expectedC592TabViewModel = Cases.c592ViewModel.map(vm => vm.copy(entryDate = Dates.format(Instant.now())))
      val expectedAttachmentsTabViewModel = Cases.attachmentsTabViewModel.map(vm => vm.copy(attachments = Seq(Cases.storedAttachment),letter = Some(Cases.letterOfAuthority)))

      when(inject[FileStoreService].getAttachments(any[Case]())(any())) thenReturn(Future.successful(Seq(Cases.storedAttachment)))
      when(inject[FileStoreService].getLetterOfAuthority(any())(any())) thenReturn(Future.successful(Some(Cases.letterOfAuthority)))

      when(inject[liability_view].apply(
        meq(expectedLiabilityViewModel),
        meq(expectedC592TabViewModel),
        meq(expectedAttachmentsTabViewModel),
        any()
      )(any(), any(), any())) thenReturn Html("body")

      val result = inject[LiabilityController].displayLiability("123456").apply(inject[RequestActionsWithPermissionsProvider])

//      val fakeReq = FakeRequest("GET", "/manage-tariff-classifications/cases/v2/123456/liability")
//      val result: Future[Result] = route(app, fakeReq).get

      status(result) shouldBe OK

      verify(inject[liability_view], times(1)).apply(
        meq(expectedLiabilityViewModel),
        meq(expectedC592TabViewModel),
        meq(expectedAttachmentsTabViewModel),
        any()
      )(any(), any(), any())
    }
  }
}
