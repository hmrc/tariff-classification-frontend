/*
 * Copyright 2026 HM Revenue & Customs
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

import models.*
import org.mockito.ArgumentMatchers
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Call
import views.ViewMatchers.*
import views.ViewSpec
import views.html.partials.pagination_without_result_counts
import org.mockito.Mockito.*

class PaginationWithoutResultCountsViewSpec extends ViewSpec with MockitoSugar with BeforeAndAfterEach {

  private val goToPage: Int => Call = mock[Int => Call]

  override def beforeEach(): Unit = {

    def returnThePage: Answer[Call] =
      (invocation: InvocationOnMock) => Call(method = "GET", url = "/page=" + invocation.getArgument(0))

    super.beforeEach()
    when(goToPage.apply(ArgumentMatchers.any[Int])).thenAnswer(returnThePage)
  }

  "Pagination Without Result Counts" should {

    "Render empty pager (single page only - no nav)" in {

      val doc = view(
        pagination_without_result_counts(
          id = "ID",
          pager = Paged(Seq.empty[String], pageIndex = 1, pageSize = 10, resultCount = 0),
          onChange = goToPage
        )
      )

      doc shouldNot containElementWithID("ID")
      doc shouldNot containElementWithClass("govuk-pagination")
    }

    "Render single page (no previous/next, no pagination list)" in {

      val doc = view(
        pagination_without_result_counts(
          id = "ID",
          pager = Paged(Seq("a", "b"), pageIndex = 1, pageSize = 10, resultCount = 2),
          onChange = goToPage
        )
      )

      doc shouldNot containElementWithClass("govuk-pagination")
      doc shouldNot containElementWithID("ID")
    }

    "Render 2 pages (show previous/next and page links)" in {

      val doc = view(
        pagination_without_result_counts(
          id = "ID",
          pager = Paged(Seq("a"), pageIndex = 1, pageSize = 1, resultCount = 2),
          onChange = goToPage
        )
      )

      doc should containElementWithClass("govuk-pagination")

      doc shouldNot containElementWithClass("govuk-pagination__prev")

      doc should containElementWithClass("govuk-pagination__next")
      doc
        .getElementsByClass("govuk-pagination__next")
        .first()
        .select("a")
        .attr("href") shouldBe "/page=2"

      doc should containElementWithClass("govuk-pagination__item")
      doc should containText("1")
      doc should containText("2")
    }

    "Render previous link on page 2" in {

      val doc = view(
        pagination_without_result_counts(
          id = "ID",
          pager = Paged(Seq("a"), pageIndex = 2, pageSize = 1, resultCount = 2),
          onChange = goToPage
        )
      )

      doc should containElementWithClass("govuk-pagination__prev")
      doc shouldNot containElementWithClass("govuk-pagination__next")

      doc
        .getElementsByClass("govuk-pagination__prev")
        .first()
        .select("a")
        .attr("href") shouldBe "/page=1"
    }

    "Render surrounding pages without ellipsis when small range" in {

      val doc = view(
        pagination_without_result_counts(
          id = "ID",
          pager = Paged(Seq("a"), pageIndex = 3, pageSize = 1, resultCount = 5),
          onChange = goToPage,
          neighbourPagesAmount = 5
        )
      )

      doc should containElementWithClass("govuk-pagination__list")

      doc should containText("1")
      doc should containText("2")
      doc should containText("3")
      doc should containText("4")
      doc should containText("5")

      doc shouldNot containElementWithClass("govuk-pagination__item--ellipsis")
    }

    "Render ellipsis when pages exceed neighbour window (left and right)" in {

      val doc = view(
        pagination_without_result_counts(
          id = "ID",
          pager = Paged(Seq("a"), pageIndex = 5, pageSize = 1, resultCount = 10),
          onChange = goToPage,
          neighbourPagesAmount = 3
        )
      )

      doc should containText("1")
      doc should containText("10")

      doc should containElementWithClass("govuk-pagination__prev")
      doc should containElementWithClass("govuk-pagination__next")
    }

    "Render current page as span with aria-current" in {

      val doc = view(
        pagination_without_result_counts(
          id = "ID",
          pager = Paged(Seq("a"), pageIndex = 3, pageSize = 1, resultCount = 5),
          onChange = goToPage
        )
      )

      val current = doc.select(".govuk-pagination__item--current span")

      current.text()               shouldBe "3"
      current.attr("aria-current") shouldBe "page"
    }
  }
}
