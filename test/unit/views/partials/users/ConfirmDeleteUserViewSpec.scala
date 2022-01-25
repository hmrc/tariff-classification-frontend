/*
 * Copyright 2022 HM Revenue & Customs
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

package views.partials.users

import models.Operator
import models.forms.v2.RemoveUserForm
import play.api.data.Form
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.users.confirm_delete_user

class ConfirmDeleteUserViewSpec extends ViewSpec {

  def confirmDeleteUser: confirm_delete_user = injector.instanceOf[confirm_delete_user]
  val userWithNoNameAndNoTeam                = Operator("1")
  val form: Form[Boolean]                    = RemoveUserForm.form

  "cannotDeleteUser View" should {
    "render successfully" in {
      val doc = view(
        confirmDeleteUser(
          userWithNoNameAndNoTeam,
          form
        )
      )
      doc should containElementWithTag("h1")
      doc should containElementWithID("remove-user")
      doc should containElementWithID("remove-user-button")
      doc should containElementWithClass("govuk-warning-text")
      doc should containElementWithTag("strong")
    }
  }
}
