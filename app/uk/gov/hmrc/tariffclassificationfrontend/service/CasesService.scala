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
import uk.gov.hmrc.tariffclassificationfrontend.models.SampleStatus.SampleStatus
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.models.request.NewEventRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.successful

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

  def updateSampleStatus(original: Case, status: Option[SampleStatus], operator: Operator)
                        (implicit hc: HeaderCarrier): Future[Case] = {

    for {
      updated <- connector.updateCase(original.copy(sampleStatus = status))
      _ <- addSampleStatusChangeEvent(original, updated, operator)
      _ = auditService.auditSampleStatusChange(original, updated, operator)
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
      _ <- addStatusChangeEvent(original, updated, operator)
      _ = auditService.auditCaseReleased(original, updated, queue, operator)
    } yield updated
  }

  def reopenCase(original: Case, operator: Operator)
                (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      updated <- connector.updateCase(original.copy(status = CaseStatus.OPEN))
      _ <- addStatusChangeEvent(original, updated, operator)
      _ = auditService.auditCaseReOpened(original, updated, operator)
    } yield updated
  }

  def referCase(original: Case, operator: Operator)
               (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      updated <- connector.updateCase(original.copy(status = CaseStatus.REFERRED))
      _ <- addStatusChangeEvent(original, updated, operator)
      _ = auditService.auditCaseReferred(original, updated, operator)
    } yield updated
  }

  def rejectCase(original: Case, operator: Operator)
                (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      updated <- connector.updateCase(original.copy(status = CaseStatus.REJECTED))
      _ <- addStatusChangeEvent(original, updated, operator)
      _ = auditService.auditCaseRejected(original, updated, operator)
    } yield updated
  }

  def suspendCase(original: Case, operator: Operator)
                 (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      updated <- connector.updateCase(original.copy(status = CaseStatus.SUSPENDED))
      _ <- addStatusChangeEvent(original, updated, operator)
      _ = auditService.auditCaseSuspended(original, updated, operator)
    } yield updated
  }

  def suppressCase(original: Case, operator: Operator)
                  (implicit hc: HeaderCarrier): Future[Case] = {
    for {
      updated <- connector.updateCase(original.copy(status = CaseStatus.SUPPRESSED))
      _ <- addStatusChangeEvent(original, updated, operator)
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
          s"The applicant was sent an Email:\n- Subject: ${email.subject}\n- Body: ${email.plain}"
        } recover {
        case t: Throwable =>
          Logger.error("Failed to send email", t)
          "Attempted to send an email to the applicant which failed"
      }

      // Create the event
      _ <- addStatusChangeEvent(original, updated, operator, Some(message))

      // Audit
      _ = auditService.auditCaseCompleted(original, updated, operator)

      // Notify the Ruling store
      _ = rulingConnector.notify(original.reference) recover loggingARulingErrorFor(original.reference)
    } yield updated
  }

  def cancelRuling(original: Case, reason: CancelReason, operator: Operator)
                  (implicit hc: HeaderCarrier): Future[Case] = {
    val updatedEndDate = LocalDate.now(appConfig.clock).atStartOfDay(appConfig.clock.getZone)

    val decisionUpdating: Decision = original.decision
      .getOrElse(throw new IllegalArgumentException("Cannot Cancel a Case without a Decision"))
      .copy(
        effectiveEndDate = Some(updatedEndDate.toInstant),
        cancellation = Some(Cancellation(reason = reason))
      )
    val caseUpdating = original.copy(status = CaseStatus.CANCELLED, decision = Some(decisionUpdating))

    for {
      // Update the case
      updated: Case <- connector.updateCase(caseUpdating)
      // Create the event
      _ <- addStatusChangeEvent(original, updated, operator, comment = Some(CancelReason.format(reason)))
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
      val attachments = c.attachments :+ Attachment(id = fileStored.id, operator = Some(o))
      connector.updateCase(c.copy(attachments = attachments))
    }
  }

  def removeAttachment(c: Case, fileId: String)(implicit headerCarrier: HeaderCarrier): Future[Case] = {
    fileService.removeAttachment(fileId) flatMap {_ =>
      connector.updateCase(c.copy(attachments = c.attachments.filter(_.id != fileId)))
    }
  }

  private def addStatusChangeEvent(original: Case, updated: Case, operator: Operator, comment: Option[String] = None)
                                  (implicit hc: HeaderCarrier): Future[Unit] = {
    val details = CaseStatusChange(original.status, updated.status, comment)
    addEvent(original, updated, details, operator)
  }

  private def addSampleStatusChangeEvent(original: Case, updated: Case, operator: Operator, comment: Option[String] = None)
                                        (implicit hc: HeaderCarrier): Future[Unit] = {
    val details = SampleStatusChange(original.sampleStatus, updated.sampleStatus, comment)
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
