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

import config.AppConfig
import javax.inject.Inject
import models.Case
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import service.{CasesService, CountriesService, FileStoreService, PdfService}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import views.html.templates.{application_template, decision_template, ruling_template}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

class PdfDownloadController @Inject()(
  authenticatedAction: AuthenticatedAction,
  mcc: MessagesControllerComponents,
  pdfService: PdfService,
  fileStore: FileStoreService,
  caseService: CasesService,
  countriesService: CountriesService,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc) with I18nSupport {

  def getRulingPdf(reference: String): Action[AnyContent] = authenticatedAction.async { implicit request =>
    caseService.getOne(reference) flatMap {
      case Some(c: Case) if c.decision.isDefined && c.application.isBTI => generatePdf(ruling_template(c, c.decision.get, getCountryName), s"BTIRuling_$reference.pdf")
      case Some(c: Case) if c.decision.isDefined && c.application.isLiabilityOrder => generatePdf(decision_template(c, c.decision.get), s"LiabilityDecision_$reference.pdf")
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
          pdf <-  generatePdf(application_template(c, attachments, letter, getCountryName), s"BTIConfirmation$reference.pdf")
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

  def getCountryName(code: String) = countriesService.getAllCountries.find(_.code == code).map(_.countryName)

}
