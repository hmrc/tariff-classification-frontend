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

package views

import models.{CaseReportGroup, ReportResult}

class HistogramTest extends ViewSpec {

  "Histogram Bucket" should {
    "count" in {
      HistogramBucket(Seq(1,2,3)).count shouldBe 3
      HistogramBucket(Seq(1,2)).count shouldBe 2
    }
  }

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
        ReportResult(Map(CaseReportGroup.QUEUE ->  None), value = Seq(0, 1, 2, 3, 4, 5))
      )

      Histogram.calculate(data, buckets) shouldBe Histogram(
        Map[(Option[String], HistogramBucketInterval), HistogramBucket](
          ((None, interval1), HistogramBucket(Seq(0, 1))),
          ((None, interval2), HistogramBucket(Seq(2, 3))),
          ((None, interval3), HistogramBucket(Seq(4, 5)))
        )
      )
    }

    "Build Groups from data with name" in {
      val data = Seq(
        ReportResult(Map(CaseReportGroup.QUEUE ->  Some("1")), value = Seq(0, 1, 2, 3, 4, 5))
      )

      Histogram.calculate(data, buckets) shouldBe Histogram(
        Map[(Option[String], HistogramBucketInterval), HistogramBucket](
          ((Some("1"), interval1), HistogramBucket(Seq(0, 1))),
          ((Some("1"), interval2), HistogramBucket(Seq(2, 3))),
          ((Some("1"), interval3), HistogramBucket(Seq(4, 5)))
        )
      )
    }

    "Build Groups from data" in {
      val data = Seq(
        ReportResult(Map(CaseReportGroup.QUEUE ->  Some("1")), value = Seq(0, 1, 2, 3, 4, 5)),
        ReportResult(Map(CaseReportGroup.QUEUE ->  None), value = Seq(0, 1, 2, 3, 4, 5))
      )

      Histogram.calculate(data, buckets) shouldBe Histogram(
        Map[(Option[String], HistogramBucketInterval), HistogramBucket](
          ((None, interval1), HistogramBucket(Seq(0, 1))),
          ((None, interval2), HistogramBucket(Seq(2, 3))),
          ((None, interval3), HistogramBucket(Seq(4, 5))),
          ((Some("1"), interval1), HistogramBucket(Seq(0, 1))),
          ((Some("1"), interval2), HistogramBucket(Seq(2, 3))),
          ((Some("1"), interval3), HistogramBucket(Seq(4, 5)))
        )
      )
    }

    "Get Bucket by Group & Interval" in {
      val histogram = Histogram(
        Map[(Option[String], HistogramBucketInterval), HistogramBucket](
          ((None, interval1), HistogramBucket(Seq(0, 1))),
          ((Some("1"), interval2), HistogramBucket(Seq(2, 3))),
          ((Some("2"), interval3), HistogramBucket(Seq(4, 5)))
        )
      )

      histogram.getBucket(None, interval1) shouldBe Some(HistogramBucket(Seq(0,1)))
      histogram.getBucket(Some("1"), interval2) shouldBe Some(HistogramBucket(Seq(2, 3)))
      histogram.getBucket(Some("2"), interval3) shouldBe Some(HistogramBucket(Seq(4, 5)))
    }

    "Get Buckets by Group" in {
      val histogram = Histogram(
        Map[(Option[String], HistogramBucketInterval), HistogramBucket](
          ((None, interval1), HistogramBucket(Seq(0, 1))),
          ((Some("1"), interval2), HistogramBucket(Seq(2, 3))),
          ((Some("2"), interval3), HistogramBucket(Seq(4, 5)))
        )
      )

      histogram.getBuckets(None) shouldBe Map(interval1 -> HistogramBucket(Seq(0,1)))
      histogram.getBuckets(Some("1")) shouldBe Map(interval2 -> HistogramBucket(Seq(2, 3)))
      histogram.getBuckets(Some("2")) shouldBe Map(interval3 -> HistogramBucket(Seq(4, 5)))
    }

    "Get Buckets by Interval" in {
      val histogram = Histogram(
        Map[(Option[String], HistogramBucketInterval), HistogramBucket](
          ((None, interval1), HistogramBucket(Seq(0, 1))),
          ((Some("1"), interval2), HistogramBucket(Seq(2, 3))),
          ((Some("2"), interval3), HistogramBucket(Seq(4, 5)))
        )
      )

      histogram.getBuckets(interval1) shouldBe Map(None -> HistogramBucket(Seq(0,1)))
      histogram.getBuckets(interval2) shouldBe Map(Some("1") -> HistogramBucket(Seq(2, 3)))
      histogram.getBuckets(interval3) shouldBe Map(Some("2") -> HistogramBucket(Seq(4, 5)))
    }

    "Filter by Interval" in {
      val histogram = Histogram(
        Map[(Option[String], HistogramBucketInterval), HistogramBucket](
          ((None, interval1), HistogramBucket(Seq(0, 1))),
          ((Some("1"), interval2), HistogramBucket(Seq(2, 3))),
          ((Some("2"), interval3), HistogramBucket(Seq(4, 5)))
        )
      )

      histogram.filterByInterval(_ == interval1) shouldBe Histogram(Map((None, interval1) -> HistogramBucket(Seq(0,1))))
      histogram.filterByInterval(_ == interval2) shouldBe Histogram(Map((Some("1"), interval2) -> HistogramBucket(Seq(2, 3))))
      histogram.filterByInterval(_ == interval3) shouldBe Histogram(Map((Some("2"), interval3) -> HistogramBucket(Seq(4, 5))))
    }

    "Filter by Group" in {
      val histogram = Histogram(
        Map[(Option[String], HistogramBucketInterval), HistogramBucket](
          ((None, interval1), HistogramBucket(Seq(0, 1))),
          ((Some("1"), interval2), HistogramBucket(Seq(2, 3))),
          ((Some("2"), interval3), HistogramBucket(Seq(4, 5)))
        )
      )

      histogram.filterByGroup(_.isEmpty) shouldBe Histogram(Map((None, interval1) -> HistogramBucket(Seq(0,1))))
      histogram.filterByGroup(_.contains("1")) shouldBe Histogram(Map((Some("1"), interval2) -> HistogramBucket(Seq(2, 3))))
      histogram.filterByGroup(_.contains("2")) shouldBe Histogram(Map((Some("2"), interval3) -> HistogramBucket(Seq(4, 5))))
    }
  }

}
