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
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.activity_details
import uk.gov.tariffclassificationfrontend.utils.Cases._

class ActivityDetailsViewSpec extends ViewSpec {

  private val date = ZonedDateTime.of(2019,1,1,0,0,0,0, ZoneOffset.UTC).toInstant
  private val queueNames = Map("1" -> "ACT")

  "Activity Details" should {

    "Render empty events - showing 'Application Submitted'" in {
      // Given
      val c = aCase(
        withCreatedDate(date)
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queueNames))

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
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queueNames))

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
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queueNames))

      // Then
      doc should containElementWithID("activity-events-row-0-operator")
      doc.getElementById("activity-events-row-0-operator") should containText("name")
      doc should containElementWithID("activity-events-row-0-content")
      doc.getElementById("activity-events-row-0-content") should containText("Case note added")
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
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queueNames))

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
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queueNames))

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
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queueNames))

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
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queueNames))

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
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queueNames))

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
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queueNames))

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
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queueNames))

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
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queueNames))

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
      val doc = view(activity_details(c, Paged(Seq(e)), ActivityForm.form, queueNames))

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
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queueNames))

      // Then
      doc should containElementWithID("activity-events-assignee")
      doc.getElementById("activity-events-assignee") should containText("You")
    }

    "Render assigned to name" in {
      // Given
      val c = aCase(
        withAssignee(Some(Operator("id", Some("name"))))
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queueNames))

      // Then
      doc should containElementWithID("activity-events-assignee")
      doc.getElementById("activity-events-assignee") should containText("name")
    }

    "Render assigned to PID" in {
      // Given
      val c = aCase(
        withAssignee(Some(Operator("id")))
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queueNames))

      // Then
      doc should containElementWithID("activity-events-assignee")
      doc.getElementById("activity-events-assignee") should containText("PID id")
    }

    "Render 'Unassigned'" in {
      // Given
      val c = aCase(
        withAssignee(None)
      )

      // When
      val doc = view(activity_details(c, Paged.empty[Event], ActivityForm.form, queueNames))

      // Then
      doc should containElementWithID("activity-events-assignee")
      doc.getElementById("activity-events-assignee") should containText("unassigned")
    }
  }

}
