/*
 * Copyright 2025 HM Revenue & Customs
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

package services

import audit.AuditService
import config.AppConfig
import connectors.{BindingTariffClassificationConnector, RulingConnector}
import models.Queue
import views.html.templates._

trait CasesServiceSpecBase extends ServiceSpecBase {

  protected val config: AppConfig = mock[AppConfig]
  protected val queue: Queue      = mock[Queue]

  protected val audit: AuditService                = mock[AuditService]
  protected val emailService: EmailService         = mock[EmailService]
  protected val fileStoreService: FileStoreService = mock[FileStoreService]
  protected val countriesService: CountriesService =
    mock[CountriesService] //used to be injector.instanceOf for CasesService_CompleteCaseSpec
  protected val reportingService: ReportingService              = mock[ReportingService]
  protected val pdfService: PdfGeneratorService                 = mock[PdfGeneratorService]
  protected val connector: BindingTariffClassificationConnector = mock[BindingTariffClassificationConnector]
  protected val rulingConnector: RulingConnector                = mock[RulingConnector]

  protected val cover_letter_template: cover_letter_template = mock[cover_letter_template]
  protected val ruling_template: ruling_template             = mock[ruling_template]
  protected val decision_template: decision_template         = mock[decision_template]

  private def service(appConfig: AppConfig) =
    new CasesService(
      audit,
      emailService,
      fileStoreService,
      countriesService,
      reportingService,
      pdfService,
      connector,
      rulingConnector,
      cover_letter_template,
      ruling_template,
      decision_template
    )(executionContext, appConfig)

  protected val serviceMockConfig: CasesService = service(config)
  protected val service: CasesService           = service(realAppConfig)
}
