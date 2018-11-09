/*
 * Copyright 2018 HM Revenue & Customs
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

import org.scalatest._
import matchers._
import org.jsoup.nodes.{Document, Element}

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

  class ElementContainsTextMatcher(content: String) extends Matcher[Element] {
    override def apply(left: Element): MatchResult = {
      MatchResult(
        left.text().contains(content),
        s"Element did not contain {$content}",
        s"Element contained {$content}"
      )
    }
  }

  def containElementWithID(id: String) = new ContainElementWithIDMatcher(id)
  def containText(text: String) = new ElementContainsTextMatcher(text)
}
