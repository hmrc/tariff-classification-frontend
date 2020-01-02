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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import play.api.Application
import play.api.http.HeaderNames.LOCATION
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Result}
import play.api.test.{FakeHeaders, FakeRequest}
import play.filters.csrf.CSRF.{Token, TokenProvider}

trait ControllerCommons {

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
    val tokenProvider: TokenProvider = application.injector.instanceOf[TokenProvider]
    val csrfTags = Map(Token.NameRequestTag -> "csrfToken", Token.RequestTag -> tokenProvider.generateToken)
    FakeRequest("GET", "/", FakeHeaders(), AnyContentAsEmpty, tags = csrfTags)
  }

  protected def newFakePOSTRequestWithCSRF(application: Application, encodedBody: Map[String, String] = Map.empty): FakeRequest[AnyContentAsFormUrlEncoded] = {
    val tokenProvider: TokenProvider = application.injector.instanceOf[TokenProvider]
    val csrfTags = Map(Token.NameRequestTag -> "csrfToken", Token.RequestTag -> tokenProvider.generateToken)
    FakeRequest("POST", "/", FakeHeaders(), AnyContentAsFormUrlEncoded, tags = csrfTags).withFormUrlEncodedBody(encodedBody.toSeq: _*)
  }
}
