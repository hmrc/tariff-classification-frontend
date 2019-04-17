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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials

import java.time.{ZoneOffset, ZonedDateTime}

import uk.gov.hmrc.tariffclassificationfrontend.forms.ActivityForm
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, _}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.activity_details
import uk.gov.tariffclassificationfrontend.utils.Cases._

class ActivityDetailsViewSpec extends ViewSpec {

  private val date = ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant
  private val queues = Seq(Queue(id = "1", slug = "test", name = "TEST"))

  "Activity Details" should {

    "Render empty events - showing 'Application Submitted'" in {
      // Given
      val c = aCase(
        withCreatedDate(date)
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-application_submitted")
      doc should containElementWithID("activity-events-row-application_submitted-content")
      doc should containElementWithID("activity-events-row-application_submitted-date")
      doc.getElementById("activity-events-row-application_submitted-date") should containText("01 Jan 2019")
    }

    "Render event without operator name" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = Note("comment"),
        operator = Operator("id", None),
        caseReference = "ref"
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("Unknown")
    }

    "Render 'Note'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = Note("comment"),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Status Change'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = CaseStatusChange(from = CaseStatus.OPEN, to = CaseStatus.COMPLETED, comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Status changed from open to completed")
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Appeal Change'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = AppealStatusChange(from = None, to = Some(AppealStatus.IN_PROGRESS), comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Appeal status changed from None to Under appeal")
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Review Change'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = ReviewStatusChange(from = None, to = Some(ReviewStatus.IN_PROGRESS), comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Review status changed from None to Under review")
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Extended Use Change'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = ExtendedUseStatusChange(from = false, to = true, comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Application for extended use status changed from No to Yes")
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Assignment Change'" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = AssignmentChange(from = None, to = None, comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc.getElementById("activity-events-row-0-content") should containText("comment")
      doc should containElementWithID("activity-events-row-0-date")
      doc.getElementById("activity-events-row-0-date") should containText("01 Jan 2019")
    }

    "Render 'Assignment Change' from Some to Some" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = AssignmentChange(from = Some(Operator("from", Some("FROM"))), to = Some(Operator("to", Some("TO"))), comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Case reassigned from FROM to TO")
    }

    "Render 'Assignment Change' from Some to None" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = AssignmentChange(from = Some(Operator("from", Some("FROM"))), to = None, comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Case unassigned from FROM")
    }

    "Render 'Assignment Change' from None to Some" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = AssignmentChange(from = None, to = Some(Operator("to", Some("TO"))), comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Case assigned to TO")
    }

    "Render 'Assignment Change' from None to None" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = AssignmentChange(from = None, to = None, comment = Some("comment")),
        operator = Operator("id", Some("name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Case unassigned")
    }

    "Render assigned to 'You'" in {
      // Given
      val c = aCase(
        withAssignee(Some(authenticatedOperator))
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-assignee")
      doc.getElementById("activity-events-assignee").text() shouldBe "You"
      doc.getElementById("activity-events-assignee-label").text() shouldBe "Currently assigned to:"
    }

    "Render assigned to name" in {
      // Given
      val c = aCase(
        withAssignee(Some(Operator("id", Some("name"))))
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-assignee")
      doc.getElementById("activity-events-assignee").text() shouldBe "name"
      doc.getElementById("activity-events-assignee-label").text() shouldBe "Currently assigned to:"
    }

    "Render assigned to PID" in {
      // Given
      val c = aCase(
        withAssignee(Some(Operator("id")))
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-assignee")
      doc.getElementById("activity-events-assignee").text() shouldBe "PID id"
      doc.getElementById("activity-events-assignee-label").text() shouldBe "Currently assigned to:"
    }

    "Render currently in 'TEST'" in {
      // Given
      val c = aCase(
        withAssignee(None),
        withQueue("1")
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-assigned-queue")
      doc.getElementById("activity-events-assigned-queue").text() shouldBe "TEST"
      doc.getElementById("activity-events-assigned-queue-label").text() shouldBe "Currently in:"
    }

    "Render currently in 'Gateway'" in {
      // Given
      val c = aCase(
        withAssignee(None)
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-assigned-queue")
      doc.getElementById("activity-events-assigned-queue").text() shouldBe "Gateway"
      doc.getElementById("activity-events-assigned-queue-label").text() shouldBe "Currently in:"
    }

    "Render currently in 'unknown'" in {
      // Given
      val c = aCase(
        withAssignee(None),
        withQueue("99")
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-assigned-queue")
      doc.getElementById("activity-events-assigned-queue").text() shouldBe "unknown"
      doc.getElementById("activity-events-assigned-queue-label").text() shouldBe "Currently in:"
    }

    "Render 'Queue Change' from Some to Some" in {
      // Given
      val c = aCase()
      val e = Event(
        id = "EVENT_ID",
        details = QueueChange(from = Some("2"), to = Some("1"), comment = Some("comment")),
        operator = Operator("id", Some("Name")),
        caseReference = "ref",
        timestamp = date
      )

      // When
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queues))

      // Then
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Name moved this case to the TEST queue")
    }

    "Render 'Reassign Link' When Case is in valid state" in {

      Set(CaseStatus.OPEN, CaseStatus.REFERRED, CaseStatus.SUSPENDED).foreach(status => {
        // Given
        val c = aCase(
          withAssignee(Some(Operator("id"))),
          withStatus(status)
        )
        // When
        val doc = view(activity_details(c, Paged(Seq.empty), ActivityForm.form, queues))

        // Then
        doc should containElementWithID("reassign-queue-link")
      })
    }

    "Not Render 'Reassign Link' When Case is in invalid state" in {

      Set(CaseStatus.NEW, CaseStatus.COMPLETED, CaseStatus.CANCELLED).foreach(status => {
        // Given
        val c = aCase(
          withAssignee(Some(Operator("id"))),
          withStatus(status)
        )
        // When
        val doc = view(activity_details(c, Paged(Seq.empty), ActivityForm.form, queues))

        // Then
        doc shouldNot containElementWithID("reassign-queue-link")
      })
    }

    "Not Render 'Reassign Link' When Case is not assigned" in {

      // Given
      val c = aCase(
        withAssignee(None),
        withStatus(CaseStatus.OPEN)
      )
      // When
      val doc = view(activity_details(c, Paged(Seq.empty), ActivityForm.form, queues))

      // Then
      doc shouldNot containElementWithID("reassign-queue-link")
    }

    "Not render 'Reassign Link' when valid state but permissions as ReadOnly " in {

      Set(CaseStatus.OPEN, CaseStatus.REFERRED, CaseStatus.SUSPENDED).foreach(status => {
        // Given
        val c = aCase(
          withAssignee(Some(Operator("id"))),
          withStatus(status)
        )

        // When
        val doc = view(activity_details(c, Paged(Seq.empty), ActivityForm.form, queues)(readOnlyRequest, messages, appConfig))

        // Then
        doc shouldNot containElementWithID("reassign-queue-link")
      })
    }

    "Render 'Reassign Link' when valid state and permissions are ReadWrite " in {

      Set(CaseStatus.OPEN, CaseStatus.REFERRED, CaseStatus.SUSPENDED).foreach(status => {
        // Given
        val c = aCase(
          withAssignee(Some(Operator("id"))),
          withStatus(status)
        )

        // When
        val doc = view(activity_details(c, Paged(Seq.empty), ActivityForm.form, queues)(readWriteRequest, messages, appConfig))

        // Then
        doc should containElementWithID("reassign-queue-link")
      })

    }
  }

}
