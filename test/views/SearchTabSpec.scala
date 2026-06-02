package views

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class SearchTabSpec extends AnyWordSpec with Matchers {

  import SearchTab._

  "SearchTab QueryStringBindable" should {

    "bind 'details' correctly" in {
      val result = searchTypeQueryStringBinder.bind("tab", Map("tab" -> Seq("details")))
      result shouldBe Some(Right(SearchTab.DETAILS))
    }

    "bind 'images' correctly" in {
      val result = searchTypeQueryStringBinder.bind("tab", Map("tab" -> Seq("images")))
      result shouldBe Some(Right(SearchTab.IMAGES))
    }

    "bind 'searchbox' correctly" in {
      val result = searchTypeQueryStringBinder.bind("tab", Map("tab" -> Seq("searchbox")))
      result shouldBe Some(Right(SearchTab.SEARCH_BOX))
    }

    "fail to bind invalid value" in {
      val result = searchTypeQueryStringBinder.bind("tab", Map("tab" -> Seq("invalid")))

      result shouldBe defined
      result.get.isLeft shouldBe true
    }

    "unbind values correctly" in {
      searchTypeQueryStringBinder.unbind("tab", SearchTab.DETAILS) shouldBe "tab=details"
      searchTypeQueryStringBinder.unbind("tab", SearchTab.IMAGES) shouldBe "tab=images"
      searchTypeQueryStringBinder.unbind("tab", SearchTab.SEARCH_BOX) shouldBe "tab=searchbox"
    }
  }
}