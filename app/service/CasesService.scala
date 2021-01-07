/*
 * Copyright 2021 HM Revenue & Customs
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

package service

import java.time.{Instant, LocalDate}
import java.util.UUID
import audit.AuditService
import config.AppConfig
import connector.{BindingTariffClassificationConnector, RulingConnector}

import java.nio.file.{Files, StandardOpenOption}
import javax.inject.{Inject, Singleton}
import models.ApplicationType._
import models.AppealStatus.AppealStatus
import models.AppealType.AppealType
import models.CancelReason.CancelReason
import models.ReferralReason.ReferralReason
import models.SampleReturn.SampleReturn
import models.SampleStatus.SampleStatus
import models._
import models.request.NewEventRequest
import play.api.Logging
import play.api.i18n.Messages
import play.api.libs.Files.SingletonTemporaryFileCreator
import uk.gov.hmrc.http.HeaderCarrier
import views.html.templates.{decision_template, ruling_template}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CasesService @Inject() (
  appConfig: AppConfig,
  auditService: AuditService,
  emailService: EmailService,
  fileService: FileStoreService,
  countriesService: CountriesService,
  reportingService: ReportingService,
  pdfService: PdfService,
  connector: BindingTariffClassificationConnector,
  rulingConnector: RulingConnector
)(implicit ec: ExecutionContext)
    extends Logging {

  def updateExtendedUseStatus(original: Case, status: Boolean, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Future[Case] = {
    val decision = original.decision.getOrElse(
      throw new IllegalArgumentException("Cannot change the Extended Use status of a case without a Decision")
    )
    val cancellation = decision.cancellation.getOrElse(
      throw new IllegalArgumentException("Cannot change the Extended Use status of a case without a Cancellation")
    )
    val updatedDecision = decision.copy(cancellation = Some(cancellation.copy(applicationForExtendedUse = status)))

    for {
      updated <- connector.updateCase(original.copy(decision = Some(updatedDecision)))
      _       <- addExtendedUseStatusChangeEvent(original, updated, operator)
      _ = auditService.auditCaseExtendedUseChange(original, updated, operator)
    } yield updated
  }

  def addAppeal(original: Case, appealType: AppealType, appealStatus: AppealStatus, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Future[Case] = {
    val decision = original.decision.getOrElse(
      throw new IllegalArgumentException("Cannot change the Appeal state of a case without a Decision")
    )
    val appeal = Appeal(
      id     = UUID.randomUUID().toString,
      status = appealStatus,
      `type` = appealType
    )
    for {
      updated <- connector.updateCase(original.copy(decision = Some(decision.copy(appeal = decision.appeal :+ appeal))))
      _       <- addAppealAddedEvent(original, updated, appeal, operator)
      _ = auditService.auditCaseAppealAdded(updated, appeal, operator)
    } yield updated
  }

  def updateAppealStatus(original: Case, existingAppeal: Appeal, appealStatus: AppealStatus, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Future[Case] = {
    val decision = original.decision.getOrElse(
      throw new IllegalArgumentException("Cannot change the Appeal state of a case without a Decision")
    )

    val newAppeals: Seq[Appeal] = decision.appeal.map {
      case appeal: Appeal if appeal.equals(existingAppeal) => appeal.copy(status = appealStatus)
      case other: Appeal                                   => other
    }

    for {
      updated <- connector.updateCase(original.copy(decision = Some(decision.copy(appeal = newAppeals))))
      _       <- addAppealStatusChangedEvent(original, updated, existingAppeal, appealStatus, operator)
      _ = auditService.auditCaseAppealStatusChange(updated, existingAppeal, appealStatus, operator)
    } yield updated
  }

  def updateSampleStatus(original: Case, status: Option[SampleStatus], operator: Operator)(
    implicit hc: HeaderCarrier
  ): Future[Case] =
    for {
      updated <- connector.updateCase(original.copy(sample = original.sample.copy(status = status)))
      _       <- addSampleStatusChangeEvent(original, updated, operator)
      _ = auditService.auditSampleStatusChange(original, updated, operator)
    } yield updated

  def updateSampleReturn(original: Case, status: Option[SampleReturn], operator: Operator)(
    implicit hc: HeaderCarrier
  ): Future[Case] =
    for {
      updated <- connector.updateCase(original.copy(sample = original.sample.copy(returnStatus = status)))
      _       <- addSampleReturnChangeEvent(original, updated, operator)
      _ = auditService.auditSampleReturnChange(original, updated, operator)
    } yield updated

  def assignCase(original: Case, operator: Operator)(implicit hc: HeaderCarrier): Future[Case] =
    for {
      updated <- connector.updateCase(original.copy(assignee = Some(operator)))
      _       <- addAssignmentChangeEvent(original, updated, operator)
      _ = auditService.auditOperatorAssigned(updated, operator)
    } yield updated

  def reassignCase(original: Case, queue: Queue, operator: Operator)(implicit hc: HeaderCarrier): Future[Case] =
    for {
      updated <- connector.updateCase(
                  original.copy(queueId = Some(queue.id), assignee = None)
                )
      _ <- addQueueChangeEvent(original, updated, operator)
      _ <- addAssignmentChangeEvent(original, updated, operator)
      _ = auditService.auditQueueReassigned(updated, operator, queue)
      _ = auditService.auditOperatorAssigned(updated, operator)
    } yield updated

  def releaseCase(original: Case, queue: Queue, operator: Operator)(implicit hc: HeaderCarrier): Future[Case] =
    for {
      updated <- connector.updateCase(original.copy(status = CaseStatus.OPEN, queueId = Some(queue.id)))
      _       <- addStatusChangeEvent(original, updated, operator, None)
      _ = auditService.auditCaseReleased(original, updated, queue, operator)
    } yield updated

  def reopenCase(original: Case, operator: Operator)(implicit hc: HeaderCarrier): Future[Case] =
    for {
      updated <- connector.updateCase(original.copy(status = CaseStatus.OPEN))
      _       <- addStatusChangeEvent(original, updated, operator, None)
      _ = auditService.auditCaseReOpened(original, updated, operator)
    } yield updated

  def referCase(
    original: Case,
    referredTo: String,
    reason: Seq[ReferralReason],
    f: FileUpload,
    note: String,
    operator: Operator
  )(implicit hc: HeaderCarrier): Future[Case] =
    for {
      fileStored <- fileService.upload(fileUpload = f)
      attachment = Attachment(id = fileStored.id, operator = Some(operator))
      updated <- connector.updateCase(
                  original
                    .addAttachment(attachment)
                    .copy(
                      status = CaseStatus.REFERRED,
                      sample = if (reason.contains(ReferralReason.REQUEST_SAMPLE)) {
                        Sample(Some(SampleStatus.AWAITING), Some(operator), Some(SampleReturn.TO_BE_CONFIRMED))
                      } else {
                        original.sample
                      }
                    )
                )
      _ <- addReferStatusChangeEvent(original, updated, operator, Some(note), referredTo, reason, Some(attachment))
      _ = auditService.auditCaseReferred(original, updated, operator)
      _ = processChangedSampleStatus(original, updated, operator)
    } yield updated

  private def processChangedSampleStatus(original: Case, updated: Case, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Unit =
    if (updated.sample.status != original.sample.status) {
      addSampleStatusChangeEvent(original, updated, operator)
      auditService.auditSampleStatusChange(original, updated, operator)
    }

  def rejectCase(original: Case, f: FileUpload, note: String, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Future[Case] =
    for {
      fileStored <- fileService.upload(fileUpload = f)
      attachment = Attachment(id = fileStored.id, operator = Some(operator))
      updated <- connector.updateCase(original.addAttachment(attachment).copy(status = CaseStatus.REJECTED))
      _       <- addStatusChangeEvent(original, updated, operator, Some(note), Some(attachment))
      _ = auditService.auditCaseRejected(original, updated, operator)
    } yield updated

  def suspendCase(original: Case, fileUpload: FileUpload, note: String, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Future[Case] =
    for {
      fileStored <- fileService.upload(fileUpload)
      attachment = Attachment(id = fileStored.id, operator = Some(operator))
      updated <- connector.updateCase(original.addAttachment(attachment).copy(status = CaseStatus.SUSPENDED))
      _       <- addStatusChangeEvent(original, updated, operator, Some(note), Some(attachment))
      _ = auditService.auditCaseSuspended(original, updated, operator)
    } yield updated

  def suppressCase(original: Case, fileUpload: FileUpload, note: String, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Future[Case] =
    for {
      fileStored <- fileService.upload(fileUpload)
      attachment = Attachment(id = fileStored.id, operator = Some(operator))
      updated <- connector.updateCase(original.addAttachment(attachment).copy(status = CaseStatus.SUPPRESSED))
      _       <- addStatusChangeEvent(original, updated, operator, Some(note), Some(attachment))
      _ = auditService.auditCaseSuppressed(original, updated, operator)
    } yield updated

  def completeCase(original: Case, operator: Operator)(implicit hc: HeaderCarrier, messages: Messages): Future[Case] = {
    val date         = LocalDate.now(appConfig.clock).atStartOfDay(appConfig.clock.getZone)
    val startInstant = date.toInstant
    val possibleEndInstant: Option[Instant] = original.application.isBTI match {
      case true =>
        Some(date.plusYears(appConfig.decisionLifetimeYears).minusDays(appConfig.decisionLifetimeDays).toInstant)
      case _ => None
    }

    val decisionUpdating: Decision = original.decision
      .getOrElse(throw new IllegalArgumentException("Cannot Complete a Case without a Decision"))
      .copy(effectiveStartDate = Some(startInstant), effectiveEndDate = possibleEndInstant)
    val caseUpdating = original.copy(status = CaseStatus.COMPLETED, decision = Some(decisionUpdating))

    def sendCaseCompleteEmail(updated: Case): Future[Option[String]] =
      emailService
        .sendCaseCompleteEmail(updated)
        .map { email: EmailTemplate =>
          Some(s"- Subject: ${email.subject}\n- Body: ${email.plain}")
        } recover {
        case t: Throwable =>
          logger.error("Failed to send email", t)
          Some("Attempted to send an email to the applicant which failed")
      }

    def getCountryName(code: String): Option[String] =
      countriesService.getAllCountriesById.get(code).map(_.countryName)

    def createRulingPdf(pdf: PdfFile): FileUpload = {
      val tempFile = SingletonTemporaryFileCreator.create(original.reference, "pdf")
      Files.write(tempFile.path, pdf.content, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
      FileUpload(tempFile, s"ATaRRuling_${original.reference}.pdf", pdf.contentType)
    }

    def createLiabilityDecisionPdf(pdf: PdfFile): FileUpload = {
      val tempFile = SingletonTemporaryFileCreator.create(original.reference, "pdf")
      Files.write(tempFile.path, pdf.content, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
      FileUpload(tempFile, s"LiabilityDecision_${original.reference}.pdf", pdf.contentType)
    }

    for {
      // Generate the decision PDF
      pdfFile <- caseUpdating.application.`type` match {
                  case ATAR =>
                    pdfService
                      .generatePdf(ruling_template(caseUpdating, decisionUpdating, getCountryName)(messages, appConfig))
                      .map(createRulingPdf(_))
                  case LIABILITY =>
                    pdfService
                      .generatePdf(decision_template(caseUpdating, decisionUpdating)(messages, appConfig))
                      .map(createLiabilityDecisionPdf(_))
                }

      // Upload the decision PDF to the filestore
      pdfStored <- fileService.upload(pdfFile)

      pdfAttachment = Attachment(id = pdfStored.id, operator = Some(operator))

      caseWithPdf = caseUpdating.copy(decision = Some(decisionUpdating.copy(decisionPdf = Some(pdfAttachment))))

      // Update the case
      updated: Case <- connector.updateCase(caseWithPdf)

      // Send the email
      message: Option[String] <- if (original.application.isBTI) sendCaseCompleteEmail(updated)
                                else Future.successful(None)

      // Create the event
      _ <- addCompletedEvent(original, updated, operator, None, message)

      // Audit
      _ = auditService.auditCaseCompleted(original, updated, operator)

      // Notify the Ruling store
      _ = if (original.application.isBTI)
        rulingConnector.notify(original.reference) recover loggingARulingErrorFor(original.reference)
    } yield updated
  }

  def cancelRuling(original: Case, reason: CancelReason, f: FileUpload, note: String, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Future[Case] = {
    val updatedEndDate = LocalDate.now(appConfig.clock).atStartOfDay(appConfig.clock.getZone)

    val decisionUpdating: Decision = original.decision
      .getOrElse(throw new IllegalArgumentException("Cannot Cancel a Case without a Decision"))
      .copy(
        effectiveEndDate = Some(updatedEndDate.toInstant),
        cancellation     = Some(Cancellation(reason = reason))
      )

    for {
      // Store file
      fileStored <- fileService.upload(fileUpload = f)
      // Create attachment
      attachment = Attachment(id = fileStored.id, operator = Some(operator))
      // Update the case
      updated: Case <- connector.updateCase(
                        original
                          .addAttachment(attachment)
                          .copy(status = CaseStatus.CANCELLED, decision = Some(decisionUpdating))
                      )
      // Create the event
      _ <- addCancelStatusChangeEvent(original, updated, operator, Some(note), reason, Some(attachment))
      // Audit
      _ = auditService.auditRulingCancelled(original, updated, operator)

      // Notify the Ruling store
      _ = rulingConnector.notify(original.reference) recover loggingARulingErrorFor(original.reference)
    } yield updated
  }

  def getOne(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] =
    connector.findCase(reference)

  def search(search: Search, sort: Sort, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    connector.search(search, sort, pagination)

  def getCasesByQueue(
    queue: Queue,
    pagination: Pagination,
    forTypes: Seq[ApplicationType] =
      Seq(ApplicationType.ATAR, ApplicationType.LIABILITY, ApplicationType.CORRESPONDENCE)
  )(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    connector.findCasesByQueue(queue, pagination, forTypes)

  def getCasesByAllQueues(
    queue: Seq[Queue],
    pagination: Pagination,
    forTypes: Seq[ApplicationType] =
      Seq(ApplicationType.ATAR, ApplicationType.LIABILITY, ApplicationType.CORRESPONDENCE)
  )(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    connector.findCasesByAllQueues(queue, pagination, forTypes)

  def countCasesByQueue(operator: Operator)(implicit hc: HeaderCarrier): Future[Map[String, Int]] =
    for {
      countMyCases <- getCasesByAssignee(operator, NoPagination())
      countByQueue <- reportingService.getQueueReport
      casesByQueueAndMyCases = countByQueue
        .map(report =>
          (
            report.group.getOrElse(CaseReportGroup.QUEUE, Some(Queues.gateway.id)).getOrElse("") + "-" + report.group
              .getOrElse(CaseReportGroup.APPLICATION_TYPE, Some(""))
              .get,
            report.value.size
          )
        )
        .toMap + ("my-cases" -> countMyCases.size)
    } yield casesByQueueAndMyCases

  def getCasesByAssignee(assignee: Operator, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    connector.findCasesByAssignee(assignee, pagination)

  def getAssignedCases(pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] =
    connector.findAssignedCases(pagination)

  def updateCase(caseToUpdate: Case)(implicit hc: HeaderCarrier): Future[Case] =
    connector.updateCase(caseToUpdate)

  def createCase(application: Application, operator: Operator)(implicit hc: HeaderCarrier): Future[Case] =
    for {
      caseCreated <- connector.createCase(application)
      _           <- addCaseCreatedEvent(caseCreated, operator)
      _ = auditService.auditCaseCreated(caseCreated, operator)
    } yield caseCreated

  def addAttachment(c: Case, f: FileUpload, o: Operator)(implicit headerCarrier: HeaderCarrier): Future[Case] =
    fileService.upload(f) flatMap { fileStored: FileStoreAttachment =>
      val attachment = Attachment(id = fileStored.id, public = true, operator = Some(o))
      connector.updateCase(c.addAttachment(attachment))
    }

  def removeAttachment(c: Case, fileId: String)(implicit headerCarrier: HeaderCarrier): Future[Case] =
    fileService.removeAttachment(fileId) flatMap { _ =>
      connector.updateCase(c.copy(attachments = c.attachments.filter(_.id != fileId)))
    }

  def addMessage(original: Case, message: Message, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Future[Case] = {
    val applicationToUpdate = original.application.asCorrespondence
      .copy(messagesLogged = message :: original.application.asCorrespondence.messagesLogged)
    val caseToUpdate = original.copy(application = applicationToUpdate)
    for {
      updated <- connector.updateCase(caseToUpdate)
      _ = auditService.auditAddMessage(updated, operator)
    } yield updated
  }

  private def addCompletedEvent(
    original: Case,
    updated: Case,
    operator: Operator,
    comment: Option[String],
    email: Option[String]
  )(implicit hc: HeaderCarrier): Future[Unit] = {
    val details = CompletedCaseStatusChange(from = original.status, comment = comment, email = email)
    addEvent(original, updated, details, operator)
  }

  private def addStatusChangeEvent(
    original: Case,
    updated: Case,
    operator: Operator,
    comment: Option[String],
    attachment: Option[Attachment] = None
  )(implicit hc: HeaderCarrier): Future[Unit] = {
    val details = CaseStatusChange(
      from         = original.status,
      to           = updated.status,
      comment      = comment,
      attachmentId = attachment.map(_.id)
    )
    addEvent(original, updated, details, operator)
  }

  private def addCancelStatusChangeEvent(
    original: Case,
    updated: Case,
    operator: Operator,
    comment: Option[String],
    reason: CancelReason,
    attachment: Option[Attachment]
  )(implicit hc: HeaderCarrier): Future[Unit] = {
    val details = CancellationCaseStatusChange(
      from         = original.status,
      reason       = reason,
      comment      = comment,
      attachmentId = attachment.map(_.id)
    )
    addEvent(original, updated, details, operator)
  }

  private def addReferStatusChangeEvent(
    original: Case,
    updated: Case,
    operator: Operator,
    comment: Option[String],
    referredTo: String,
    reason: Seq[ReferralReason],
    attachment: Option[Attachment]
  )(implicit hc: HeaderCarrier): Future[Unit] = {
    val details = ReferralCaseStatusChange(
      from         = original.status,
      comment      = comment,
      attachmentId = attachment.map(_.id),
      referredTo   = referredTo,
      reason       = reason
    )
    addEvent(original, updated, details, operator)
  }

  private def addSampleStatusChangeEvent(
    original: Case,
    updated: Case,
    operator: Operator,
    comment: Option[String] = None
  )(implicit hc: HeaderCarrier): Future[Unit] = {
    val details = SampleStatusChange(original.sample.status, updated.sample.status, comment)
    addEvent(original, updated, details, operator)
  }

  private def addSampleReturnChangeEvent(
    original: Case,
    updated: Case,
    operator: Operator,
    comment: Option[String] = None
  )(implicit hc: HeaderCarrier): Future[Unit] = {
    val details = SampleReturnChange(original.sample.returnStatus, updated.sample.returnStatus, comment)
    addEvent(original, updated, details, operator)
  }

  private def addQueueChangeEvent(original: Case, updated: Case, operator: Operator, comment: Option[String] = None)(
    implicit hc: HeaderCarrier
  ): Future[Unit] = {
    val details = QueueChange(original.queueId, updated.queueId, comment)
    addEvent(original, updated, details, operator)
  }

  private def addAppealAddedEvent(
    original: Case,
    updated: Case,
    appeal: Appeal,
    operator: Operator,
    comment: Option[String] = None
  )(implicit hc: HeaderCarrier): Future[Unit] = {
    val details = AppealAdded(appeal.`type`, appeal.status, comment)
    addEvent(original, updated, details, operator)
  }

  private def addAppealStatusChangedEvent(
    original: Case,
    updated: Case,
    appeal: Appeal,
    newStatus: AppealStatus,
    operator: Operator,
    comment: Option[String] = None
  )(implicit hc: HeaderCarrier): Future[Unit] = {
    val details = AppealStatusChange(appeal.`type`, appeal.status, newStatus, comment)
    addEvent(original, updated, details, operator)
  }

  private def addAssignmentChangeEvent(
    original: Case,
    updated: Case,
    operator: Operator,
    comment: Option[String] = None
  )(implicit hc: HeaderCarrier): Future[Unit] =
    (original.assignee, updated.assignee) match {
      case (None, None) => Future.successful(())
      case _            => addEvent(original, updated, AssignmentChange(original.assignee, updated.assignee, comment), operator)
    }

  private def addExtendedUseStatusChangeEvent(
    original: Case,
    updated: Case,
    operator: Operator,
    comment: Option[String] = None
  )(implicit hc: HeaderCarrier): Future[Unit] = {
    val details = ExtendedUseStatusChange(extendedUseStatus(original), extendedUseStatus(updated), comment)
    addEvent(original, updated, details, operator)
  }

  private def addCaseCreatedEvent(liabilityCase: Case, operator: Operator)(implicit hc: HeaderCarrier): Future[Unit] = {
    val details = CaseCreated("Liability case created")
    addEvent(liabilityCase, liabilityCase, details, operator)
  }

  private def extendedUseStatus: Case => Boolean =
    _.decision.flatMap(_.cancellation).map(_.applicationForExtendedUse).get

  private def addEvent(original: Case, updated: Case, details: Details, operator: Operator)(
    implicit hc: HeaderCarrier
  ): Future[Unit] = {
    val event = NewEventRequest(details, operator)
    connector.createEvent(updated, event) recover {
      case t: Throwable =>
        logger.error(s"Could not create Event for case [${original.reference}] with payload [${event.details}]", t)
    } map (_ => ())
  }

  private def loggingARulingErrorFor(reference: String): PartialFunction[Throwable, Unit] = {
    case t: Throwable => logger.error(s"Failed to notify the ruling store for case $reference", t)
  }

}
