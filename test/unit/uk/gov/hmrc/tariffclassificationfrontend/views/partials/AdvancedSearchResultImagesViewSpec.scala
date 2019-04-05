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

import uk.gov.hmrc.tariffclassificationfrontend.forms.SearchForm
import uk.gov.hmrc.tariffclassificationfrontend.models.response.ScanStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus, Paged}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.advanced_search
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.advanced_search_results_images
import uk.gov.tariffclassificationfrontend.utils.Cases
import uk.gov.tariffclassificationfrontend.utils.Cases._

class AdvancedSearchResultImagesViewSpec extends ViewSpec {

  private val defaultTab = "details"

  "Advanced Search" should {

    "Render No Results" in {

      // When
      val doc = view(advanced_search_results_images(Paged.empty[SearchResult]))
      // Then
      doc should  containElementWithID("advanced_search_results-empty")
      doc shouldNot containElementWithID("advanced_search_images-results")
    }


    "Render with Results" in {
      //Given
      val c = aCase(
        withReference("reference"),
        withStatus(CaseStatus.OPEN),
        withoutDecision(),
        withAttachment(attachment("FILE_ID_1")),
        withAttachment(attachment("FILE_ID_2")),
        withHolder(businessName = "business-name")
      )

      val storedAttachments =  Seq(Cases.storedAttachment.copy(id = "FILE_ID_1", mimeType= "image/png") , Cases.storedAttachment.copy(id = "FILE_ID_2", mimeType= "image/png"))
      val searchResult = SearchResult(c, storedAttachments)

      // When
      val doc = view(advanced_search_results_images(Paged(Seq(searchResult))))

      // Then
      doc shouldNot  containElementWithID("advanced_search_results-empty")
      doc should containElementWithID("advanced_search_images-results")
      doc should containElementWithID("thumbnail-advanced_search_results-row-0-attachments-0")
      doc should containElementWithID("thumbnail-advanced_search_results-row-0-attachments-1")

    }
  }

}
