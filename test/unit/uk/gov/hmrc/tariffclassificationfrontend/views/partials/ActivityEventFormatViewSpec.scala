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

import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, _}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.activity_event_format

class ActivityEventFormatViewSpec extends ViewSpec {

  private val queues = Seq(Queue(id = "1", slug = "test_1", name = "TEST_1"), Queue(id = "2", slug = "test_2", name = "TEST_2"))

  "Activity Event content Format" should {

    def defaultEventWith(details: Details): Event = {
      Event(
        id = "EVENT_ID",
        details = details,
        operator = Operator("id", Some("operator")),
        caseReference = "ref",
        timestamp = ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant
      )
    }

    def defaultView(details: Details) = {
      view(
        activity_event_format(
          event = defaultEventWith(details),
          index = 0,
          queues = queues
        )
      )
    }

    "Render 'Note'" in {
      val doc = defaultView(
        Note("comment")
      )

      doc.getElementById("activity-events-row-0-title") should containText("Case note added")
      doc.getElementById("activity-events-row-0-body") should containText("comment")
    }

    "Render 'Status Change'" in {
      val doc = defaultView(
        CaseStatusChange(from = CaseStatus.OPEN, to = CaseStatus.COMPLETED, comment = Some("comment"))
      )

      doc.getElementById("activity-events-row-0-title") should containText("Status changed from open to completed")
      doc.getElementById("activity-events-row-0-body") should containText("comment")
    }

    "Render 'Appeal Change'" in {
      val doc = defaultView(
        AppealStatusChange(from = None, to = Some(AppealStatus.IN_PROGRESS), comment = Some("comment"))
      )

      doc.getElementById("activity-events-row-0-title") should containText("Appeal status changed from None to Under appeal")
      doc.getElementById("activity-events-row-0-body") should containText("comment")
    }

    "Render 'Review Change'" in {
      val doc = defaultView(
        ReviewStatusChange(from = None, to = Some(ReviewStatus.IN_PROGRESS), comment = Some("comment"))
      )

      doc.getElementById("activity-events-row-0-title") should containText("Review status changed from None to Under review")
      doc.getElementById("activity-events-row-0-body") should containText("comment")
    }

    "Render 'Extended Use Change'" in {
      val doc = defaultView(
        ExtendedUseStatusChange(from = false, to = true, comment = Some("comment"))
      )

      doc.getElementById("activity-events-row-0-title") should containText("Application for extended use status changed from No to Yes")
      doc.getElementById("activity-events-row-0-body") should containText("comment")
    }

    "Render 'Assignment Change'" in {
      val doc = defaultView(
        AssignmentChange(from = None, to = None, comment = Some("comment"))
      )

      doc.getElementById("activity-events-row-0-title") should containText("Case unassigned from unknown operator")
    }

    "Render 'Assignment Change' from Some to Some" in {
      val doc = defaultView(
        AssignmentChange(from = Some(Operator("from", Some("FROM"))), to = Some(Operator("to", Some("TO"))), comment = Some("comment"))
      )

      doc.getElementById("activity-events-row-0-title") should containText("Case reassigned from FROM to TO")
      doc.getElementById("activity-events-row-0-body") should containText("comment")
    }

    "Render 'Assignment Change' from Some to None" in {
      val doc = defaultView(
        AssignmentChange(from = Some(Operator("from", Some("FROM"))), to = None, comment = Some("comment"))
      )

      doc.getElementById("activity-events-row-0-title") should containText("Case unassigned from FROM")
      doc.getElementById("activity-events-row-0-body") should containText("comment")
    }

    "Render 'Assignment Change' from None to Some" in {
      val doc = defaultView(
        AssignmentChange(from = None, to = Some(Operator("to", Some("TO"))), comment = Some("comment"))
      )

      doc.getElementById("activity-events-row-0-title") should containText("Case assigned to TO")
      doc.getElementById("activity-events-row-0-body") should containText("comment")
    }

    "Render 'Assignment Change' from None to None" in {
      val doc = defaultView(
        AssignmentChange(from = None, to = None, comment = Some("comment"))
      )

      doc.getElementById("activity-events-row-0-title") should containText("Case unassigned")
      doc.getElementById("activity-events-row-0-body") should containText("comment")
    }


    "Render 'Queue Change'" in {
      val doc = defaultView(
        QueueChange(from = Some("1"), to = Some("2"), comment = Some("comment"))
      )

      doc.getElementById("activity-events-row-0-title") should containText("operator moved this case to the TEST_2 queue")
      doc.getElementById("activity-events-row-0-body") should containText("comment")
    }

    "Render 'Note' already open" in {

      val doc = view(
        activity_event_format(
          defaultEventWith(Note("comment")), 0, queues, true
        )
      )

      // Then
      assert(doc.getElementsByTag("details").hasAttr("open"))
    }

    "Render 'Note' default closed" in {

      val doc = view(
        activity_event_format(
          defaultEventWith(Note("comment")), 0, queues
        )
      )

      assert(!doc.getElementsByTag("details").hasAttr("open"))
    }

  }
}
