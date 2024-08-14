/*
 * Copyright 2024 HM Revenue & Customs
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

package views.partials

import models.request.AuthenticatedRequest
import models.viewmodels.atar.ApplicantTabViewModel
import models.{Permission, SampleStatus}
import play.api.mvc.AnyContentAsEmpty
import services.CountriesService
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.case_trader

class CaseTraderViewSpec extends ViewSpec {

  val requestWithReleaseAndSuppressPermission: AuthenticatedRequest[AnyContentAsEmpty.type] =
    requestWithPermissions(Permission.RELEASE_CASE, Permission.SUPPRESS_CASE)
  val countriesService = new CountriesService

  "Case Trader" should {

    "Not render agent details when not present" in {

      val `case` = aCase(
        withReference("ref"),
        withoutAgent()
      )

      val applicantTab = ApplicantTabViewModel.fromCase(`case`, Map.empty)

      val doc = view(case_trader(applicantTab))

      doc shouldNot containElementWithID("agent-submitted-heading")
    }

    "render boards file number when present" in {

      val c            = aCase().copy(caseBoardsFileNumber = Some("file 123"))
      val applicantTab = ApplicantTabViewModel.fromCase(c, Map.empty)

      val doc = view(case_trader(applicantTab))

      val boardFileNumber = doc.getElementById("boards-file-number")
      boardFileNumber.text() shouldBe "file 123"
    }

    "not show boards file number when not present" in {

      val c            = aCase()
      val applicantTab = ApplicantTabViewModel.fromCase(c, Map.empty)

      val doc = view(case_trader(applicantTab))

      doc shouldNot containElementWithID("boards-file-number-label")
      doc shouldNot containElementWithID("boards-file-number")
    }

    "show agent details for the Atar case if it is migrated" in {

      val c = aCase(
        withBTIApplication,
        withAgent(),
        withSampleStatus(Some(SampleStatus.AWAITING)),
        withBTIDetails(sampleToBeProvided = true, sampleToBeReturned = true)
      )

      val applicantTab = ApplicantTabViewModel.fromCase(c, Map.empty)

      val doc = view(case_trader(applicantTab))

      doc                                         should containElementWithID("agent-details-heading")
      doc                                         should containElementWithID("agent-details-eori")
      doc.getElementById("agent-details-eori")    should containText("agent-eori")
      doc                                         should containElementWithID("agent-details-name")
      doc.getElementById("agent-details-name")    should containText("agent-business")
      doc                                         should containElementWithID("agent-details-address")
      doc.getElementById("agent-details-address") should containText("agent-address1")
      doc.getElementById("agent-details-address") should containText("agent-address2")
      doc.getElementById("agent-details-address") should containText("agent-address3")
      doc.getElementById("agent-details-address") should containText("agent-postcode")
      doc.getElementById("agent-details-address") should containText("agent-country")
    }
  }

}
