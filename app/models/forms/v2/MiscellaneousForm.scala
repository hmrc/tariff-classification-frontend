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

package models.forms.v2

import models.forms.mappings.FormMappings._
import models.{Contact, MiscApplication, MiscCaseType}
import play.api.data.Form
import play.api.data.Forms._

object MiscellaneousForm {

  private val form2Misc: (String, String, String) => MiscApplication = {
    case (shortDescription, contactName, caseType) =>
      MiscApplication(
        contact             = Contact("", "", None),
        name                = shortDescription,
        contactName         = Some(contactName),
        caseType            = MiscCaseType.withName(caseType),
        detailedDescription = None,
        sampleToBeProvided  = false,
        sampleToBeReturned  = false,
        messagesLogged      = List.empty
      )
  }

  private val misc2Form: MiscApplication => Option[(String, String, String)] = misc =>
    Some((misc.name, misc.contactName.getOrElse(""), misc.caseType.toString))

  val newMiscForm: Form[MiscApplication] = Form(
    mapping(
      "name"        -> textNonEmpty("error.empty.misc.shortDesc"),
      "contactName" -> textNonEmpty("error.empty.misc.contactName"),
      "caseType"    -> oneOf("error.empty.miscCaseType", MiscCaseType)
    )(form2Misc)(misc2Form)
  )
}
