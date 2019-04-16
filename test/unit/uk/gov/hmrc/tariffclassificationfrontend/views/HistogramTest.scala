package uk.gov.hmrc.tariffclassificationfrontend.views

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.ReportResult

class HistogramTest extends UnitSpec {

  "Histogram Bucket Interval" should {
    "calculate if a value is within its bounds" in {
      HistogramBucketInterval(None, None).contains(Integer.MIN_VALUE) shouldBe true
      HistogramBucketInterval(None, None).contains(Integer.MAX_VALUE) shouldBe true

      HistogramBucketInterval(Some(0), None).contains(Integer.MIN_VALUE) shouldBe false
      HistogramBucketInterval(Some(0), None).contains(Integer.MAX_VALUE) shouldBe true


      HistogramBucketInterval(None, Some(0)).contains(Integer.MIN_VALUE) shouldBe true
      HistogramBucketInterval(None, Some(0)).contains(Integer.MAX_VALUE) shouldBe false

      HistogramBucketInterval(Some(0), Some(0)).contains(Integer.MIN_VALUE) shouldBe false
      HistogramBucketInterval(Some(0), Some(0)).contains(Integer.MAX_VALUE) shouldBe false
      HistogramBucketInterval(Some(0), Some(0)).contains(0) shouldBe true
      HistogramBucketInterval(Some(0), Some(1)).contains(0) shouldBe true
      HistogramBucketInterval(Some(0), Some(1)).contains(1) shouldBe true
    }
  }

  "Histogram" should {
    val interval1 = HistogramBucketInterval(None, 1) // x <= 1
    val interval2 = HistogramBucketInterval(2, 3) // 2 <= x <= 3
    val interval3 = HistogramBucketInterval(4, None) // x >= 4
    val buckets = Seq(interval1, interval2, interval3)

    "Build Groups from data without name" in {
      val data = Seq(
        ReportResult(group = None, value = Seq(0, 1, 2, 3, 4, 5))
      )

      Histogram.calculate(data, buckets) shouldBe Histogram(
        Map[(Option[String], HistogramBucketInterval), HistogramBucket](
          ((None, interval1), HistogramBucket(Seq(0,1))),
          ((None, interval2), HistogramBucket(Seq(2,3))),
          ((None, interval3), HistogramBucket(Seq(4,5)))
        )
      )
    }
  }

}
