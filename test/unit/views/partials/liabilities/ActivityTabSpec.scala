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

package views.partials.liabilities

import java.time.{ZoneOffset, ZonedDateTime}

import controllers.routes
import models._
import models.forms.{ActivityForm, ActivityFormData}
import models.viewmodels.ActivityViewModel
import play.api.data.Form
import utils.Cases._
import utils.{Cases, Events}
import views.ViewMatchers.{containElementWithID, containText, haveAttribute}
import views.ViewSpec
import views.html.partials.liabilities.activity_tab

class ActivityTabSpec extends ViewSpec {

  private val date = ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant

  private val queues = Seq(Queue(id = "1", slug = "test", name = "TEST"))

  private val activityForm: Form[ActivityFormData] = ActivityForm.form

  private val pagedEvent = Paged(Seq(Events.event), 1, 1, 1)

  private val activityViewModel = Cases.activityTabViewModelWithPermissions

  val requestWithMoveCasePermission = requestWithPermissions(Permission.MOVE_CASE_BACK_TO_QUEUE)

  val requestWithAddNotePermission = requestWithPermissions(Permission.ADD_NOTE)

  val requestWithViewCaseAssigneePermission = requestWithPermissions(Permission.VIEW_CASE_ASSIGNEE)

  val requestWithAddNoteViewCasePermission = requestWithPermissions(Permission.ADD_NOTE, Permission.VIEW_CASE_ASSIGNEE)

  def activityTab: activity_tab = injector.instanceOf[activity_tab]

  "Activity Tab" should {

    "display tab title" in {

      val doc = view(activityTab(activityViewModel, activityForm))

      doc should containText(messages("case.menu.activity"))
    }

    "display add-note-submit for user with correct permissions" in {

      val doc = view(activityTab(activityViewModel, activityForm)(requestWithAddNotePermission, messages, appConfig))

      doc should containElementWithID("add-note-submit")
    }

    "not display add-note-submit for user with correct permissions" in {

      val doc = view(activityTab(activityViewModel, activityForm)(operatorRequest, messages, appConfig))

      doc shouldNot containElementWithID("add-note-submit")
    }

    "Render 'Liability case created' for a liability case" in {
      val e = Event(
        id            = "EVENT_ID",
        details       = CaseCreated("Liability case created"),
        operator      = Operator("PID", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                  should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("PID")
      doc                                                  should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-title")    should containText("Liability case created")
      doc                                                  should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date")     should containText("01 Jan 2019")
    }

    "Render 'Status Change'" in {

      val e = Event(
        id = "EVENT_ID",
        details = CaseStatusChange(
          from         = CaseStatus.OPEN,
          to           = CaseStatus.COMPLETED,
          comment      = Some("comment"),
          attachmentId = Some("att-id")
        ),
        operator      = Operator("PID", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                  should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("PID")
      doc                                                  should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content")  should containText("Status changed from open to completed")
      doc                                                  should containElementWithID("activity-events-row-0-comment")
      doc.getElementById("activity-events-row-0-comment")  should containText("comment")
      doc                                                  should containElementWithID("activity-events-row-0-email_link")
      doc.getElementById("activity-events-row-0-email_link") should haveAttribute(
        "href",
        routes.ViewAttachmentController.get("att-id").url
      )
      doc                                              should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Cancellation Status Change'" in {

      val e = Event(
        id = "EVENT_ID",
        details = CancellationCaseStatusChange(
          from         = CaseStatus.OPEN,
          comment      = Some("comment"),
          attachmentId = Some("att-id"),
          reason       = CancelReason.INVALIDATED_CODE_CHANGE
        ),
        operator      = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                  should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("id")
      doc                                                  should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content")  should containText("Status changed from open to cancelled")
      doc                                                  should containElementWithID("activity-events-row-0-comment")
      doc.getElementById("activity-events-row-0-comment")  should containText("comment")
      doc                                                  should containElementWithID("activity-events-row-0-reason")
      doc.getElementById("activity-events-row-0-reason") should containText(
        CancelReason.format(CancelReason.INVALIDATED_CODE_CHANGE)
      )
      doc should containElementWithID("activity-events-row-0-email_link")
      doc.getElementById("activity-events-row-0-email_link") should haveAttribute(
        "href",
        routes.ViewAttachmentController.get("att-id").url
      )
      doc                                              should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Referral Status Change'" in {

      val e = Event(
        id = "EVENT_ID",
        details = ReferralCaseStatusChange(
          from         = CaseStatus.OPEN,
          comment      = Some("comment"),
          attachmentId = Some("att-id"),
          referredTo   = "Applicant",
          reason       = Seq(ReferralReason.REQUEST_SAMPLE, ReferralReason.REQUEST_MORE_INFO)
        ),
        operator      = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                  should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("id")
      doc                                                  should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content")  should containText("Status changed from open to referred")
      doc                                                  should containElementWithID("activity-events-row-0-comment")
      doc.getElementById("activity-events-row-0-comment")  should containText("comment")
      doc                                                  should containElementWithID("activity-events-row-0-reason_0")
      doc.getElementById("activity-events-row-0-reason_0") should containText(
        ReferralReason.format(ReferralReason.REQUEST_SAMPLE)
      )
      doc should containElementWithID("activity-events-row-0-reason_1")
      doc.getElementById("activity-events-row-0-reason_1") should containText(
        ReferralReason.format(ReferralReason.REQUEST_MORE_INFO)
      )
      doc should containElementWithID("activity-events-row-0-email_link")
      doc.getElementById("activity-events-row-0-email_link") should haveAttribute(
        "href",
        routes.ViewAttachmentController.get("att-id").url
      )
      doc                                              should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Completed Status Change'" when {
      "Email is present" in {

        val e = Event(
          id = "EVENT_ID",
          details =
            CompletedCaseStatusChange(from = CaseStatus.OPEN, comment = Some("comment"), email = Some("some email")),
          operator      = Operator("id", Some("name")),
          caseReference = "ref",
          timestamp     = date
        )

        val doc = view(
          activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
            requestWithAddNotePermission,
            messages,
            appConfig
          )
        )

        doc                                                  should containElementWithID("activity-events-row-0-operator")
        doc.getElementById("activity-events-row-0-operator") should containText("id")
        doc                                                  should containElementWithID("activity-events-row-0-content")
        doc.getElementById("activity-events-row-0-content")  should containText("Status changed from open to completed")
        doc                                                  should containElementWithID("activity-events-row-0-comment")
        doc.getElementById("activity-events-row-0-comment")  should containText("comment")
        doc                                                  should containElementWithID("activity-events-row-0-email")
        doc.getElementById("activity-events-row-0-email")    should containText("some email")
        doc                                                  should containElementWithID("activity-events-row-0-date")
        doc.getElementById("activity-events-row-0-date")     should containText("01 Jan 2019")
      }

      "Email is absent" in {

        val e = Event(
          id            = "EVENT_ID",
          details       = CompletedCaseStatusChange(from = CaseStatus.OPEN, comment = Some("comment"), email = None),
          operator      = Operator("id", Some("name")),
          caseReference = "ref",
          timestamp     = date
        )

        val doc = view(
          activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
            requestWithAddNotePermission,
            messages,
            appConfig
          )
        )

        doc                                                  should containElementWithID("activity-events-row-0-operator")
        doc.getElementById("activity-events-row-0-operator") should containText("id")
        doc                                                  should containElementWithID("activity-events-row-0-content")
        doc.getElementById("activity-events-row-0-content")  should containText("Status changed from open to completed")
        doc                                                  should containElementWithID("activity-events-row-0-comment")
        doc.getElementById("activity-events-row-0-comment")  should containText("comment")
        doc shouldNot containElementWithID("activity-events-row-0-email")
        doc                                              should containElementWithID("activity-events-row-0-date")
        doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
      }
    }

    "Render 'Appeal Added'" in {

      val e = Event(
        id = "EVENT_ID",
        details = AppealAdded(
          appealType   = AppealType.APPEAL_TIER_1,
          appealStatus = AppealStatus.IN_PROGRESS,
          comment      = Some("comment")
        ),
        operator      = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                  should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("id")
      doc                                                  should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText(
        "Appeal added with type Appeal tier 1 and status Under appeal"
      )
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc                                                 should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date")    should containText("01 Jan 2019")
    }

    "Render 'Review Added'" in {

      val e = Event(
        id = "EVENT_ID",
        details = AppealAdded(
          appealType   = AppealType.REVIEW,
          appealStatus = AppealStatus.IN_PROGRESS,
          comment      = Some("comment")
        ),
        operator      = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                  should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("id")
      doc                                                  should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content")  should containText("Review added with status Under review")
      doc.getElementById("activity-events-row-0-content")  should containText("comment")
      doc                                                  should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date")     should containText("01 Jan 2019")
    }

    "Render 'Appeal Status Changed'" in {
      val e = Event(
        id = "EVENT_ID",
        details = AppealStatusChange(
          appealType = AppealType.APPEAL_TIER_1,
          from       = AppealStatus.IN_PROGRESS,
          to         = AppealStatus.ALLOWED,
          comment    = Some("comment")
        ),
        operator      = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                  should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("id")
      doc                                                  should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText(
        "Appeal tier 1 status changed from Under appeal to Appeal allowed"
      )
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc                                                 should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date")    should containText("01 Jan 2019")
    }

    "Render 'Expert advice received'" in {
      val e = Event(
        id            = "EVENT_ID",
        details       = ExpertAdviceReceived(comment = "advice paragraph"),
        operator      = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                  should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("id")
      doc                                                  should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content")  should containText("Expert advice received")
      doc.getElementById("activity-events-row-0-content")  should containText("advice paragraph")
      doc                                                  should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date")     should containText("01 Jan 2019")
    }

    "Render 'Extended Use Change'" in {

      val e = Event(
        id            = "EVENT_ID",
        details       = ExtendedUseStatusChange(from = false, to = true, comment = Some("comment")),
        operator      = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                  should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("id")
      doc                                                  should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText(
        "Application for extended use status changed from No to Yes"
      )
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc                                                 should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date")    should containText("01 Jan 2019")
    }

    "Render 'Assignment Change'" in {
      val e = Event(
        id            = "EVENT_ID",
        details       = AssignmentChange(from = None, to = None, comment = Some("comment")),
        operator      = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                  should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("id")
      doc.getElementById("activity-events-row-0-content")  should containText("comment")
      doc                                                  should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date")     should containText("01 Jan 2019")
    }

    "Render 'Assignment Change' from Some to Some" in {
      val e = Event(
        id = "EVENT_ID",
        details = AssignmentChange(
          from    = Some(Operator("from", Some("FROM"))),
          to      = Some(Operator("to", Some("TO"))),
          comment = Some("comment")
        ),
        operator      = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                 should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Case reassigned from FROM to TO")
    }

    "Render 'Assignment Change' from Some to None" in {
      val e = Event(
        id            = "EVENT_ID",
        details       = AssignmentChange(from = Some(Operator("from", Some("FROM"))), to = None, comment = Some("comment")),
        operator      = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                 should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Case unassigned from FROM")
    }

    "Render 'Assignment Change' from None to Some" in {
      val e = Event(
        id            = "EVENT_ID",
        details       = AssignmentChange(from = None, to = Some(Operator("to", Some("TO"))), comment = Some("comment")),
        operator      = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                 should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Case assigned to TO")
    }

    "Render 'Assignment Change' from None to None" in {
      val e = Event(
        id            = "EVENT_ID",
        details       = AssignmentChange(from = None, to = None, comment = Some("comment")),
        operator      = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp     = date
      )

      val doc = view(
        activityTab(activityViewModel.copy(events = Paged(Seq(e))), activityForm)(
          requestWithAddNotePermission,
          messages,
          appConfig
        )
      )

      doc                                                 should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Case assignment changed")
    }

    "Not render assigned information if does not have right permissions" in {
      val doc = view(activityTab(activityViewModel, activityForm)(operatorRequest, messages, appConfig))

      doc shouldNot containElementWithID("activity-events-assignee")
      doc shouldNot containElementWithID("activity-events-assignee-label")
    }

    "Render assigned to 'You'" in {
      val doc = view(
        activityTab(activityViewModel.copy(assignee = Some(authenticatedOperator)), activityForm)(
          requestWithAddNoteViewCasePermission,
          messages,
          appConfig
        )
      )

      doc                                                         should containElementWithID("activity-events-assignee")
      doc.getElementById("activity-events-assignee").text()       shouldBe "You"
      doc.getElementById("activity-events-assignee-label").text() shouldBe "Currently assigned to:"
    }

    "Render assigned to name" in {
      val doc = view(
        activityTab(
          activityViewModel.copy(assignee = Some(authenticatedOperator.copy(id = "id", name = Some("name")))),
          activityForm
        )(requestWithAddNoteViewCasePermission, messages, appConfig)
      )

      doc                                                         should containElementWithID("activity-events-assignee")
      doc.getElementById("activity-events-assignee").text()       shouldBe "name"
      doc.getElementById("activity-events-assignee-label").text() shouldBe "Currently assigned to:"
    }

    "Render assigned to PID" in {
      val doc = view(
        activityTab(activityViewModel.copy(assignee = Some(authenticatedOperator.copy(id = "id"))), activityForm)(
          requestWithAddNoteViewCasePermission,
          messages,
          appConfig
        )
      )

      doc                                                         should containElementWithID("activity-events-assignee")
      doc.getElementById("activity-events-assignee").text()       shouldBe "PID id"
      doc.getElementById("activity-events-assignee-label").text() shouldBe "Currently assigned to:"
    }

    "Render currently in 'TEST'" in {
      val c = aLiabilityCase(
        withAssignee(None),
        withQueue("1")
      )

      val doc = view(
        activityTab(ActivityViewModel.fromCase(c, pagedEvent, queues), activityForm)(
          requestWithViewCaseAssigneePermission,
          messages,
          appConfig
        )
      )

      doc                                                               should containElementWithID("activity-events-assigned-queue")
      doc.getElementById("activity-events-assigned-queue").text()       shouldBe "TEST"
      doc.getElementById("activity-events-assigned-queue-label").text() shouldBe "Currently in:"
    }

    "Render currently in 'Gateway'" in {

      val doc = view(
        activityTab(activityViewModel.copy(assignee = None, queueId = None), activityForm)(
          requestWithPermissions(Permission.VIEW_CASE_ASSIGNEE),
          messages,
          appConfig
        )
      )

      doc                                                               should containElementWithID("activity-events-assigned-queue")
      doc.getElementById("activity-events-assigned-queue").text()       shouldBe "Gateway"
      doc.getElementById("activity-events-assigned-queue-label").text() shouldBe "Currently in:"
    }

    "Render currently in 'unknown'" in {

      val c = aLiabilityCase(
        withAssignee(None),
        withQueue("99")
      )

      val doc = view(
        activityTab(ActivityViewModel.fromCase(c, pagedEvent, queues), activityForm)(
          requestWithPermissions(Permission.VIEW_CASE_ASSIGNEE),
          messages,
          appConfig
        )
      )

      doc                                                               should containElementWithID("activity-events-assigned-queue")
      doc.getElementById("activity-events-assigned-queue").text()       shouldBe "unknown"
      doc.getElementById("activity-events-assigned-queue-label").text() shouldBe "Currently in:"
    }
  }
}
