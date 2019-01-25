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

package uk.gov.hmrc.tariffclassificationfrontend.views

import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest._
import org.scalatest.matchers._

object ViewMatchers {

  class ContainElementWithIDMatcher(id: String) extends Matcher[Document] {
    override def apply(left: Document): MatchResult = {
      MatchResult(
        left.getElementById(id) != null,
        s"Document did not contain element with ID {$id}",
        s"Document contained an element with ID {$id}"
      )
    }
  }

  class ContainElementWithTagMatcher(tag: String) extends Matcher[Document] {
    override def apply(left: Document): MatchResult = {
      MatchResult(
        !left.getElementsByTag(tag).isEmpty,
        s"Document did not contain element with Tag {$tag}",
        s"Document contained an element with Tag {$tag}"
      )
    }
  }

  class ElementContainsTextMatcher(content: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      MatchResult(
        left.text().contains(content),
        s"Element did not contain {$content}",
        s"Element contained {$content}"
      )
    }
  }

  class ElementContainsChildWithTextMatcher(tag: String, content: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      MatchResult(
        left.getElementsByTag(tag).text().contains(content),
        s"Element did not contain text {$content}",
        s"Element contained text {$content}"
      )
    }
  }

  class ElementContainsChildWithAttributeMatcher(tag: String, key: String, value: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      MatchResult(
        left.getElementsByTag(tag).attr(key) == value,
        s"Element attribute {$key} had value {${left.attr(key)}}, expected {$value}",
        s"Element attribute {$key} had value {$value}"
      )
    }
  }

  class ElementHasAttributeMatcher(key: String, value: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      MatchResult(
        left.attr(key) == value,
        s"Element attribute {$key} had value {${left.attr(key)}}, expected {$value}",
        s"Element attribute {$key} had value {$value}"
      )
    }
  }

  class ElementHasChildCountMatcher(count: Int) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      MatchResult(
        left.children().size() == count,
        s"Element had child count {${left.children().size()}}, expected {$count}",
        s"Element had child count {$count}"
      )
    }
  }

  class ElementsHasSizeMatcher(size: Int) extends Matcher[Elements] {
    override def apply(left: Elements): MatchResult = {
      MatchResult(
        left.size() == size,
        s"Elements had size {${left.size()}}, expected {$size}",
        s"Elements had size {$size}"
      )
    }
  }

  class ChildMatcherBuilder(tag: String) {
    def containingText(text: String) = new ElementContainsChildWithTextMatcher(tag, text)
    def withAttribute(key: String, value: String)  = new ElementContainsChildWithAttributeMatcher(tag, key, value)
  }

  def containElementWithID(id: String) = new ContainElementWithIDMatcher(id)
  def containElementWithTag(tag: String) = new ContainElementWithTagMatcher(tag)
  def containText(text: String) = new ElementContainsTextMatcher(text)
  def haveSize(size: Int) = new ElementsHasSizeMatcher(size)
  def haveAttribute(key: String, value: String)  = new ElementHasAttributeMatcher(key, value)
  def haveChildCount(count: Int) = new ElementHasChildCountMatcher(count)
  def haveChild(tag: String) = new ChildMatcherBuilder(tag)
}
