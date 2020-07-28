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
import views.ViewMatchers._
import views.html.accessibility_view

class AccessibilityViewSpec extends ViewSpec{

  val accessibility_view = new views.html.accessibility_view()

  val messageKeyPrefix = "accessibility"
  val expectTimeoutDialog = false
  def asDocument(html: Html): Document = Jsoup.parse(html.toString())
  def assertLinkContainsHref(doc: Document, id: String, href: String): Assertion = {
    assert(doc.getElementById(id) != null, s"\n\nElement $id is not present")
    assert(doc.getElementById(id).attr("href").contains(href))
  }

  def view = () => accessibility_view()
 
  protected def normalPage(view: () => HtmlFormat.Appendable, messageKeyPrefix: String, messageHeadingArgs: Any*)
                          (expectedGuidanceKeys: String*): Unit = {

    "AccessibilityView view" must {
      behave like normalPage(view, messageKeyPrefix)()
      //behave like pageWithoutBackLink(view)

      "contain a link to gov Accessibility Help page" in {
        val doc = asDocument(view())
        assertLinkContainsHref(doc, "govuk_accessibility_url", appConfig.govukAccessibilityUrl)
      }

      "contain a link to gov tax service Binding Tariff Information" in {
        val doc = asDocument(view())
        assertLinkContainsHref(doc, "govuk_taxservice_subdomain", appConfig.subdomainUrl)
      }

      "contain a link to Ability Net page" in {
        val doc = asDocument(view())
        assertLinkContainsHref(doc, "abilityNet_url", appConfig.abilityNetUrl)
      }

      "contain a link to Web Content Accessibility Guidelines page" in {
        val doc = asDocument(view())
        assertLinkContainsHref(doc, "webstandard_url", appConfig.webStandards)
      }

      "contain a link to email regarding Accessibility Problem" in {
        val doc = asDocument(view())
        assertLinkContainsHref(doc, "email_link", appConfig.reportEmail)
      }

      "contain a link to Equality Advisory and Support Service page" in {
        val doc = asDocument(view())
        assertLinkContainsHref(doc, "equality_advisory", appConfig.equalityadvisoryservice)
      }

      "contain a link to Equality Commission for Northern Ireland page" in {
        val doc = asDocument(view())
        assertLinkContainsHref(doc, "equality_commission", appConfig.equalityni)
      }

      "contain a link to Get help from HMRC" in {
        val doc = asDocument(view())
        assertLinkContainsHref(doc, "get_help", appConfig.extrasupport)
      }

      "contain a link to Digital Accessibility Centre" in {
        val doc = asDocument(view())
        assertLinkContainsHref(doc, "digital_centre", appConfig.digitalcentre)
      }

      "contain correct number of bullet points display on the page" in {
        val doc = asDocument(view())
        assert(doc.getElementsByClass("acceList_item").eachText().size() == 12 ,"expected list of elements is 12")
      }

    }

  }

}