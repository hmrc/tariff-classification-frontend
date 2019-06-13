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

import org.jsoup.select.Elements
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.tariffclassificationfrontend.controllers.routes
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.case_nav_liability
import uk.gov.hmrc.tariffclassificationfrontend.views.{CaseDetailPage, ViewSpec}

class CaseNavLiabilityViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private val liability = "Liability"
  private val sample = "Sample"
  private val attachments = "Attachments"
  private val activity = "Activity"
  private val appeal = "Appeal"

  private val liabilityURL = routes.LiabilityController.liabilityDetails("ref").url
  private val sampleDetailsURL = routes.CaseController.sampleDetails("ref").url
  private val attachmentsURL = routes.AttachmentsController.attachmentsDetails("ref").url
  private val activityURL = routes.CaseController.activityDetails("ref").url

  private val `case` = mock[Case]

  private val tabOrder : Seq[String] = Seq(liability, sample, attachments, activity, appeal)
  private val urlOrder : Seq[String] = Seq(liabilityURL, sampleDetailsURL, attachmentsURL, activityURL)
  private val tabWithUrl : Seq[(String,String)] = tabOrder.zip(urlOrder)

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(`case`)
  }

  def tabsWithLinksShouldBeActiveExceptGiven(anchors : Elements, exceptTab : String): Unit = {

    val filteredIndexed =  tabWithUrl.filter( entry => entry._1 != exceptTab).zipWithIndex

    filteredIndexed
      .foreach(entry => {
      anchors.get(entry._2) should containText(entry._1._1)
      anchors.get(entry._2) should haveAttribute("href", entry._1._2)
    })
  }

  "Case Heading" should {

    val expectedTabAnchors = 3
    val expectedTabAnchorsForCompletedOrCancelledCases = 4
    val expectedTotalTabIndexes = 5

    "Render Liability Details" in {
      // Given
      given(`case`.reference) willReturn "ref"

      // When
      val doc = view(case_nav_liability(CaseDetailPage.LIABILITY, `case`))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText(liability)
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(expectedTabAnchors)

      tabsWithLinksShouldBeActiveExceptGiven(anchors, liability)
    }

    "Render Sample Details" in {
      // Given
      given(`case`.reference) willReturn "ref"

      // When
      val doc = view(case_nav_liability(CaseDetailPage.SAMPLE_DETAILS, `case`))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText(sample)
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(expectedTabAnchors)

      tabsWithLinksShouldBeActiveExceptGiven(anchors, sample)

    }

    "Render Attachments" in {
      // Given
      given(`case`.reference) willReturn "ref"

      // When
      val doc = view(case_nav_liability(CaseDetailPage.ATTACHMENTS, `case`))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText(attachments)
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(expectedTabAnchors)

      tabsWithLinksShouldBeActiveExceptGiven(anchors, attachments)
    }

    "Render Activity" in {
      // Given
      given(`case`.reference) willReturn "ref"

      // When
      val doc = view(case_nav_liability(CaseDetailPage.ACTIVITY, `case`))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText(activity)
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(expectedTabAnchors)

      tabsWithLinksShouldBeActiveExceptGiven(anchors, activity)
    }

    "Render Appeal for COMPLETE Cases" in {
      // Given
      given(`case`.reference) willReturn "ref"
      given(`case`.status) willReturn CaseStatus.COMPLETED

      // When
      val doc = view(case_nav_liability(CaseDetailPage.APPEAL, `case`))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText(appeal)
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(expectedTabAnchorsForCompletedOrCancelledCases)

      tabsWithLinksShouldBeActiveExceptGiven(anchors, appeal)
    }

    "Render Appeal for CANCELLED Cases" in {
      // Given
      given(`case`.reference) willReturn "ref"
      given(`case`.status) willReturn CaseStatus.CANCELLED

      // When
      val doc = view(case_nav_liability(CaseDetailPage.APPEAL, `case`))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(1)

      val selectedTab = spans.first()
      selectedTab should containText(appeal)
      selectedTab should haveAttribute("aria-selected", "true")

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(expectedTabAnchorsForCompletedOrCancelledCases)

      tabsWithLinksShouldBeActiveExceptGiven(anchors, appeal)
    }

    "Not render Appeal for other Statuses" in {
      // Given
      given(`case`.reference) willReturn "ref"
      given(`case`.status) willReturn CaseStatus.OPEN

      // When
      val doc = view(case_nav_liability(CaseDetailPage.APPEAL, `case`))

      // Then
      val spans = doc.getElementsByTag("span")
      spans should haveSize(0)

      val anchors = doc.getElementsByTag("a")
      anchors should haveSize(expectedTabAnchorsForCompletedOrCancelledCases)

      tabsWithLinksShouldBeActiveExceptGiven(anchors, appeal)
    }

    "Render tabindexes for all tabs" in {
      // Given
      given(`case`.reference) willReturn "ref"
      given(`case`.status) willReturn CaseStatus.COMPLETED //so we can see all tabs

      // When
      val doc = view(case_nav_liability(CaseDetailPage.APPEAL, `case`))

      // Then
      val indexedElements = doc.getElementsByAttribute("tabindex")

      indexedElements should haveSize(expectedTotalTabIndexes)

    }
  }
}
