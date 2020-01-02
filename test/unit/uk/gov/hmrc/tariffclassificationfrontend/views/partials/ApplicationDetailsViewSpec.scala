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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials

import uk.gov.hmrc.tariffclassificationfrontend.models.{ImportExport, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.models.response.ScanStatus
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.application_details
import uk.gov.tariffclassificationfrontend.utils.Cases
import uk.gov.tariffclassificationfrontend.utils.Cases._

class ApplicationDetailsViewSpec extends ViewSpec {

  "Application Details" should {

    "render default negative text on optional fields when not present" in {
      // Given
      val `case` = aCase(
        withOptionalApplicationFields(),
        withoutAttachments()
      )

      // When
      val doc = view(application_details(`case`, Seq.empty, None))

      // Then
      doc.getElementById("app-details-reissue-application-type") should containText(messages("case.bti.new"))
      doc.getElementById("app-details-confidential-info") should containText(messages("answer.none"))
      doc shouldNot containElementWithID("app-details-related-reference")
      doc.getElementById("app-details-legal-proceedings") should containText(messages("answer.no"))
      doc.getElementById("app-details-other-info") should containText(messages("answer.none"))
      doc.getElementById("app-details-import-or-export") should containText(messages("site.unknown"))
    }

    "Render optional fields when present" in {
      // Given
      val `case` = aCase(
        withOptionalApplicationFields(
          confidentialInformation = Some("confidential info"),
          otherInformation = Some("other info"),
          reissuedBTIReference = Some("reissued bti"),
          relatedBTIReference = Some("related bti"),
          knownLegalProceedings = Some("legal proceedings"),
          envisagedCommodityCode = Some("envisaged code"),
          importOrExport = Some(ImportExport.IMPORT)
        ),
        withAttachment(attachment("FILE_ID"))
      )
      val storedAttachment = Cases.storedAttachment.copy(id = "FILE_ID", url = Some("url"), scanStatus = Some(ScanStatus.READY))

      // When
      val doc = view(application_details(`case`, Seq(storedAttachment), None))

      // Then
      doc should containElementWithID("app-details-reissue-application-type")
      doc.getElementById("app-details-reissue-application-type") should containText(messages("case.bti.renewal"))
      doc should containElementWithID("app-details-reissue-application-reference")
      doc.getElementById("app-details-reissue-application-reference") should containText("reissued bti")
      doc should containElementWithID("app-details-envisaged-code")
      doc.getElementById("app-details-envisaged-code") should containText("envisaged code")
      doc should containElementWithID("app-details-confidential-info")
      doc.getElementById("app-details-confidential-info") should containText("confidential info")
      doc should containElementWithID("app-details-related-reference")
      doc.getElementById("app-details-related-reference") should containText("related bti")
      doc should containElementWithID("app-details-legal-proceedings")
      doc.getElementById("app-details-legal-proceedings") should containText("legal proceedings")
      doc should containElementWithID("app-details-other-info")
      doc.getElementById("app-details-other-info") should containText("other info")
      doc should containElementWithID("app-details-import-or-export")
      doc.getElementById("app-details-import-or-export") should containText("Import")
    }
  }
}
