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

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, PdfService}
import uk.gov.hmrc.tariffclassificationfrontend.views.html.templates.ruling

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PdfDownloadController @Inject()(appConfig: AppConfig,
                                      authenticatedAction: AuthenticatedAction,
                                      override val messagesApi: MessagesApi,
                                      pdfService: PdfService,
                                      caseService: CasesService
                                     ) extends FrontendController with I18nSupport {

  def rulingPdf(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    caseService.getOne(reference) flatMap {
      case Some(c: Case) if c.decision.isDefined => generatePdf(
        // TODO: c.decision is inside c
        ruling(appConfig, c, c.decision.getOrElse(throw new IllegalStateException("Missing decision"))), s"BTIRuling$reference.pdf"
      )
//      case _ => // TODO
    }
  }

  private def generatePdf(htmlContent: Html, filename: String): Future[Result] = {
    pdfService.generatePdf(htmlContent) map { pdfFile =>
      Results.Ok(pdfFile.content)
        .as(pdfFile.contentType)
        .withHeaders(CONTENT_DISPOSITION -> s"filename=$filename")
    }
  }

}
