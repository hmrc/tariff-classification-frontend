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
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, FileStoreService, PdfService}
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.hmrc.tariffclassificationfrontend.views.html.templates.{application_template, ruling_template}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class PdfDownloadController @Inject()(authenticatedAction: AuthenticatedAction,
                                      override val messagesApi: MessagesApi,
                                      pdfService: PdfService,
                                      fileStore: FileStoreService,
                                      caseService: CasesService
                                     )(implicit appConfig: AppConfig) extends FrontendController with I18nSupport {


  def rulingPdf(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    caseService.getOne(reference) flatMap {
      case Some(c: Case) if c.decision.isDefined => generatePdf(ruling_template(c, c.decision.get), s"BTIRuling_$reference.pdf")
      case Some(c: Case) if c.decision.isEmpty =>  successful(Ok(views.html.ruling_not_found(reference)))
      case _ => successful(Ok(views.html.case_not_found(reference)))
    }
  }

  def applicationPdf(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    caseService.getOne(reference).flatMap {
      case Some(c) => {
        for {
          attachments <- fileStore.getAttachments(c)
          letter <- fileStore.getLetterOfAuthority(c)
          pdf <-  generatePdf(application_template(c, attachments, letter), s"BTIConfirmation$reference.pdf")
        } yield pdf
      }
      case _ => successful(Ok(views.html.case_not_found(reference)))
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
