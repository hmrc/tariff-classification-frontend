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

import java.time.{LocalDate, ZoneOffset}

import uk.gov.hmrc.tariffclassificationfrontend.controllers.routes
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus, Paged}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.cases_list
import uk.gov.tariffclassificationfrontend.utils.Cases._

class CasesListViewSpec extends ViewSpec {

  "Cases List" should {

    "Render nothing" in {
      // Given

      // When
      val doc = view(cases_list(Paged.empty[Case]))

      // Then
      doc should containElementWithID("cases_list-empty")
    }

    "Render some" in {
      // Given
      val c = aCase(
        withReference("REF"),
        withStatus(CaseStatus.NEW),
        withCreatedDate(LocalDate.of(2019,1,1).atStartOfDay().toInstant(ZoneOffset.UTC)),
        withDaysElapsed(1),
        withHolder(businessName = "BUSINESS-NAME"),
        withBTIDetails(goodName = "GOOD-NAME")
      )

      // When
      val doc = view(cases_list(Paged(Seq(c))))

      // Then
      doc shouldNot containElementWithID("cases_list-empty")
      doc should containElementWithID("cases_list-row-0")
      doc should containElementWithID("cases_list-row-0-reference")
      doc.getElementById("cases_list-row-0-reference") should containText("REF")
      doc should containElementWithID("cases_list-row-0-good_name")
      doc.getElementById("cases_list-row-0-good_name") should containText("GOOD-NAME")
      doc should containElementWithID("cases_list-row-0-business_name")
      doc.getElementById("cases_list-row-0-business_name") should containText("BUSINESS-NAME")
      doc should containElementWithID("cases_list-row-0-status")
      doc.getElementById("cases_list-row-0-status") should containText("NEW")
      doc should containElementWithID("cases_list-row-0-type")
      doc.getElementById("cases_list-row-0-type") should containText("BTI")
      doc should containElementWithID("cases_list-row-0-days_elapsed")
      doc.getElementById("cases_list-row-0-days_elapsed") should containText("1")
      doc should containElementWithID("cases_list-row-0-created_date")
      doc.getElementById("cases_list-row-0-created_date") should containText("01 Jan 2019")
    }

    "Render some - with link to case - when assigned to self" in {
      // Given
      val c = aCase(
        withReference("REF"),
        withAssignee(Some(authenticatedOperator))
      )

      // When
      val doc = view(cases_list(Paged(Seq(c))))

      // Then
      doc should containElementWithID("cases_list-row-0-reference")
      doc.getElementById("cases_list-row-0-reference") should haveAttribute("href", routes.CaseController.get("REF").url)
    }

    "Render some - with link to 'take ownership' - when assigned to a queue and no operator" in {
      // Given
      val c = aCase(
        withReference("REF"),
        withQueue("1"),
        withoutAssignee()
      )

      // When
      val doc = view(cases_list(Paged(Seq(c))))

      // Then
      doc should containElementWithID("cases_list-row-0-reference")
      doc.getElementById("cases_list-row-0-reference") should haveAttribute("href", routes.AssignCaseController.get("REF").url)
    }

    "Render some - with link to case - when not assigned to a queue" in {
      // Given
      val c = aCase(
        withReference("REF"),
        withoutQueue()
      )

      // When
      val doc = view(cases_list(Paged(Seq(c))))

      // Then
      doc should containElementWithID("cases_list-row-0-reference")
      doc.getElementById("cases_list-row-0-reference") should haveAttribute("href", routes.CaseController.get("REF").url)
    }
  }
}
