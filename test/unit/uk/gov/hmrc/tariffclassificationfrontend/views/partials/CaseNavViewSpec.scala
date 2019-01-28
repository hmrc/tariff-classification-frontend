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

import uk.gov.hmrc.tariffclassificationfrontend.controllers.routes
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.case_nav
import uk.gov.hmrc.tariffclassificationfrontend.views.{CaseDetailPage, ViewSpec}

class CaseNavViewSpec extends ViewSpec {

  "Case Heading" should {

    "Render Summary" in {
      // When
      val doc = view(case_nav(CaseDetailPage.SUMMARY, "ref"))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText("Summary")
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(4)
      anchors.get(0) should containText("Application details")
      anchors.get(0) should haveAttribute("href", routes.CaseController.applicationDetails("ref").url)
      anchors.get(1) should containText("Ruling")
      anchors.get(1) should haveAttribute("href", routes.CaseController.rulingDetails("ref").url)
      anchors.get(2) should containText("Attachments")
      anchors.get(2) should haveAttribute("href", routes.CaseController.attachmentsDetails("ref").url)
      anchors.get(3) should containText("Activity")
      anchors.get(3) should haveAttribute("href", routes.CaseController.activityDetails("ref").url)
    }

    "Render Application Details" in {
      // When
      val doc = view(case_nav(CaseDetailPage.APPLICATION_DETAILS, "ref"))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText("Application details")
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(4)
      anchors.get(0) should containText("Summary")
      anchors.get(0) should haveAttribute("href", routes.CaseController.summary("ref").url)
      anchors.get(1) should containText("Ruling")
      anchors.get(1) should haveAttribute("href", routes.CaseController.rulingDetails("ref").url)
      anchors.get(2) should containText("Attachments")
      anchors.get(2) should haveAttribute("href", routes.CaseController.attachmentsDetails("ref").url)
      anchors.get(3) should containText("Activity")
      anchors.get(3) should haveAttribute("href", routes.CaseController.activityDetails("ref").url)
    }

    "Render Ruling Details" in {
      // When
      val doc = view(case_nav(CaseDetailPage.RULING, "ref"))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText("Ruling")
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(4)
      anchors.get(0) should containText("Summary")
      anchors.get(0) should haveAttribute("href", routes.CaseController.summary("ref").url)
      anchors.get(1) should containText("Application details")
      anchors.get(1) should haveAttribute("href", routes.CaseController.applicationDetails("ref").url)
      anchors.get(2) should containText("Attachments")
      anchors.get(2) should haveAttribute("href", routes.CaseController.attachmentsDetails("ref").url)
      anchors.get(3) should containText("Activity")
      anchors.get(3) should haveAttribute("href", routes.CaseController.activityDetails("ref").url)
    }

    "Render Attachments" in {
      // When
      val doc = view(case_nav(CaseDetailPage.ATTACHMENTS, "ref"))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText("Attachments")
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(4)
      anchors.get(0) should containText("Summary")
      anchors.get(0) should haveAttribute("href", routes.CaseController.summary("ref").url)
      anchors.get(1) should containText("Application details")
      anchors.get(1) should haveAttribute("href", routes.CaseController.applicationDetails("ref").url)
      anchors.get(2) should containText("Ruling")
      anchors.get(2) should haveAttribute("href", routes.CaseController.rulingDetails("ref").url)
      anchors.get(3) should containText("Activity")
      anchors.get(3) should haveAttribute("href", routes.CaseController.activityDetails("ref").url)
    }

    "Render Activity" in {
      // When
      val doc = view(case_nav(CaseDetailPage.ACTIVITY, "ref"))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText("Activity")
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(4)
      anchors.get(0) should containText("Summary")
      anchors.get(0) should haveAttribute("href", routes.CaseController.summary("ref").url)
      anchors.get(1) should containText("Application details")
      anchors.get(1) should haveAttribute("href", routes.CaseController.applicationDetails("ref").url)
      anchors.get(2) should containText("Ruling")
      anchors.get(2) should haveAttribute("href", routes.CaseController.rulingDetails("ref").url)
      anchors.get(3) should containText("Attachments")
      anchors.get(3) should haveAttribute("href", routes.CaseController.attachmentsDetails("ref").url)
    }
  }

}
