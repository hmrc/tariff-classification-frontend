/*
 * Copyright 2024 HM Revenue & Customs
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

import models.LiabilityOrder
import models.forms.LiabilityForm
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import views.ViewMatchers.{containElementWithClass, containElementWithID, haveAttribute}
import views.html.create_liability

class CreateLiabilityViewSpec extends ViewSpec {

  private lazy val liabilityOrder: Form[LiabilityOrder] = LiabilityForm.newLiabilityForm

  val createLiabilityView: create_liability = injector.instanceOf[create_liability]

  private lazy val doc = view(createLiabilityView(liabilityOrder))

  def getElementByAttributeValue(html: Document): String =
    html.getElementsByAttributeValue("class", "govuk-list govuk-error-summary__list").text

  def liabilityOrderWithErrors(errorKey: String, errorValue: String): Document = {

    val liabilityOrderWithErrors: Form[LiabilityOrder] =
      LiabilityForm.newLiabilityForm.copy(errors = Seq(FormError.apply(errorKey, errorValue)))

    view(createLiabilityView(liabilityOrderWithErrors))

  }

  "Create Liability View" should {

    "contain a liability status" in {

      val expectedMessage = messages("liability.create_liability.liability-status.heading") + " " +
        messages("liability.create_liability.liability-status.radio.yes") + " " +
        messages("liability.create_liability.liability-status.radio.no")

      doc should containElementWithID("create_liability-liability_status-LIVE")
      doc should containElementWithID("create_liability-liability_status-NON_LIVE")
      doc.getElementById("liability-status").text shouldBe expectedMessage

    }

    "contain a create liability heading" in {

      doc.getElementById("create_liability-heading").text shouldBe messages("page.title.create_liability.h1")

    }

    "contain item name" in {

      doc should containElementWithID("item-name")
      doc.getElementsByAttributeValue("for", "item-name").text shouldBe messages(
        "liability.create_liability.item-name.heading"
      )
      doc.getElementById("item-name-hint").text shouldBe messages("liability.create_liability.item-name.heading.hint")

    }

    "contain trader's name" in {

      doc should containElementWithID("trader-name")
      doc.getElementsByAttributeValue("for", "trader-name").text shouldBe messages(
        "liability.create_liability.trader-name.heading"
      )
      doc.getElementById("trader-name-hint").text shouldBe messages(
        "liability.create_liability.trader-name.heading.hint"
      )

    }

    "contain the correct cancel link" in {

      doc.getElementById("create_liability-cancel_button") should haveAttribute(
        "href",
        "/manage-tariff-classifications"
      )

    }

    "contain the correct buttons" in {

      doc                                                  should containElementWithID("create_liability-button")
      doc                                                  should containElementWithID("create_liability-cancel_button")
      doc.getElementById("create_liability-button").text shouldBe messages("liability.create_liability.confirm-button")
      doc.getElementById("create_liability-cancel_button").text shouldBe messages(
        "liability.create_liability.cancel-button"
      )
    }

    "render the correct errors if item name is missing" in {

      val errorKey             = "item-name-error"
      val errorValue           = "error.empty.item-name"
      val expectedErrorMessage = messages(errorValue)

      val docWithErrors = liabilityOrderWithErrors(errorKey, errorValue)
      docWithErrors                               should containElementWithClass("govuk-error-summary")
      getElementByAttributeValue(docWithErrors) shouldBe expectedErrorMessage

    }

    "render the correct errors if trader name is missing" in {

      val errorKey             = "trader-name-error"
      val errorValue           = "error.empty.trader-name"
      val expectedErrorMessage = messages(errorValue)

      val docWithErrors = liabilityOrderWithErrors(errorKey, errorValue)

      docWithErrors                               should containElementWithClass("govuk-error-summary")
      getElementByAttributeValue(docWithErrors) shouldBe expectedErrorMessage

    }

    "render the correct errors if liability option is missing" in {

      val errorKey             = "liability-status"
      val errorValue           = "error.empty.liability-status"
      val expectedErrorMessage = messages(errorValue)

      val docWithErrors = liabilityOrderWithErrors(errorKey, errorValue)

      docWithErrors                               should containElementWithClass("govuk-error-summary")
      getElementByAttributeValue(docWithErrors) shouldBe expectedErrorMessage

    }

  }
}
