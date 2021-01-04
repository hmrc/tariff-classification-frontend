/*
 * Copyright 2021 HM Revenue & Customs
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
package viewmodels.atar

case class GoodsTabViewModel(
  caseReference: String,
  goodsName: String,
  goodsDescription: String,
  confidentialInformation: Option[String],
  hasAttachments: Boolean,
  hasAttachmentsFromApplicant: Boolean,
  sendingSamples: Boolean,
  suggestedCommodityCode: Option[String],
  knownLegalProceedings: Option[String],
  reissuedBTIReference: Option[String],
  relatedBTIReferences: List[String],
  otherInformation: Option[String]
)

object GoodsTabViewModel {
  def fromCase(cse: Case) = {
    val atarApplication = cse.application.asATAR
    GoodsTabViewModel(
      cse.reference,
      atarApplication.goodName,
      atarApplication.goodDescription,
      atarApplication.confidentialInformation,
      cse.attachments.nonEmpty,
      cse.attachments.exists(_.operator.isEmpty),
      atarApplication.sampleToBeProvided,
      atarApplication.envisagedCommodityCode,
      atarApplication.knownLegalProceedings,
      atarApplication.reissuedBTIReference,
      if (atarApplication.relatedBTIReferences.nonEmpty)
        atarApplication.relatedBTIReferences
      else
        atarApplication.relatedBTIReference.toList,
      atarApplication.otherInformation
    )
  }
}
