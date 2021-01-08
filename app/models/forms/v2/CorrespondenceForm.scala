/*
 * Copyright 2021 HM Revenue & Customs
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

package models.forms
import models.{CorrespondenceApplication, Address, Contact}
import play.api.data.Form
import play.api.data.Forms._
import models.forms.mappings.FormMappings._

object CorrespondenceForm {

  private val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  private val form2Correspondence: (String, String, String) => CorrespondenceApplication = {
    case (shortDescr, source, contactEmail) =>
      CorrespondenceApplication(
        correspondenceStarter = Some(source),
        agentName = None,
        contact = Contact("", contactEmail, None),
        summary = shortDescr,
        detailedDescription = "",
        sampleToBeProvided = false,
        sampleToBeReturned = false,
        address = Address("", "", None, None)
      )
  }

  private val correspondence2Form: CorrespondenceApplication => Option[(String, String, String)] = correspondence =>
    Some((correspondence.summary, correspondence.correspondenceStarter.getOrElse(""), correspondence.contact.email))

  val newCorrespondenceForm: Form[CorrespondenceApplication] = Form(
    mapping(
      "summary"      -> textNonEmpty("Please enter a short description"),
      "source"       -> textNonEmpty("Please enter a case source"),
      "contactEmail" -> text.verifying("case.liability.error.email", e => validEmailFormat(e))
    )(form2Correspondence)(correspondence2Form)
  )

  private def validEmailFormat(email: String): Boolean =
    email.trim.isEmpty || emailRegex.findFirstMatchIn(email.trim).nonEmpty
}
