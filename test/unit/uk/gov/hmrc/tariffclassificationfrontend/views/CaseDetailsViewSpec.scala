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

package uk.gov.hmrc.tariffclassificationfrontend.views

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.tariffclassificationfrontend.controllers.ActiveTab
import uk.gov.hmrc.tariffclassificationfrontend.models.request.AuthenticatedRequest
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, Decision, Operator, Permission}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.tariffclassificationfrontend.utils.Cases._

class CaseDetailsViewSpec extends ViewSpec {

  private val completeDecision = Decision(
    bindingCommodityCode = "040900",
    justification = "justification-content",
    goodsDescription = "goods-description",
    methodSearch = Some("method-to-search"),
    explanation = Some("explanation")
  )

  private def request[A](operator: Operator, request: Request[A]) = new AuthenticatedRequest(operator, request)

  "Case Details View" should {

    "render BTI application" in {

      // When
      val c = aCase(withReference("reference"), withBTIApplication)
      val doc = view(html.case_details(c, CaseDetailPage.TRADER, Html("html"), Some(ActiveTab.Applicant)))

      // Then
      val listItems: Elements = doc.getElementsByClass("tabs__list-item")

      listItems.size() shouldBe 7
      listItems.first should containText("Applicant")
      haveAttribute("aria-selected", "true")
    }

    "render liability order" in {

      // When
      val c = aCase(withReference("reference"), withLiabilityApplication())
      val doc = view(html.case_details(c, CaseDetailPage.LIABILITY, Html("html"), Some(ActiveTab.Liability)))

      // Then
      val listItems: Elements = doc.getElementsByClass("tabs__list-item")

      listItems.size() shouldBe 4
      listItems.first should containText("Liability")
      haveAttribute("aria-selected", "true")
    }

    "show the complete case button if case is open and operator has permissions to complete the case" in {

      // When
      val myCase = aCase(withReference("reference"), withBTIApplication, withStatus(CaseStatus.OPEN))
      val c = myCase.copy(decision = Some(completeDecision))
      val doc = view(html.case_details(c, CaseDetailPage.RULING, Html("html"), None)(requestWithPermissions(Permission.COMPLETE_CASE), messages, appConfig))

      // Then
      val item: Element = doc.getElementById("change-case-status-button")
      item should containText("Change case status")

    }

    "not show the complete button if a case status is completed" in {

      //When
      val myCase = aCase(withReference("reference"), withBTIApplication, withStatus(CaseStatus.COMPLETED))
      val c = myCase.copy(decision = Some(completeDecision))

      //Then
      val doc = view(html.case_details(c, CaseDetailPage.RULING, Html("html"), None)(requestWithPermissions(Permission.COMPLETE_CASE), messages, appConfig))
      val item: Element = doc.getElementById("change-case-status-button")
      item shouldNot containText("Change case status")

    }

    "not show the complete button if operator does not have permissions to complete the case" in {

      // When
      val myCase = aCase(withReference("reference"), withBTIApplication, withStatus(CaseStatus.OPEN))
      val c = myCase.copy(decision = Some(completeDecision))
      val doc = view(html.case_details(c, CaseDetailPage.RULING, Html("html"), None)(requestWithPermissions(Permission.VIEW_CASES), messages, appConfig))

      //Then
      val item: Element = doc.getElementById("case-status")
      item should containText("OPEN")
    }

    "show the Reopen case button if the case status is suspended and the operator has the permission to reopen the case" in {

      val myCase = aCase(withReference("reference"), withBTIApplication, withStatus(CaseStatus.SUSPENDED))
      val doc = view(html.case_details(myCase, CaseDetailPage.APPLICATION_DETAILS, Html("html"))(requestWithPermissions(Permission.REOPEN_CASE), messages, appConfig))

      val item: Element = doc.getElementById("reopen-case-button")
      item should containText("Reopen case")
      item shouldNot containText("Change case status")
    }

    "not show the Reopen case button if the case status is not suspended" in {

      val myCase = aCase(withReference("reference"), withBTIApplication, withStatus(CaseStatus.OPEN))
      val doc = view(html.case_details(myCase, CaseDetailPage.APPLICATION_DETAILS, Html("html"))(requestWithPermissions(Permission.COMPLETE_CASE), messages, appConfig))

      val item: Element = doc.getElementById("change-case-status-button")
      item shouldNot containText("Reopen case")
      item should containText("Change case status")

    }

    "not show the Reopen case button if the case in Suspended and the operator does not have the required permission" in {

      val myCase = aCase(withReference("reference"), withBTIApplication, withStatus(CaseStatus.SUSPENDED))
      val doc = view(html.case_details(myCase, CaseDetailPage.APPLICATION_DETAILS, Html("html"))(requestWithPermissions(Permission.VIEW_CASES), messages, appConfig))

      val item: Element = doc.getElementById("reopen-case-button")
      item shouldNot containText("Reopen case")
    }

    "show the Take off referral button if the case status is referral and the operator has the permission to reopen the case" in {

      val myCase = aCase(withReference("reference"), withBTIApplication, withStatus(CaseStatus.REFERRED))
      val doc = view(html.case_details(myCase, CaseDetailPage.APPLICATION_DETAILS, Html("html"))(requestWithPermissions(Permission.REOPEN_CASE), messages, appConfig))

      val item: Element = doc.getElementById("take-off-referral-button")
      item should containText("Take off referral")
      item shouldNot containText("Change case status")
    }

    "not show the Take off referral button if the case status is not suspended" in {

      val myCase = aCase(withReference("reference"), withBTIApplication, withStatus(CaseStatus.OPEN))
      val doc = view(html.case_details(myCase, CaseDetailPage.APPLICATION_DETAILS, Html("html"))(requestWithPermissions(Permission.COMPLETE_CASE), messages, appConfig))

      val item: Element = doc.getElementById("change-case-status-button")
      item shouldNot containText("Take off referral")
      item should containText("Change case status")

    }

    "not show the Take off referral if the case status is referral and the operator does not have the required permission" in {

      val myCase = aCase(withReference("reference"), withBTIApplication, withStatus(CaseStatus.REFERRED))
      val doc = view(html.case_details(myCase, CaseDetailPage.APPLICATION_DETAILS, Html("html"))(requestWithPermissions(Permission.VIEW_CASES), messages, appConfig))

      val item: Element = doc.getElementById("take-off-referral-button")
      item shouldNot containText("Take off referral")
    }
  }

}
