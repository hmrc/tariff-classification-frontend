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

package models.viewmodels

import models.SampleReturn.SampleReturn
import models.{Event, Paged, Sample}
import models.SampleStatus._

case class SampleStatusTabViewModel(caseReference: String, isSampleBeingSent: Boolean, whoIsSendingTheSample: Option[String],
                                    shouldSampleBeReturned: Option[SampleReturn], sampleLocation: String,
                                    sampleActivity: Paged[Event])

object SampleStatusTabViewModel {

  val NOT_YET_IMPLEMENTED: Option[String] = None

  def apply(caseReference: String, sample: Sample, activity: Paged[Event]): SampleStatusTabViewModel =
    SampleStatusTabViewModel(caseReference, sample.status.isDefined,
    NOT_YET_IMPLEMENTED,
    sample.returnStatus,
    format(sample.status),
    activity )

}