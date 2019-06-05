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

package uk.gov.hmrc.tariffclassificationfrontend.service

import java.time.LocalDate
import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.audit.AuditService
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.connector.{BindingTariffClassificationConnector, RulingConnector}
import uk.gov.hmrc.tariffclassificationfrontend.models.AppealStatus.AppealStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.AppealType.AppealType
import uk.gov.hmrc.tariffclassificationfrontend.models.CancelReason.CancelReason
import uk.gov.hmrc.tariffclassificationfrontend.models.ReferralReason.ReferralReason
import uk.gov.hmrc.tariffclassificationfrontend.models.SampleReturn.SampleReturn
import uk.gov.hmrc.tariffclassificationfrontend.models.SampleStatus.SampleStatus
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CasesService @Inject()(appConfig: AppConfig,
                             auditService: AuditService,
                             emailService: EmailService,
                             fileService: FileStoreService,
                             reportingService: ReportingService,
                             connector: BindingTariffClassificationConnector,
                             rulingConnector: RulingConnector) {

  def updateExtendedUseStatus(original: Case, status: Boolean, operator: Operator)
                             (implicit hc: HeaderCarrier): Future[Case] = {
    val decision = original.decision.getOrElse(throw new IllegalArgumentException("Cannot change the Extended Use status of a case without a Decision"))
    val cancellation = decision.cancellation.getOrElse(throw new IllegalArgumentException("Cannot change the Extended Use status of a case without a Cancellation"))
    val updatedDecision = decision.copy(cancellation = Some(cancellation.copy(applicationForExtendedUse = status)))

    for {
      updated <- connector.updateCase(original.copy(decision = Some(updatedDecision)))
      _ <- addExtendedUseStatusChangeEvent(original, updated, operator)
      _ = auditService.auditCaseExtendedUseChange(original, updated, operator)
    } yield updated
  }

  def addAppeal(original: Case, appealType: AppealType, appealStatus: AppealStatus, operator: Operator)(implicit hc: HeaderCarrier): Future[Case] = {
    val decision = original.decision.getOrElse(throw new IllegalArgumentException("Cannot change the Appeal state of a case without a Decision"))
    val appeal = Appeal(
      id = UUID.randomUUID().toString,
      status = appealStatus,
      `type` = appealType
    )
    for {
      updated <- connector.updateCase(original.copy(decision = Some(decision.copy(appeal = decision.appeal :+ appeal))))
      _ <- addAppealAddedEvent(original, updated, appeal, operator)
      _ = auditService.auditCaseAppealAdded(updated, appeal, operator)
    } yield updated
  }

  def updateAppealStatus(original: Case, existingAppeal: Appeal, appealStatus: AppealStatus, operator: Operator)(implicit hc: HeaderCarrier): Future[Case] = {
    val decision = original.decision.getOrElse(throw new IllegalArgumentException("Cannot change the Appeal state of a case without a Decision"))

    val newAppeals: Seq[Appeal] = decision.appeal.map {
      case appeal: Appeal if appeal.equals(existingAppeal) => appeal.copy(status = appealStatus)
      case other: Appeal => other
    }

    for {
      updated <- connector.updateCase(original.copy(decision = Some(decision.copy(appeal = newAppeals))))
      _ <- addAppealStatusChangedEvent(original, updated, existingAppeal, appealStatus, operator)
      _ = auditService.auditCaseAppealStatusChange(updated, existingAppeal, appealStatus, operator)
    } yield updated
  }

  def updateSampleStatus(original: Case, status: Option[SampleStatus], operator: Operator)
                        (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      updated <- connector.updateCase(original.copy(sample = original.sample.copy(status = status)))
      _ <- addSampleStatusChangeEvent(original, updated, operator)
      _ = auditService.auditSampleStatusChange(original, updated, operator)
    } yield updated
  }

  def updateSampleReturn(original: Case, status: Option[SampleReturn], operator: Operator)
                        (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      updated <- connector.updateCase(original.copy(sample = original.sample.copy(returnStatus = status)))
      _ <- addSampleReturnChangeEvent(original, updated, operator)
      _ = auditService.auditSampleReturnChange(original, updated, operator)
    } yield updated
  }


  def assignCase(original: Case, operator: Operator)
                (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      updated <- connector.updateCase(original.copy(assignee = Some(operator)))
      _ <- addAssignmentChangeEvent(original, updated, operator)
      _ = auditService.auditOperatorAssigned(updated, operator)
    } yield updated
  }

  def reassignCase(original: Case, queue: Queue, operator: Operator)
                  (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      updated <- connector.updateCase(
        original.copy(queueId = Some(queue.id), assignee = None)
      )
      _ <- addQueueChangeEvent(original, updated, operator)
      _ <- addAssignmentChangeEvent(original, updated, operator)
      _ = auditService.auditQueueReassigned(updated, operator, queue)
      _ = auditService.auditOperatorAssigned(updated, operator)
    } yield updated
  }

  def releaseCase(original: Case, queue: Queue, operator: Operator)
                 (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      updated <- connector.updateCase(original.copy(status = CaseStatus.OPEN, queueId = Some(queue.id)))
      _ <- addStatusChangeEvent(original, updated, operator, None)
      _ = auditService.auditCaseReleased(original, updated, queue, operator)
    } yield updated
  }

  def reopenCase(original: Case, operator: Operator)
                (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      updated <- connector.updateCase(original.copy(status = CaseStatus.OPEN))
      _ <- addStatusChangeEvent(original, updated, operator, None)
      _ = auditService.auditCaseReOpened(original, updated, operator)
    } yield updated
  }

  def referCase(original: Case, referredTo : String, reason: Seq[ReferralReason], f: FileUpload, note: String, operator: Operator)
               (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      fileStored <- fileService.upload(fileUpload = f)
      attachment = Attachment(id = fileStored.id, operator = Some(operator))
      updated <- connector.updateCase(original.addAttachment(attachment).copy(status = CaseStatus.REFERRED))
      _ <- addReferStatusChangeEvent(original, updated, operator, Some(note), referredTo, reason, Some(attachment))
      _ = auditService.auditCaseReferred(original, updated, operator)
    } yield updated
  }

  def rejectCase(original: Case, f: FileUpload, note: String, operator: Operator)
                (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      fileStored <- fileService.upload(fileUpload = f)
      attachment = Attachment(id = fileStored.id, operator = Some(operator))
      updated <- connector.updateCase(original.addAttachment(attachment).copy(status = CaseStatus.REJECTED))
      _ <- addStatusChangeEvent(original, updated, operator, Some(note), Some(attachment))
      _ = auditService.auditCaseRejected(original, updated, operator)
    } yield updated
  }

  def suspendCase(original: Case, fileUpload: FileUpload, note: String, operator: Operator)
                 (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      fileStored <- fileService.upload(fileUpload)
      attachment = Attachment(id = fileStored.id, operator = Some(operator))
      updated <- connector.updateCase(original.addAttachment(attachment).copy(status = CaseStatus.SUSPENDED))
      _ <- addStatusChangeEvent(original, updated, operator, Some(note), Some(attachment))
      _ = auditService.auditCaseSuspended(original, updated, operator)
    } yield updated
  }

  def suppressCase(original: Case, fileUpload: FileUpload, note: String, operator: Operator)
                  (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      fileStored <- fileService.upload(fileUpload)
      attachment = Attachment(id = fileStored.id, operator = Some(operator))
      updated <- connector.updateCase(original.addAttachment(attachment).copy(status = CaseStatus.SUPPRESSED))
      _ <- addStatusChangeEvent(original, updated, operator, Some(note), Some(attachment))
      _ = auditService.auditCaseSuppressed(original, updated, operator)
    } yield updated
  }

  def completeCase(original: Case, operator: Operator)
                  (implicit hc: HeaderCarrier): Future[Case] = {
    val date = LocalDate.now(appConfig.clock).atStartOfDay(appConfig.clock.getZone)
    val startInstant = date.toInstant
    val endInstant = date.plusYears(appConfig.decisionLifetimeYears).toInstant

    val decisionUpdating: Decision = original.decision
      .getOrElse(throw new IllegalArgumentException("Cannot Complete a Case without a Decision"))
      .copy(effectiveStartDate = Some(startInstant), effectiveEndDate = Some(endInstant))
    val caseUpdating = original.copy(status = CaseStatus.COMPLETED, decision = Some(decisionUpdating))

    for {
      // Update the case
      updated: Case <- connector.updateCase(caseUpdating)

      // Send the email
      message <- emailService.sendCaseCompleteEmail(updated)
        .map { email: EmailTemplate =>
          s"- Subject: ${email.subject}\n- Body: ${email.plain}"
        } recover {
        case t: Throwable =>
          Logger.error("Failed to send email", t)
          "Attempted to send an email to the applicant which failed"
      }

      // Create the event
      _ <- addCompletedEvent(original, updated, operator, None, message)

      // Audit
      _ = auditService.auditCaseCompleted(original, updated, operator)

      // Notify the Ruling store
      _ = rulingConnector.notify(original.reference) recover loggingARulingErrorFor(original.reference)
    } yield updated
  }

  def cancelRuling(original: Case, reason: CancelReason, f: FileUpload, note: String, operator: Operator)
                  (implicit hc: HeaderCarrier): Future[Case] = {
    val updatedEndDate = LocalDate.now(appConfig.clock).atStartOfDay(appConfig.clock.getZone)

    val decisionUpdating: Decision = original.decision
      .getOrElse(throw new IllegalArgumentException("Cannot Cancel a Case without a Decision"))
      .copy(
        effectiveEndDate = Some(updatedEndDate.toInstant),
        cancellation = Some(Cancellation(reason = reason))
      )

    for {
      // Store file
      fileStored <- fileService.upload(fileUpload = f)
      // Create attachment
      attachment = Attachment(id = fileStored.id, operator = Some(operator))
      // Update the case
      updated: Case <- connector.updateCase( original.addAttachment(attachment).copy(status = CaseStatus.CANCELLED, decision = Some(decisionUpdating)))
      // Create the event
      _ <- addCancelStatusChangeEvent(original, updated, operator, Some(note), reason, Some(attachment))
      // Audit
      _ = auditService.auditRulingCancelled(original, updated, operator)

      // Notify the Ruling store
      _ = rulingConnector.notify(original.reference) recover loggingARulingErrorFor(original.reference)
    } yield updated
  }

  def getOne(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] = {
    connector.findCase(reference)
  }

  def search(search: Search, sort: Sort, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] = {
    connector.search(search, sort, pagination)
  }

  def getCasesByQueue(queue: Queue, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] = {
    connector.findCasesByQueue(queue, pagination)
  }

  def countCasesByQueue(operator: Operator)(implicit hc: HeaderCarrier): Future[Map[String, Int]] = {
    for {
      countMyCases <- getCasesByAssignee(operator, NoPagination())
      countByQueue <- reportingService.getQueueReport
      casesByQueueAndMyCases = countByQueue.map(report => (
        report.group.getOrElse(Queues.gateway.id), report.value.size))
        .toMap + ("my-cases" -> countMyCases.size)
    } yield casesByQueueAndMyCases
  }

  def getCasesByAssignee(assignee: Operator, pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] = {
    connector.findCasesByAssignee(assignee, pagination)
  }

  def getAssignedCases(pagination: Pagination)(implicit hc: HeaderCarrier): Future[Paged[Case]] = {
    connector.findAssignedCases(pagination)
  }

  def updateCase(caseToUpdate: Case)(implicit hc: HeaderCarrier): Future[Case] = {
    connector.updateCase(caseToUpdate)
  }

  def addAttachment(c: Case, f: FileUpload, o: Operator)(implicit headerCarrier: HeaderCarrier): Future[Case] = {
    fileService.upload(f) flatMap { fileStored: FileStoreAttachment =>
      val attachment = Attachment(id = fileStored.id, operator = Some(o))
      connector.updateCase(c.addAttachment(attachment))
    }
  }

  def removeAttachment(c: Case, fileId: String)(implicit headerCarrier: HeaderCarrier): Future[Case] = {
    fileService.removeAttachment(fileId) flatMap {_ =>
      connector.updateCase(c.copy(attachments = c.attachments.filter(_.id != fileId)))
    }
  }

  private def addCompletedEvent(original: Case,
                                   updated: Case,
                                   operator: Operator,
                                   comment: Option[String],
                                    email: String)
                                  (implicit hc: HeaderCarrier): Future[Unit] = {
    val details = CompletedCaseStatusChange(from = original.status, comment = comment, email = email)
    addEvent(original, updated, details, operator)
  }

  private def addStatusChangeEvent(original: Case,
                                   updated: Case,
                                   operator: Operator,
                                   comment: Option[String],
                                   attachment: Option[Attachment] = None)
                                  (implicit hc: HeaderCarrier): Future[Unit] = {
    val details = CaseStatusChange(from = original.status, to = updated.status, comment = comment, attachmentId = attachment.map(_.id))
    addEvent(original, updated, details, operator)
  }

  private def addCancelStatusChangeEvent(original: Case,
                                         updated: Case,
                                         operator: Operator,
                                         comment: Option[String],
                                         reason: CancelReason,
                                         attachment: Option[Attachment] = None)
                                        (implicit hc: HeaderCarrier): Future[Unit] = {
    val details = CancellationCaseStatusChange(from = original.status, reason = reason, comment = comment, attachmentId = attachment.map(_.id) )
    addEvent(original, updated, details, operator)
  }

  private def addReferStatusChangeEvent(original: Case,
                                         updated: Case,
                                         operator: Operator,
                                         comment: Option[String],
                                         referredTo: String,
                                         reason: Seq[ReferralReason],
                                         attachment: Option[Attachment] = None)
                                        (implicit hc: HeaderCarrier): Future[Unit] = {
    val details = ReferralCaseStatusChange(from = original.status, comment = comment, attachmentId = attachment.map(_.id), referredTo = referredTo, reason = reason )
    addEvent(original, updated, details, operator)
  }

  private def addSampleStatusChangeEvent(original: Case, updated: Case, operator: Operator, comment: Option[String] = None)
                                           (implicit hc: HeaderCarrier): Future[Unit] = {
    val details = SampleStatusChange(original.sample.status, updated.sample.status, comment)
    addEvent(original, updated, details, operator)
  }

  private def addSampleReturnChangeEvent(original: Case, updated: Case, operator: Operator, comment: Option[String] = None)
                                        (implicit hc: HeaderCarrier): Future[Unit] = {
    val details = SampleReturnChange(original.sample.returnStatus, updated.sample.returnStatus, comment)
    addEvent(original, updated, details, operator)
  }

  private def addQueueChangeEvent(original: Case, updated: Case, operator: Operator, comment: Option[String] = None)
                                 (implicit hc: HeaderCarrier): Future[Unit] = {
    val details = QueueChange(original.queueId, updated.queueId, comment)
    addEvent(original, updated, details, operator)
  }

  private def addAppealAddedEvent(original: Case, updated: Case, appeal: Appeal, operator: Operator, comment: Option[String] = None)
                                        (implicit hc: HeaderCarrier): Future[Unit] = {
    val details = AppealAdded(appeal.`type`, appeal.status, comment)
    addEvent(original, updated, details, operator)
  }

  private def addAppealStatusChangedEvent(original: Case, updated: Case, appeal: Appeal, newStatus: AppealStatus, operator: Operator, comment: Option[String] = None)
                                 (implicit hc: HeaderCarrier): Future[Unit] = {
    val details = AppealStatusChange(appeal.`type`, appeal.status, newStatus, comment)
    addEvent(original, updated, details, operator)
  }

  private def addAssignmentChangeEvent(original: Case, updated: Case, operator: Operator, comment: Option[String] = None)
                                      (implicit hc: HeaderCarrier): Future[Unit] = {
    val details = AssignmentChange(original.assignee, updated.assignee, comment)
    addEvent(original, updated, details, operator)
  }

  private def addExtendedUseStatusChangeEvent(original: Case, updated: Case, operator: Operator, comment: Option[String] = None)
                                             (implicit hc: HeaderCarrier): Future[Unit] = {
    val details = ExtendedUseStatusChange(extendedUseStatus(original), extendedUseStatus(updated), comment)
    addEvent(original, updated, details, operator)
  }

  private def extendedUseStatus: Case => Boolean = {
    _.decision.flatMap(_.cancellation).map(_.applicationForExtendedUse).get
  }

  private def addEvent(original: Case, updated: Case, details: Details, operator: Operator)
                      (implicit hc: HeaderCarrier): Future[Unit] = {
    val event = NewEventRequest(details, operator)
    connector.createEvent(updated, event) recover {
      case t: Throwable => Logger.error(s"Could not create Event for case [${original.reference}] with payload [$event]", t)
    } map (_ => ())
  }

  private def loggingARulingErrorFor(reference: String): PartialFunction[Throwable, Unit] = {
    case t: Throwable => Logger.error(s"Failed to notify the ruling store for case $reference", t)
  }

}
