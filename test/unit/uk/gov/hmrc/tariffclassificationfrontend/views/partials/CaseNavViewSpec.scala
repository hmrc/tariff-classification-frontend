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

import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.tariffclassificationfrontend.controllers.routes
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.case_nav
import uk.gov.hmrc.tariffclassificationfrontend.views.{CaseDetailPage, ViewSpec}

class CaseNavViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private val application = "Application"
  private val trader = "Trader"
  private val ruling = "Ruling"
  private val attachments = "Attachments"
  private val activity = "Activity"
  private val keywords = "Keywords"
  private val appeal = "Appeal"

  private val applicationDetailsURL = routes.CaseController.applicationDetails("ref").url
  private val rulingURL = routes.CaseController.rulingDetails("ref").url
  private val attachmentsURL = routes.AttachmentsController.attachmentsDetails("ref").url
  private val activityURL = routes.CaseController.activityDetails("ref").url
  private val traderURL = routes.CaseController.trader("ref").url
  private val keywordsURL = routes.CaseController.keywordsDetails("ref").url

  private val `case` = mock[Case]

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(`case`)
  }

  "Case Heading" should {

    "Render Trader" in {
      // Given
      given(`case`.reference) willReturn "ref"

      // When
      val doc = view(case_nav(CaseDetailPage.TRADER, `case`))

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
      // Given
      given(`case`.reference) willReturn "ref"

      // When
      val doc = view(case_nav(CaseDetailPage.APPLICATION_DETAILS, `case`))

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
      // Given
      given(`case`.reference) willReturn "ref"

      // When
      val doc = view(case_nav(CaseDetailPage.RULING, `case`))

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
      // Given
      given(`case`.reference) willReturn "ref"

      // When
      val doc = view(case_nav(CaseDetailPage.ATTACHMENTS, `case`))

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
      // Given
      given(`case`.reference) willReturn "ref"

      // When
      val doc = view(case_nav(CaseDetailPage.ACTIVITY, `case`))

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

    "Render Appeal for COMPLETE Cases" in {
      // Given
      given(`case`.reference) willReturn "ref"
      given(`case`.status) willReturn CaseStatus.COMPLETED

      // When
      val doc = view(case_nav(CaseDetailPage.APPEAL, `case`))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText(appeal)
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(6)
      anchors.get(0) should containText(trader)
      anchors.get(0) should haveAttribute("href", traderURL)
      anchors.get(1) should containText(application)
      anchors.get(1) should haveAttribute("href", applicationDetailsURL)
      anchors.get(2) should containText(ruling)
      anchors.get(2) should haveAttribute("href", rulingURL)
      anchors.get(3) should containText(attachments)
      anchors.get(3) should haveAttribute("href", attachmentsURL)
      anchors.get(4) should containText(activity)
      anchors.get(4) should haveAttribute("href", activityURL)
      anchors.get(5) should containText(keywords)
      anchors.get(5) should haveAttribute("href", keywordsURL)
    }

    "Render Appeal for CANCELLED Cases" in {
      // Given
      given(`case`.reference) willReturn "ref"
      given(`case`.status) willReturn CaseStatus.CANCELLED

      // When
      val doc = view(case_nav(CaseDetailPage.APPEAL, `case`))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText(appeal)
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(6)
      anchors.get(0) should containText(trader)
      anchors.get(0) should haveAttribute("href", traderURL)
      anchors.get(1) should containText(application)
      anchors.get(1) should haveAttribute("href", applicationDetailsURL)
      anchors.get(2) should containText(ruling)
      anchors.get(2) should haveAttribute("href", rulingURL)
      anchors.get(3) should containText(attachments)
      anchors.get(3) should haveAttribute("href", attachmentsURL)
      anchors.get(4) should containText(activity)
      anchors.get(4) should haveAttribute("href", activityURL)
      anchors.get(5) should containText(keywords)
      anchors.get(5) should haveAttribute("href", keywordsURL)
    }

    "Not render Appeal for other Statuses" in {
      // Given
      given(`case`.reference) willReturn "ref"
      given(`case`.status) willReturn CaseStatus.OPEN

      // When
      val doc = view(case_nav(CaseDetailPage.APPEAL, `case`))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(0)

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(6)
      anchors.get(0) should containText(trader)
      anchors.get(0) should haveAttribute("href", traderURL)
      anchors.get(1) should containText(application)
      anchors.get(1) should haveAttribute("href", applicationDetailsURL)
      anchors.get(2) should containText(ruling)
      anchors.get(2) should haveAttribute("href", rulingURL)
      anchors.get(3) should containText(attachments)
      anchors.get(3) should haveAttribute("href", attachmentsURL)
      anchors.get(4) should containText(activity)
      anchors.get(4) should haveAttribute("href", activityURL)
      anchors.get(5) should containText(keywords)
      anchors.get(5) should haveAttribute("href", keywordsURL)
    }
  }
}
