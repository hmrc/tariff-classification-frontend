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

import models.Queue

class SLAReport(histogram: Histogram) {

  lazy val totalCount: Int =
    histogram.map.values.map(_.count).sum

  def totalCountFor(queue: Queue): Int =
    histogram.getBuckets(Some(queue.id)).values.map(_.count).sum

  def totalCountFor(interval: HistogramBucketInterval): Int =
    histogram.getBuckets(interval).values.map(_.count).sum

  def totalPercentFor(interval: HistogramBucketInterval): Int =
    Math.round(100 * totalCountFor(interval).toDouble / totalCount.toDouble).toInt

  def totalCountFor(queue: Queue, interval: HistogramBucketInterval): Int =
    histogram.getBucket(Some(queue.id), interval).map(_.count).getOrElse(0)

  def totalPercentFor(queue: Queue, interval: HistogramBucketInterval): Int =
    Math.round(100 * totalCountFor(queue, interval).toDouble / totalCountFor(queue).toDouble).toInt

}
