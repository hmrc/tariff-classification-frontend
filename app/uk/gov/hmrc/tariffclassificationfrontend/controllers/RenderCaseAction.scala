/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.models.request.{AuthenticatedCaseRequest, AuthenticatedRequest}
import uk.gov.hmrc.tariffclassificationfrontend.service.CasesService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

trait RenderCaseAction extends FrontendController with I18nSupport {

  protected implicit val config: AppConfig
  protected val caseService: CasesService

  protected def redirect: String => Call

  protected def isValidCase(c: Case)(implicit request: AuthenticatedRequest[_]): Boolean

  protected def getCaseAndRenderView(caseReference: String, toHtml: Case => Future[HtmlFormat.Appendable])
                                    (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {

    request.`case` match {
      case c: Case if isValidCase(c)(request) => toHtml(c).map(Ok(_))
      case _ => successful(Redirect(redirect(caseReference)))
    }
  }

  protected def defaultRedirect(reference : Option[String] = None) (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    successful(Redirect(redirect(reference.getOrElse(request.`case`.reference))))
  }

  protected def validateAndRedirect(toHtml: Case => Future[Call])
                                     (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {

    if (isValidCase(request.`case`)(request)) {
      toHtml(request.`case`).map(Redirect)
    } else {
      defaultRedirect()
    }
  }

  protected def validateAndRenderView(toHtml: Case => Future[HtmlFormat.Appendable])
                                     (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {

    if (isValidCase(request.`case`)(request)) {
      toHtml(request.`case`).map(Ok(_))
    } else {
      defaultRedirect()
    }
  }

  protected def renderView(valid: Case => Boolean, toHtml: Case => Future[HtmlFormat.Appendable])
                          (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {
    if (valid(request.`case`)) {
      toHtml(request.`case`).map(Ok(_))
    } else {
      defaultRedirect()
    }
  }


  protected def getCaseAndRespond(caseReference: String,
                                  toResult: Case => Future[Result])
                                 (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {

    request.`case` match {
      case c: Case if isValidCase(c)(request) => toResult(c)
      case _ => defaultRedirect(Some(caseReference))
    }
  }


  protected def validateAndRespond(toResult: Case => Future[Result])
                                  (implicit request: AuthenticatedCaseRequest[_]): Future[Result] = {

    if (isValidCase(request.`case`)(request)) {
      toResult(request.`case`)
    } else {
      defaultRedirect()
    }
  }

}
