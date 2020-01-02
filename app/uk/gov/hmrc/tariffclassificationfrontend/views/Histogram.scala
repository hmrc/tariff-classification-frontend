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

package uk.gov.hmrc.tariffclassificationfrontend.views

import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseReportGroup, ReportResult}

case class HistogramBucket(data: Seq[Int]) {
  def count: Int = data.size
}

case class HistogramBucketInterval(lower: Option[Int], upper: Option[Int]) {
  def contains(value: Int): Boolean = !lower.exists(_ >  value) && !upper.exists(_ < value)
}

case class Histogram(map: Map[(Option[String], HistogramBucketInterval), HistogramBucket]) {

  def getBucket(group: Option[String], interval: HistogramBucketInterval): Option[HistogramBucket] = {
    map.get((group, interval))
  }

  def getBuckets(group: Option[String]): Map[HistogramBucketInterval, HistogramBucket] = {
    val matchingKeys: Set[(Option[String], HistogramBucketInterval)] = map.keys.filter(_._1 == group).toSet
    map.filter(e => matchingKeys.contains(e._1)).map(e => (e._1._2, e._2))
  }

  def getBuckets(interval: HistogramBucketInterval): Map[Option[String], HistogramBucket] = {
    val matchingKeys: Set[(Option[String], HistogramBucketInterval)] = map.keys.filter(_._2 == interval).toSet
    map.filter(e => matchingKeys.contains(e._1)).map(e => (e._1._1, e._2))
  }

  def filterByGroup(f: Option[String] => Boolean): Histogram = {
    val matchingKeys: Set[(Option[String], HistogramBucketInterval)] = map.keys.filter(key => f(key._1)).toSet
    Histogram(map.filter(e => matchingKeys.contains(e._1)))
  }

  def filterByInterval(f: HistogramBucketInterval => Boolean): Histogram = {
    val matchingKeys: Set[(Option[String], HistogramBucketInterval)] = map.keys.filter(key => f(key._2)).toSet
    Histogram(map.filter(e => matchingKeys.contains(e._1)))
  }
}

object Histogram {
  def calculate(results: Seq[ReportResult], intervals: Seq[HistogramBucketInterval]): Histogram = Histogram(
    results flatMap { result: ReportResult =>
      intervals.map { interval =>
        ((result.group.get(CaseReportGroup.QUEUE).get, interval), HistogramBucket(result.value.filter(interval.contains)))
      }
    } toMap
  )
}

object HistogramBucketInterval {
  def apply(lower: Int, upper: Option[Int]): HistogramBucketInterval = HistogramBucketInterval(Some(lower), upper)
  def apply(lower: Int, upper: Int): HistogramBucketInterval = HistogramBucketInterval(Some(lower), Some(upper))
  def apply(lower: Option[Int], upper: Int): HistogramBucketInterval = HistogramBucketInterval(lower, Some(upper))
}

