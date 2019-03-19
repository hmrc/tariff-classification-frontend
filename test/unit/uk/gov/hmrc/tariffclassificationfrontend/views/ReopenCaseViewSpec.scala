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

import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.tariffclassificationfrontend.controllers.routes
import uk.gov.hmrc.tariffclassificationfrontend.models.Operator
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.html.reopen_case
import uk.gov.tariffclassificationfrontend.utils.Cases._

class ReopenCaseViewSpec extends ViewSpec {

  "Reopen Case" should {

    "Render link to attachments" in {
      // Given
      val c = aCase(
        withReference("REF")
      )

      // When
      val doc = view(reopen_case(c))

      // Then
      doc.getElementById("reopen-case-attachments-link") should haveAttribute("href", routes.AttachmentsController.attachmentsDetails("REF").url)
    }

  }
}
