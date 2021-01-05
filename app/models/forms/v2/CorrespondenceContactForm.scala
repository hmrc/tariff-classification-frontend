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

package models.forms.v2

import models._
import models.forms.mappings.Constraints
import models.forms.mappings.FormMappings._
import play.api.data.Form
import play.api.data.Forms._

object CorrespondenceContactForm extends Constraints {

  def correspondenceContactForm(existingCorrespondence: Case): Form[Case] =
    Form[Case](
      mapping[
        Case,
        Option[String],
        String,
        String,
        Option[String],
        Option[String],
        String,
        String,
        Option[String],
        Option[String],
        Option[String]
      ](
        "correspondenceStarter" -> optional(text),
        "name"                  -> textNonEmpty("can not be empty"),
        "email"                 -> textNonEmpty("can not be empty"),
        "phone"                 -> optional(text),
        "fax"                   -> optional(text),
        "buildingAndStreet"     -> textNonEmpty("can not be empty"),
        "townOrCity"            -> textNonEmpty("can not be empty"),
        "county"                -> optional(text),
        "postCode"              -> optional(text),
        "agentName"             -> optional(text)
      )(form2Correspondence(existingCorrespondence))(correspondence2Form)
    ).fillAndValidate(existingCorrespondence)

  private def form2Correspondence(existingCase: Case): (
    Option[String],
    String,
    String,
    Option[String],
    Option[String],
    String,
    String,
    Option[String],
    Option[String],
    Option[String]
  ) => Case = {
    case (
        correspondenceStarter,
        name,
        email,
        phone,
        fax,
        buildingAndStreet,
        townOrCity,
        county,
        postCode,
        agentName
        ) =>
      existingCase.copy(
        application = existingCase.application.asCorrespondence.copy(
          correspondenceStarter = correspondenceStarter,
          agentName             = agentName,
          address               = Address(buildingAndStreet, townOrCity, county, postCode),
          contact               = Contact(name, email, phone),
          fax                   = fax
        )
      )
  }

  private def correspondence2Form(existingCase: Case): Option[
    (
      Option[String],
      String,
      String,
      Option[String],
      Option[String],
      String,
      String,
      Option[String],
      Option[String],
      Option[String]
    )
  ] = {
    val existingCorrespondence = existingCase.application.asCorrespondence

    Some(
      (
        existingCorrespondence.correspondenceStarter,
        existingCorrespondence.contact.name,
        existingCorrespondence.contact.email,
        existingCorrespondence.contact.phone,
        existingCorrespondence.fax,
        existingCorrespondence.address.buildingAndStreet,
        existingCorrespondence.address.townOrCity,
        existingCorrespondence.address.county,
        existingCorrespondence.address.postCode,
        existingCorrespondence.agentName
      )
    )
  }
}
