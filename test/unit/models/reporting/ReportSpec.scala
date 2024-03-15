/*
 * Copyright 2023 HM Revenue & Customs
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

package models
package reporting

import cats.data.NonEmptySeq
import models.ModelsBaseSpec

import java.net.URLDecoder
import java.time.Instant

class ReportSpec extends ModelsBaseSpec {
  "Report" should {
    "assume SummaryReport if group_by and sort_by is provided" in {
      val summaryReportParams = Map[String, Seq[String]](
        "name"     -> Seq("Summary report"),
        "group_by" -> Seq("assigned_user"),
        "sort_by"  -> Seq("assigned_user")
      )

      Report.reportQueryStringBindable.bind("", summaryReportParams) shouldBe Some(
        Right(
          SummaryReport(
            name    = "Summary report",
            groupBy = NonEmptySeq.one(ReportField.User),
            sortBy  = ReportField.User
          )
        )
      )
    }

    "assume CaseReport if fields is provided" in {
      val caseReportParams = Map[String, Seq[String]](
        "name"   -> Seq("Case report"),
        "fields" -> Seq("reference", "status", "elapsed_days", "total_days")
      )

      Report.reportQueryStringBindable.bind("", caseReportParams) shouldBe Some(
        Right(
          CaseReport(
            name   = "Case report",
            sortBy = ReportField.Reference,
            fields =
              NonEmptySeq.of(ReportField.Reference, ReportField.Status, ReportField.ElapsedDays, ReportField.TotalDays)
          )
        )
      )
    }

    "assume QueueReport otherwise" in {
      Report.reportQueryStringBindable.bind("", Map.empty) shouldBe Some(Right(QueueReport()))
    }

    "unbind to query string" in {
      val summaryReport =
        SummaryReport("Summary report", groupBy = NonEmptySeq.one(ReportField.User), sortBy = ReportField.User)

      URLDecoder.decode(Report.reportQueryStringBindable.unbind("", summaryReport), "UTF-8") shouldBe (
        "name=Summary report" +
          "&group_by=assigned_user" +
          "&sort_by=assigned_user" +
          "&sort_order=asc" +
          "&case_type=" +
          "&status=" +
          "&liability_status=" +
          "&team=" +
          "&max_fields=" +
          "&include_cases=false"
      )

      val caseReport = CaseReport(
        "Case report",
        fields =
          NonEmptySeq.of(ReportField.Reference, ReportField.Status, ReportField.ElapsedDays, ReportField.TotalDays)
      )
      URLDecoder.decode(Report.reportQueryStringBindable.unbind("", caseReport), "UTF-8") shouldBe (
        "name=Case report" +
          "&sort_by=reference" +
          "&sort_order=asc" +
          "&case_type=" +
          "&status=" +
          "&liability_status=" +
          "&team=" +
          "&fields=reference,status,elapsed_days,total_days" +
          "&due_to_expire=false"
      )

      URLDecoder.decode(Report.reportQueryStringBindable.unbind("", QueueReport()), "UTF-8") shouldBe (
        "name=Number of cases in queues" +
          "&sort_by=assigned_team" +
          "&sort_order=asc" +
          "&case_type=" +
          "&status=" +
          "&liability_status=" +
          "&team="
      )
    }
  }

  "CaseReport" should {
    "bind from query string" in {
      val params1 = Map[String, Seq[String]](
        "name"       -> Seq("Case report"),
        "sort_by"    -> Seq("count"),
        "sort_order" -> Seq("desc"),
        "case_type"  -> Seq("BTI", "CORRESPONDENCE"),
        "status"     -> Seq("LIVE", "REFERRED"),
        "team"       -> Seq("1", "3"),
        "min_date"   -> Seq("2020-03-21T12:03:15.000Z"),
        "max_date"   -> Seq("2021-03-21T12:03:15.000Z"),
        "fields"     -> Seq("reference", "status", "assigned_user")
      )

      CaseReport.caseReportQueryStringBindable.bind("", params1) shouldBe Some(
        Right(
          CaseReport(
            name      = "Case report",
            sortBy    = ReportField.Count,
            sortOrder = SortDirection.DESCENDING,
            caseTypes = Set(ApplicationType.ATAR, ApplicationType.CORRESPONDENCE),
            statuses  = Set(PseudoCaseStatus.LIVE, PseudoCaseStatus.REFERRED),
            teams     = Set("1", "3"),
            dateRange = InstantRange(
              Instant.parse("2020-03-21T12:03:15.000Z"),
              Instant.parse("2021-03-21T12:03:15.000Z")
            ),
            fields = NonEmptySeq.of(ReportField.Reference, ReportField.Status, ReportField.User)
          )
        )
      )

      val params2 = Map[String, Seq[String]](
        "name"       -> Seq("Case report"),
        "sort_by"    -> Seq("date_created"),
        "sort_order" -> Seq("asc"),
        "case_type"  -> Seq("MISCELLANEOUS", "CORRESPONDENCE"),
        "status"     -> Seq("COMPLETED", "REJECTED"),
        "team"       -> Seq("4", "5"),
        "fields"     -> Seq("reference", "status", "elapsed_days", "total_days")
      )

      CaseReport.caseReportQueryStringBindable.bind("", params2) shouldBe Some(
        Right(
          CaseReport(
            name      = "Case report",
            sortBy    = ReportField.DateCreated,
            sortOrder = SortDirection.ASCENDING,
            caseTypes = Set(ApplicationType.MISCELLANEOUS, ApplicationType.CORRESPONDENCE),
            statuses  = Set(PseudoCaseStatus.COMPLETED, PseudoCaseStatus.REJECTED),
            teams     = Set("4", "5"),
            fields =
              NonEmptySeq.of(ReportField.Reference, ReportField.Status, ReportField.ElapsedDays, ReportField.TotalDays)
          )
        )
      )

      val minParams = Map[String, Seq[String]](
        "name"   -> Seq("Case report"),
        "fields" -> Seq("reference", "status", "elapsed_days", "total_days")
      )

      CaseReport.caseReportQueryStringBindable.bind("", minParams) shouldBe Some(
        Right(
          CaseReport(
            name   = "Case report",
            sortBy = ReportField.Reference,
            fields =
              NonEmptySeq.of(ReportField.Reference, ReportField.Status, ReportField.ElapsedDays, ReportField.TotalDays)
          )
        )
      )
    }

    "unbind to query string" in {
      URLDecoder.decode(
        CaseReport.caseReportQueryStringBindable.unbind(
          "",
          CaseReport(
            name              = "Case report",
            fields            = NonEmptySeq.one(ReportField.Reference),
            sortBy            = ReportField.Count,
            sortOrder         = SortDirection.DESCENDING,
            caseTypes         = Set(ApplicationType.ATAR, ApplicationType.CORRESPONDENCE),
            statuses          = Set(PseudoCaseStatus.LIVE, PseudoCaseStatus.NEW),
            liabilityStatuses = Set(LiabilityStatus.NON_LIVE),
            teams             = Set("1", "3"),
            dateRange = InstantRange(
              Instant.parse("2020-03-21T12:03:15.000Z"),
              Instant.parse("2021-03-21T12:03:15.000Z")
            ),
            dueToExpireReport = true
          )
        ),
        "UTF-8"
      ) shouldBe (
        "name=Case report" +
          "&sort_by=count" +
          "&sort_order=desc" +
          "&case_type=BTI,CORRESPONDENCE" +
          "&status=LIVE,NEW" +
          "&liability_status=NON_LIVE" +
          "&team=1,3" +
          "&min_date=2020-03-21T12:03:15Z" +
          "&max_date=2021-03-21T12:03:15Z" +
          "&fields=reference" +
          "&due_to_expire=true"
      )

      URLDecoder.decode(
        CaseReport.caseReportQueryStringBindable.unbind(
          "",
          CaseReport(
            name              = "Case report",
            sortBy            = ReportField.DateCreated,
            sortOrder         = SortDirection.ASCENDING,
            caseTypes         = Set(ApplicationType.MISCELLANEOUS, ApplicationType.CORRESPONDENCE),
            statuses          = Set(PseudoCaseStatus.COMPLETED, PseudoCaseStatus.REJECTED),
            liabilityStatuses = Set(LiabilityStatus.NON_LIVE),
            teams             = Set("4", "5"),
            fields =
              NonEmptySeq.of(ReportField.Reference, ReportField.Status, ReportField.ElapsedDays, ReportField.TotalDays)
          )
        ),
        "UTF-8"
      ) shouldBe (
        "name=Case report" +
          "&sort_by=date_created" +
          "&sort_order=asc" +
          "&case_type=MISCELLANEOUS,CORRESPONDENCE" +
          "&status=COMPLETED,REJECTED" +
          "&liability_status=NON_LIVE" +
          "&team=4,5" +
          "&fields=reference,status,elapsed_days,total_days" +
          "&due_to_expire=false"
      )
    }
  }

  "SummaryReport" should {
    "bind from query string" in {
      val params1 = Map[String, Seq[String]](
        "name"       -> Seq("Summary report"),
        "group_by"   -> Seq("status"),
        "sort_by"    -> Seq("count"),
        "sort_order" -> Seq("desc"),
        "case_type"  -> Seq("BTI", "CORRESPONDENCE"),
        "status"     -> Seq("LIVE", "REFERRED"),
        "team"       -> Seq("1", "3"),
        "min_date"   -> Seq("2020-03-21T12:03:15.000Z"),
        "max_date"   -> Seq("2021-03-21T12:03:15.000Z"),
        "max_fields" -> Seq("total_days")
      )

      SummaryReport.summaryReportQueryStringBindable.bind("", params1) shouldBe Some(
        Right(
          SummaryReport(
            name      = "Summary report",
            groupBy   = NonEmptySeq.one(ReportField.Status),
            sortBy    = ReportField.Count,
            sortOrder = SortDirection.DESCENDING,
            caseTypes = Set(ApplicationType.ATAR, ApplicationType.CORRESPONDENCE),
            statuses  = Set(PseudoCaseStatus.LIVE, PseudoCaseStatus.REFERRED),
            teams     = Set("1", "3"),
            maxFields = Seq(ReportField.TotalDays),
            dateRange = InstantRange(
              Instant.parse("2020-03-21T12:03:15.000Z"),
              Instant.parse("2021-03-21T12:03:15.000Z")
            )
          )
        )
      )

      val params2 = Map[String, Seq[String]](
        "name"       -> Seq("Summary report"),
        "group_by"   -> Seq("assigned_user"),
        "sort_by"    -> Seq("date_created"),
        "sort_order" -> Seq("asc"),
        "case_type"  -> Seq("MISCELLANEOUS", "CORRESPONDENCE"),
        "status"     -> Seq("COMPLETED", "REJECTED"),
        "team"       -> Seq("4", "5"),
        "max_fields" -> Seq("elapsed_days")
      )

      SummaryReport.summaryReportQueryStringBindable.bind("", params2) shouldBe Some(
        Right(
          SummaryReport(
            name      = "Summary report",
            groupBy   = NonEmptySeq.one(ReportField.User),
            sortBy    = ReportField.DateCreated,
            sortOrder = SortDirection.ASCENDING,
            caseTypes = Set(ApplicationType.MISCELLANEOUS, ApplicationType.CORRESPONDENCE),
            statuses  = Set(PseudoCaseStatus.COMPLETED, PseudoCaseStatus.REJECTED),
            teams     = Set("4", "5"),
            maxFields = Seq(ReportField.ElapsedDays)
          )
        )
      )

      val minParams = Map[String, Seq[String]](
        "name"     -> Seq("Summary report"),
        "group_by" -> Seq("assigned_user"),
        "sort_by"  -> Seq("assigned_user")
      )

      SummaryReport.summaryReportQueryStringBindable.bind("", minParams) shouldBe Some(
        Right(
          SummaryReport(
            name    = "Summary report",
            groupBy = NonEmptySeq.one(ReportField.User),
            sortBy  = ReportField.User
          )
        )
      )
    }

    "unbind to query string" in {
      URLDecoder.decode(
        SummaryReport.summaryReportQueryStringBindable.unbind(
          "",
          SummaryReport(
            name              = "Summary report",
            groupBy           = NonEmptySeq.one(ReportField.Status),
            sortBy            = ReportField.Count,
            sortOrder         = SortDirection.DESCENDING,
            caseTypes         = Set(ApplicationType.ATAR, ApplicationType.CORRESPONDENCE),
            statuses          = Set(PseudoCaseStatus.LIVE, PseudoCaseStatus.REFERRED),
            liabilityStatuses = Set(LiabilityStatus.NON_LIVE),
            teams             = Set("1", "3"),
            maxFields         = Seq(ReportField.ElapsedDays),
            dateRange = InstantRange(
              Instant.parse("2020-03-21T12:03:15.000Z"),
              Instant.parse("2021-03-21T12:03:15.000Z")
            )
          )
        ),
        "UTF-8"
      ) shouldBe (
        "name=Summary report" +
          "&group_by=status" +
          "&sort_by=count" +
          "&sort_order=desc" +
          "&case_type=BTI,CORRESPONDENCE" +
          "&status=LIVE,REFERRED" +
          "&liability_status=NON_LIVE" +
          "&team=1,3" +
          "&min_date=2020-03-21T12:03:15Z" +
          "&max_date=2021-03-21T12:03:15Z" +
          "&max_fields=elapsed_days" +
          "&include_cases=false"
      )

      URLDecoder.decode(
        SummaryReport.summaryReportQueryStringBindable.unbind(
          "",
          SummaryReport(
            name              = "Summary report",
            groupBy           = NonEmptySeq.one(ReportField.User),
            sortBy            = ReportField.DateCreated,
            sortOrder         = SortDirection.ASCENDING,
            caseTypes         = Set(ApplicationType.MISCELLANEOUS, ApplicationType.CORRESPONDENCE),
            statuses          = Set(PseudoCaseStatus.COMPLETED, PseudoCaseStatus.REJECTED),
            liabilityStatuses = Set(LiabilityStatus.NON_LIVE),
            teams             = Set("4", "5"),
            maxFields         = Seq(ReportField.TotalDays)
          )
        ),
        "UTF-8"
      ) shouldBe (
        "name=Summary report" +
          "&group_by=assigned_user" +
          "&sort_by=date_created" +
          "&sort_order=asc" +
          "&case_type=MISCELLANEOUS,CORRESPONDENCE" +
          "&status=COMPLETED,REJECTED" +
          "&liability_status=NON_LIVE" +
          "&team=4,5" +
          "&max_fields=total_days" +
          "&include_cases=false"
      )
    }
  }

  "QueueReport" should {
    "bind from query string" in {
      val params1 = Map[String, Seq[String]](
        "sort_by"       -> Seq("count"),
        "sort_order"    -> Seq("desc"),
        "case_type"     -> Seq("BTI", "CORRESPONDENCE"),
        "status"        -> Seq("LIVE", "REFERRED"),
        "team"          -> Seq("1", "3"),
        "assigned_user" -> Seq("1"),
        "min_date"      -> Seq("2020-03-21T12:03:15.000Z"),
        "max_date"      -> Seq("2021-03-21T12:03:15.000Z")
      )

      QueueReport.queueReportQueryStringBindable.bind("", params1) shouldBe Some(
        Right(
          QueueReport(
            sortBy    = ReportField.Count,
            sortOrder = SortDirection.DESCENDING,
            caseTypes = Set(ApplicationType.ATAR, ApplicationType.CORRESPONDENCE),
            statuses  = Set(PseudoCaseStatus.LIVE, PseudoCaseStatus.REFERRED),
            teams     = Set("1", "3"),
            assignee  = Some("1"),
            dateRange = InstantRange(
              Instant.parse("2020-03-21T12:03:15.000Z"),
              Instant.parse("2021-03-21T12:03:15.000Z")
            )
          )
        )
      )

      val params2 = Map[String, Seq[String]](
        "sort_by"    -> Seq("date_created"),
        "sort_order" -> Seq("asc"),
        "case_type"  -> Seq("MISCELLANEOUS", "CORRESPONDENCE"),
        "status"     -> Seq("COMPLETED", "REJECTED"),
        "team"       -> Seq("4", "5")
      )

      QueueReport.queueReportQueryStringBindable.bind("", params2) shouldBe Some(
        Right(
          QueueReport(
            sortBy    = ReportField.DateCreated,
            sortOrder = SortDirection.ASCENDING,
            caseTypes = Set(ApplicationType.MISCELLANEOUS, ApplicationType.CORRESPONDENCE),
            statuses  = Set(PseudoCaseStatus.COMPLETED, PseudoCaseStatus.REJECTED),
            teams     = Set("4", "5")
          )
        )
      )

      QueueReport.queueReportQueryStringBindable.bind("", Map.empty) shouldBe Some(Right(QueueReport()))
    }

    "unbind to query string" in {
      URLDecoder.decode(
        QueueReport.queueReportQueryStringBindable.unbind(
          "",
          QueueReport(
            sortBy    = ReportField.Count,
            sortOrder = SortDirection.DESCENDING,
            caseTypes = Set(ApplicationType.ATAR, ApplicationType.CORRESPONDENCE),
            statuses  = Set(PseudoCaseStatus.LIVE, PseudoCaseStatus.NEW),
            teams     = Set("1", "3"),
            dateRange = InstantRange(
              Instant.parse("2020-03-21T12:03:15.000Z"),
              Instant.parse("2021-03-21T12:03:15.000Z")
            )
          )
        ),
        "UTF-8"
      ) shouldBe (
        "name=Number of cases in queues" +
          "&sort_by=count" +
          "&sort_order=desc" +
          "&case_type=BTI,CORRESPONDENCE" +
          "&status=LIVE,NEW" +
          "&liability_status=" +
          "&team=1,3" +
          "&min_date=2020-03-21T12:03:15Z" +
          "&max_date=2021-03-21T12:03:15Z"
      )

      URLDecoder.decode(
        QueueReport.queueReportQueryStringBindable.unbind(
          "",
          QueueReport(
            sortBy    = ReportField.DateCreated,
            sortOrder = SortDirection.ASCENDING,
            caseTypes = Set(ApplicationType.MISCELLANEOUS, ApplicationType.CORRESPONDENCE),
            statuses  = Set(PseudoCaseStatus.COMPLETED, PseudoCaseStatus.REJECTED),
            teams     = Set("4", "5")
          )
        ),
        "UTF-8"
      ) shouldBe (
        "name=Number of cases in queues" +
          "&sort_by=date_created" +
          "&sort_order=asc" +
          "&case_type=MISCELLANEOUS,CORRESPONDENCE" +
          "&status=COMPLETED,REJECTED" +
          "&liability_status=" +
          "&team=4,5"
      )
    }
  }
}
