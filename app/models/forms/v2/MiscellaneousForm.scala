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

package models.forms.v2

import models.MiscCaseType.MiscCaseType
import models.forms.FormUtils.textTransformingTo
import models.{Contact, MiscApplication, MiscCaseType}
import play.api.data.{Form, Forms, Mapping}
import play.api.data.Forms._
import models.forms.mappings.FormMappings._

object MiscellaneousForm {

  private val miscTypeMapping: Mapping[MiscCaseType] = Forms.mapping[MiscCaseType, MiscCaseType](
    "caseType" -> textTransformingTo(MiscCaseType.withName, _.toString, "error.empty.miscCaseType")
  )(identity)(Some(_))

  private val form2Misc: (String, String) => MiscApplication = {
    case (shortDescr, contactName) =>
      MiscApplication(
        contact = Contact("", contactName, None),
        offline = false,
        name = "",
        contactName = Some(contactName),
        caseType = MiscCaseType.IB,
        detailedDescription = Some(shortDescr),
        sampleToBeProvided = false,
        sampleToBeReturned = false,
        messagesLogged = List.empty
      )
  }

    private val misc2Form: MiscApplication => Option[(String, String, MiscCaseType)] = misc =>
      Some((misc.detailedDescription.getOrElse(""), misc.contactName.getOrElse(""), misc.caseType))

    val newMiscForm: Form[MiscApplication] =
      mapping(
        "detailedDescription"       -> textNonEmpty("Please enter a short case description"),
        "contactName"      -> textNonEmpty("Please enter a case contact name"),
        "caseType" -> oneOf("error.empty.miscCaseType", MiscCaseType)
      )(form2Misc)(misc2Form)
    )
}