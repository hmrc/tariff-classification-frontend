package uk.gov.hmrc.tariffclassificationfrontend.views

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.{Queue, ReportResult}

class ReferralReportTest extends UnitSpec {

  "Referral Report" should {
    val queue1 = Queue("id1", "", "")
    val queue2 = Queue("id2", "", "")

    val report = new ReferralReport(
      Seq(
        ReportResult(Some("id1"), Seq(1, 2, 3)),
        ReportResult(Some("id2"), Seq(4, 5))
      )
    )

    "Calculate Count" in {
      report.count shouldBe 5
    }

    "Calculate Average" in {
      report.average shouldBe 3
    }

    "Calculate Count for group" in {
      report.countFor(queue1) shouldBe 3
      report.countFor(queue2) shouldBe 2
    }

    "Calculate Average for group" in {
      report.averageFor(queue1) shouldBe 2
      report.averageFor(queue2) shouldBe 5
    }
  }

}
