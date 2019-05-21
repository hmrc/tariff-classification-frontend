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

import uk.gov.hmrc.tariffclassificationfrontend.forms.AppealForm
import uk.gov.hmrc.tariffclassificationfrontend.models.AppealType
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.html.appeal_choose_type
import uk.gov.tariffclassificationfrontend.utils.Cases

class AppealChooseTypeViewSpec extends ViewSpec {

  "Appeal Choose Type" should {

    "Render value for each AppealType" in {
      // When
      val c = Cases.btiCaseWithExpiredRuling
      val doc = view(appeal_choose_type(c, AppealForm.appealTypeForm))

      // Then

      AppealType.values.foreach(at => {
        val id = s"appeal_type-$at"
        doc should containElementWithID(id)
        doc.getElementById(id) should haveAttribute("value", at.toString)
      })
    }

  }


}
