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

import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.HeaderNames.LOCATION
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Request, Result}
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.CSRFTokenHelper._
import play.mvc.BodyParser.AnyContent

trait ControllerCommons { self : GuiceOneAppPerSuite =>

//  override lazy val app = new GuiceApplicationBuilder().overrides().build()

  def inject[T](implicit m: Manifest[T]) = app.injector.instanceOf[T]

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

  protected def newFakePOSTRequestWithCSRF(application: Application, encodedBody: Map[String, String] = Map.empty): FakeRequest[AnyContentAsFormUrlEncoded] = {
    FakeRequest("POST", "/", FakeHeaders(Seq("csrfToken"->"csrfToken")), AnyContentAsFormUrlEncoded).withFormUrlEncodedBody(encodedBody.toSeq: _*)
      .withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsFormUrlEncoded]]
  }
}
