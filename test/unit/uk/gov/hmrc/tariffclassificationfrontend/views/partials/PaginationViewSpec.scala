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

import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.Call
import uk.gov.hmrc.tariffclassificationfrontend.models._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers.{containElementWithID, _}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.partials.pagination

class PaginationViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private val goToPage: Int => Call = mock[Int => Call]

  override def beforeEach(): Unit = {
    def returnThePage: Answer[Call] = {
      new Answer[Call] {
        override def answer(invocation: InvocationOnMock): Call = Call(method = "GET", url = "/page=" + invocation.getArgument(0))
      }
    }

    given(goToPage.apply(ArgumentMatchers.any[Int])) will returnThePage
  }

  "Pagination" should {

    "Render empty page" in {
      // When
      val doc = view(pagination(
        id = "ID",
        pager = Paged(Seq.empty[String], pageIndex = 1, pageSize = 1, resultCount = 0),
        onChange = goToPage
      ))

      // Then
      doc should containElementWithID("ID-pagination-none")
      doc shouldNot containElementWithID("ID-pagination-some")
    }

    "Render 1 page" in {
      // When
      val doc = view(pagination(
        id = "ID",
        pager = Paged(Seq("", ""), pageIndex = 1, pageSize = 2, resultCount = 2),
        onChange = goToPage
      ))

      // Then
      doc should containElementWithID("ID-pagination-some")
      doc shouldNot containElementWithID("ID-pagination-none")

      doc should containElementWithID("ID-pagination-start")
      doc.getElementById("ID-pagination-start") should containText("1")
      doc should containElementWithID("ID-pagination-end")
      doc.getElementById("ID-pagination-end") should containText("2")
      doc should containElementWithID("ID-pagination-total")
      doc.getElementById("ID-pagination-total") should containText("2")

      doc shouldNot containElementWithID("ID-pagination-page_back")
      doc shouldNot containElementWithID("ID-pagination-page_next")

      doc shouldNot containElementWithID("ID-pagination-page_2")
      doc shouldNot containElementWithID("ID-pagination-page_3")
      doc shouldNot containElementWithID("ID-pagination-page_4")
      doc shouldNot containElementWithID("ID-pagination-page_5")
      doc shouldNot containElementWithID("ID-pagination-page_6")
    }

    "Render 1 partially full page" in {
      // When
      val doc = view(pagination(
        id = "ID",
        pager = Paged(Seq(""), pageIndex = 1, pageSize = 2, resultCount = 1),
        onChange = goToPage
      ))

      // Then
      doc should containElementWithID("ID-pagination-some")
      doc shouldNot containElementWithID("ID-pagination-none")

      doc should containElementWithID("ID-pagination-start")
      doc.getElementById("ID-pagination-start") should containText("1")
      doc should containElementWithID("ID-pagination-end")
      doc.getElementById("ID-pagination-end") should containText("1")
      doc should containElementWithID("ID-pagination-total")
      doc.getElementById("ID-pagination-total") should containText("1")

      doc shouldNot containElementWithID("ID-pagination-page_back")
      doc shouldNot containElementWithID("ID-pagination-page_next")

      doc shouldNot containElementWithID("ID-pagination-page_2")
      doc shouldNot containElementWithID("ID-pagination-page_3")
      doc shouldNot containElementWithID("ID-pagination-page_4")
      doc shouldNot containElementWithID("ID-pagination-page_5")
      doc shouldNot containElementWithID("ID-pagination-page_6")
    }

    "Render 2 pages" in {
      // When
      val doc = view(pagination(
        id = "ID",
        pager = Paged(Seq(""), pageIndex = 1, pageSize = 1, resultCount = 2),
        onChange = goToPage
      ))

      // Then
      doc should containElementWithID("ID-pagination-some")
      doc shouldNot containElementWithID("ID-pagination-none")

      doc should containElementWithID("ID-pagination-start")
      doc.getElementById("ID-pagination-start") should containText("1")
      doc should containElementWithID("ID-pagination-end")
      doc.getElementById("ID-pagination-end") should containText("1")
      doc should containElementWithID("ID-pagination-total")
      doc.getElementById("ID-pagination-total") should containText("2")

      doc shouldNot containElementWithID("ID-pagination-page_back")
      doc should containElementWithID("ID-pagination-page_next")
      doc.getElementById("ID-pagination-page_next") should haveAttribute("href", "/page=2")

      doc should containElementWithID("ID-pagination-page_2")
      doc.getElementById("ID-pagination-page_2") should haveAttribute("href", "/page=2")
      doc shouldNot containElementWithID("ID-pagination-page_3")
      doc shouldNot containElementWithID("ID-pagination-page_4")
      doc shouldNot containElementWithID("ID-pagination-page_5")
      doc shouldNot containElementWithID("ID-pagination-page_6")
    }

    "Render 3 pages" in {
      // When
      val doc = view(pagination(
        id = "ID",
        pager = Paged(Seq(""), pageIndex = 1, pageSize = 1, resultCount = 3),
        onChange = goToPage
      ))

      // Then
      doc should containElementWithID("ID-pagination-some")
      doc shouldNot containElementWithID("ID-pagination-none")

      doc should containElementWithID("ID-pagination-start")
      doc.getElementById("ID-pagination-start") should containText("1")
      doc should containElementWithID("ID-pagination-end")
      doc.getElementById("ID-pagination-end") should containText("1")
      doc should containElementWithID("ID-pagination-total")
      doc.getElementById("ID-pagination-total") should containText("3")

      doc shouldNot containElementWithID("ID-pagination-page_back")
      doc should containElementWithID("ID-pagination-page_next")
      doc.getElementById("ID-pagination-page_next") should haveAttribute("href", "/page=2")

      doc should containElementWithID("ID-pagination-page_2")
      doc.getElementById("ID-pagination-page_2") should haveAttribute("href", "/page=2")
      doc should containElementWithID("ID-pagination-page_3")
      doc.getElementById("ID-pagination-page_3") should haveAttribute("href", "/page=3")
      doc shouldNot containElementWithID("ID-pagination-page_4")
      doc shouldNot containElementWithID("ID-pagination-page_5")
      doc shouldNot containElementWithID("ID-pagination-page_6")
    }

    "Render 4 pages" in {
      // When
      val doc = view(pagination(
        id = "ID",
        pager = Paged(Seq(""), pageIndex = 1, pageSize = 1, resultCount = 4),
        onChange = goToPage
      ))

      // Then
      doc should containElementWithID("ID-pagination-some")
      doc shouldNot containElementWithID("ID-pagination-none")

      doc should containElementWithID("ID-pagination-start")
      doc.getElementById("ID-pagination-start") should containText("1")
      doc should containElementWithID("ID-pagination-end")
      doc.getElementById("ID-pagination-end") should containText("1")
      doc should containElementWithID("ID-pagination-total")
      doc.getElementById("ID-pagination-total") should containText("4")

      doc shouldNot containElementWithID("ID-pagination-page_back")
      doc should containElementWithID("ID-pagination-page_next")
      doc.getElementById("ID-pagination-page_next") should haveAttribute("href", "/page=2")

      doc should containElementWithID("ID-pagination-page_2")
      doc.getElementById("ID-pagination-page_2") should haveAttribute("href", "/page=2")
      doc should containElementWithID("ID-pagination-page_3")
      doc.getElementById("ID-pagination-page_3") should haveAttribute("href", "/page=3")
      doc should containElementWithID("ID-pagination-page_4")
      doc.getElementById("ID-pagination-page_4") should haveAttribute("href", "/page=4")
      doc shouldNot containElementWithID("ID-pagination-page_5")
      doc shouldNot containElementWithID("ID-pagination-page_6")
    }

    "Render 5 pages" in {
      // When
      val doc = view(pagination(
        id = "ID",
        pager = Paged(Seq(""), pageIndex = 1, pageSize = 1, resultCount = 5),
        onChange = goToPage
      ))

      // Then
      doc should containElementWithID("ID-pagination-some")
      doc shouldNot containElementWithID("ID-pagination-none")

      doc should containElementWithID("ID-pagination-start")
      doc.getElementById("ID-pagination-start") should containText("1")
      doc should containElementWithID("ID-pagination-end")
      doc.getElementById("ID-pagination-end") should containText("1")
      doc should containElementWithID("ID-pagination-total")
      doc.getElementById("ID-pagination-total") should containText("5")

      doc shouldNot containElementWithID("ID-pagination-page_back")
      doc should containElementWithID("ID-pagination-page_next")
      doc.getElementById("ID-pagination-page_next") should haveAttribute("href", "/page=2")

      doc should containElementWithID("ID-pagination-page_2")
      doc.getElementById("ID-pagination-page_2") should haveAttribute("href", "/page=2")
      doc should containElementWithID("ID-pagination-page_3")
      doc.getElementById("ID-pagination-page_3") should haveAttribute("href", "/page=3")
      doc should containElementWithID("ID-pagination-page_4")
      doc.getElementById("ID-pagination-page_4") should haveAttribute("href", "/page=4")
      doc should containElementWithID("ID-pagination-page_5")
      doc.getElementById("ID-pagination-page_5") should haveAttribute("href", "/page=5")
      doc shouldNot containElementWithID("ID-pagination-page_6")
    }

    "Render more pages" in {
      // When
      val doc = view(pagination(
        id = "ID",
        pager = Paged(Seq(""), pageIndex = 1, pageSize = 1, resultCount = 100),
        onChange = goToPage
      ))

      // Then
      doc should containElementWithID("ID-pagination-some")
      doc shouldNot containElementWithID("ID-pagination-none")

      doc should containElementWithID("ID-pagination-start")
      doc.getElementById("ID-pagination-start") should containText("1")
      doc should containElementWithID("ID-pagination-end")
      doc.getElementById("ID-pagination-end") should containText("1")
      doc should containElementWithID("ID-pagination-total")
      doc.getElementById("ID-pagination-total") should containText("100")

      doc shouldNot containElementWithID("ID-pagination-page_back")
      doc should containElementWithID("ID-pagination-page_next")
      doc.getElementById("ID-pagination-page_next") should haveAttribute("href", "/page=2")

      doc should containElementWithID("ID-pagination-page_2")
      doc.getElementById("ID-pagination-page_2") should haveAttribute("href", "/page=2")
      doc should containElementWithID("ID-pagination-page_3")
      doc.getElementById("ID-pagination-page_3") should haveAttribute("href", "/page=3")
      doc should containElementWithID("ID-pagination-page_4")
      doc.getElementById("ID-pagination-page_4") should haveAttribute("href", "/page=4")
      doc should containElementWithID("ID-pagination-page_5")
      doc.getElementById("ID-pagination-page_5") should haveAttribute("href", "/page=5")

      doc shouldNot containElementWithID("ID-pagination-page_6")
    }

    "Render 1 previous page" in {
      // When
      val doc = view(pagination(
        id = "ID",
        pager = Paged(Seq(""), pageIndex = 2, pageSize = 1, resultCount = 2),
        onChange = goToPage
      ))

      // Then
      doc should containElementWithID("ID-pagination-some")
      doc shouldNot containElementWithID("ID-pagination-none")

      doc should containElementWithID("ID-pagination-start")
      doc.getElementById("ID-pagination-start") should containText("2")
      doc should containElementWithID("ID-pagination-end")
      doc.getElementById("ID-pagination-end") should containText("2")
      doc should containElementWithID("ID-pagination-total")
      doc.getElementById("ID-pagination-total") should containText("2")

      doc should containElementWithID("ID-pagination-page_back")
      doc.getElementById("ID-pagination-page_back") should haveAttribute("href", "/page=1")
      doc shouldNot containElementWithID("ID-pagination-page_next")

      doc should containElementWithID("ID-pagination-page_1")
      doc shouldNot containElementWithID("ID-pagination-page_2")
      doc shouldNot containElementWithID("ID-pagination-page_3")
      doc shouldNot containElementWithID("ID-pagination-page_4")
      doc shouldNot containElementWithID("ID-pagination-page_5")
      doc shouldNot containElementWithID("ID-pagination-page_6")
    }


  }

}
