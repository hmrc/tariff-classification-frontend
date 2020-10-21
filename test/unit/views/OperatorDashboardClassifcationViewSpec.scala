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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.twirl.api.{Html, HtmlFormat}
import views.html.operator_dashboard_classification

class OperatorDashboardClassifcationViewSpec extends ViewSpec {

  val operator_dashboard_calssification = new operator_dashboard_classification()

  val messageKeyPrefix = "accessibility"
  val expectTimeoutDialog = false
  def asDocument(html: Html): Document = Jsoup.parse(html.toString())
  def assertLinkContainsHref(doc: Document, id: String, href: String): Assertion = {
    assert(doc.getElementById(id) != null, s"\n\nElement $id is not present")
    assert(doc.getElementById(id).attr("href").contains(href))
  }

  def view = () => operator_dashboard_calssification()

  protected def normalPage(view: () => HtmlFormat.Appendable, messageKeyPrefix: String, messageHeadingArgs: Any*)
                          (expectedGuidanceKeys: String*): Unit = {

    "OperatorDashboardClassifcationView view" must {
      behave like normalPage(view, messageKeyPrefix)()

      "contain a link to my cases" in {
        val doc = asDocument(view())
        assertLinkContainsHref(doc, "my-cases", "my-cases")
      }

      "contain a link to my Referred cases" in {
        val doc = asDocument(view())
        assertLinkContainsHref(doc, "my-referred-cases", "my-referred-cases")
      }


    }
  }

}
