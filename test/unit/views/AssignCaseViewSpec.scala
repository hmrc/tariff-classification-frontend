/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.routes
import models.Operator
import models.forms.TakeOwnerShipForm
import play.api.data.Form
import utils.Cases._
import views.ViewMatchers._
import views.html.assign_case

class AssignCaseViewSpec extends ViewSpec {

  val form: Form[Boolean] = TakeOwnerShipForm.form

  val assignCaseView: assign_case = app.injector.instanceOf[assign_case]

  "Assign Case" should {

    "Render link to assign - when currently unassigned" in {

      val c = aCase(
        withReference("REF"),
        withoutAssignee()
      )

      val doc = view(assignCaseView(c, form))


      doc shouldNot containElementWithID("assign_case-assigned_heading")

      doc                                  should containElementWithID("take-ownership")
      doc.getElementById("take-ownership") should haveAttribute("action", routes.AssignCaseController.post("REF").url)

      doc should containElementWithID("back-link")
    }

    "Render message - when currently assigned" in {

      val c = aCase(
        withReference("REF"),
        withAssignee(Some(Operator("1")))
      )


      val doc = view(assignCaseView(c, form))


      doc should containElementWithID("assign_case-assigned_heading")
      doc shouldNot containElementWithID("assign_case-unassigned_heading")

      doc shouldNot containElementWithID("assign_case-assign_button")

      doc should containElementWithID("assign_case-continue_button")
      doc.getElementById("assign_case-continue_button") should haveAttribute(
        "href",
        routes.CaseController.get("REF").url
      )
    }
  }
}
