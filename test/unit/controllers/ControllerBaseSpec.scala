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

import base.SpecBase
import controllers.v2.RequestActionsWithPermissionsProvider
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.HeaderNames.LOCATION
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContent, AnyContentAsEmpty, AnyContentAsFormUrlEncoded, AnyContentAsText, Request, Result}
import play.api.test.{FakeHeaders, FakeRequest}
import service.{CasesService, EventsService, FileStoreService, QueuesService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import views.html.partials.liabilities.{attachments_details, attachments_list}
import views.html.v2.{case_heading, liability_view, remove_attachment}
import play.api.test.CSRFTokenHelper._

class ControllerBaseSpec extends SpecBase {

//  override lazy val app: Application = new GuiceApplicationBuilder().overrides(
//    //providers
//    bind[RequestActions].toProvider[RequestActionsWithPermissionsProvider],
//    //views
//    bind[liability_view].toInstance(mock[liability_view]),
//    bind[case_heading].toInstance(mock[case_heading]),
//    bind[attachments_details].toInstance(mock[attachments_details]),
//    bind[remove_attachment].toInstance(mock[remove_attachment]),
//    bind[attachments_list].toInstance(mock[attachments_list]),
//    //services
//    bind[FileStoreService].toInstance(mock[FileStoreService]),
//    bind[EventsService].toInstance(mock[EventsService]),
//    bind[QueuesService].toInstance(mock[QueuesService]),
//    bind[CasesService].toInstance(mock[CasesService])
//  ).configure(
//    "metrics.jvm" -> false,
//    "metrics.enabled" -> false,
//    "toggle.new-liability-details" -> true
//  ).build()


  protected def locationOf(result: Result): Option[String] = {
    result.header.headers.get(LOCATION)
  }

  protected def contentTypeOf(result: Result): Option[String] = {
    result.body.contentType.map(_.split(";").take(1).mkString.trim)
  }

  protected def charsetOf(result: Result): Option[String] = {
    result.body.contentType match {
      case Some(s) if s.contains("charset=") => Some(s.split("; *charset=").drop(1).mkString.trim)
      case _ => None
    }
  }

  protected def newFakeGETRequestWithCSRF(application: Application): FakeRequest[AnyContentAsEmpty.type] = {
    FakeRequest("GET", "/", FakeHeaders(Seq("csrfToken"->"csrfToken")), AnyContentAsEmpty)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
  }

  protected def newFakePOSTRequestWithCSRF(application: Application, body: String): FakeRequest[AnyContentAsText] = {
    FakeRequest("POST", "/", FakeHeaders(Seq("csrfToken"->"csrfToken")), AnyContentAsText).withTextBody(body)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsText]]
  }

  protected def newFakePOSTRequestWithCSRF(application: Application, encodedBody: Map[String, String] = Map.empty): FakeRequest[AnyContentAsFormUrlEncoded] = {
    FakeRequest("POST", "/", FakeHeaders(Seq("csrfToken"->"csrfToken")), AnyContentAsFormUrlEncoded).withFormUrlEncodedBody(encodedBody.toSeq: _*)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsFormUrlEncoded]]
  }
}
