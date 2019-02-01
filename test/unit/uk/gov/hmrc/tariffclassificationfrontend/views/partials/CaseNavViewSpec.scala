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

  private val application = "Application"
  private val trader = "Trader"
  private val ruling = "Ruling"
  private val attachments = "Attachments"
  private val activity = "Activity"
  private val keywords = "Keywords"

  private val applicationDetailsURL = routes.CaseController.applicationDetails("ref").url
  private val rulingURL = routes.CaseController.rulingDetails("ref").url
  private val attachmentsURL = routes.AttachmentsController.attachmentsDetails("ref").url
  private val activityURL = routes.CaseController.activityDetails("ref").url
  private val traderURL = routes.CaseController.trader("ref").url
  private val keywordsURL = routes.CaseController.keywordsDetails("ref").url

  "Case Heading" should {

    "Render Trader" in {
      // When
      val doc = view(case_nav(CaseDetailPage.TRADER, "ref"))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText(trader)
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(5)
      anchors.get(0) should containText(application)
      anchors.get(0) should haveAttribute("href", applicationDetailsURL)
      anchors.get(1) should containText(ruling)
      anchors.get(1) should haveAttribute("href", rulingURL)
      anchors.get(2) should containText(attachments)
      anchors.get(2) should haveAttribute("href", attachmentsURL)
      anchors.get(3) should containText(activity)
      anchors.get(3) should haveAttribute("href", activityURL)
      anchors.get(4) should containText(keywords)
      anchors.get(4) should haveAttribute("href", keywordsURL)
    }

    "Render Application Details" in {
      // When
      val doc = view(case_nav(CaseDetailPage.APPLICATION_DETAILS, "ref"))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText(application)
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(5)
      anchors.get(0) should containText(trader)
      anchors.get(0) should haveAttribute("href", traderURL)
      anchors.get(1) should containText(ruling)
      anchors.get(1) should haveAttribute("href", rulingURL)
      anchors.get(2) should containText(attachments)
      anchors.get(2) should haveAttribute("href", attachmentsURL)
      anchors.get(3) should containText(activity)
      anchors.get(3) should haveAttribute("href", activityURL)
      anchors.get(4) should containText(keywords)
      anchors.get(4) should haveAttribute("href", keywordsURL)

    }

    "Render Ruling Details" in {
      // When
      val doc = view(case_nav(CaseDetailPage.RULING, "ref"))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText(ruling)
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(5)
      anchors.get(0) should containText(trader)
      anchors.get(0) should haveAttribute("href", traderURL)
      anchors.get(1) should containText(application)
      anchors.get(1) should haveAttribute("href", applicationDetailsURL)
      anchors.get(2) should containText(attachments)
      anchors.get(2) should haveAttribute("href", attachmentsURL)
      anchors.get(3) should containText(activity)
      anchors.get(3) should haveAttribute("href", activityURL)
      anchors.get(4) should containText(keywords)
      anchors.get(4) should haveAttribute("href", keywordsURL)
    }

    "Render Attachments" in {
      // When
      val doc = view(case_nav(CaseDetailPage.ATTACHMENTS, "ref"))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText(attachments)
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(5)
      anchors.get(0) should containText(trader)
      anchors.get(0) should haveAttribute("href", traderURL)
      anchors.get(1) should containText(application)
      anchors.get(1) should haveAttribute("href", applicationDetailsURL)
      anchors.get(2) should containText(ruling)
      anchors.get(2) should haveAttribute("href", rulingURL)
      anchors.get(3) should containText(activity)
      anchors.get(3) should haveAttribute("href", activityURL)
      anchors.get(4) should containText(keywords)
      anchors.get(4) should haveAttribute("href", keywordsURL)
    }

    "Render Activity" in {
      // When
      val doc = view(case_nav(CaseDetailPage.ACTIVITY, "ref"))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText(activity)
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(5)
      anchors.get(0) should containText(trader)
      anchors.get(0) should haveAttribute("href", traderURL)
      anchors.get(1) should containText(application)
      anchors.get(1) should haveAttribute("href", applicationDetailsURL)
      anchors.get(2) should containText(ruling)
      anchors.get(2) should haveAttribute("href", rulingURL)
      anchors.get(3) should containText(attachments)
      anchors.get(3) should haveAttribute("href", attachmentsURL)
      anchors.get(4) should containText(keywords)
      anchors.get(4) should haveAttribute("href", keywordsURL)
    }
  }

}
