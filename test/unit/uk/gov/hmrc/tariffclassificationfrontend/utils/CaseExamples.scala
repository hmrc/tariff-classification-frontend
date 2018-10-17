/*
 * Copyright 2018 HM Revenue & Customs
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

package unit.uk.gov.hmrc.tariffclassificationfrontend.utils

import java.time.ZonedDateTime

import uk.gov.hmrc.tariffclassificationfrontend.models
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, Contact, EORIDetails}

object CaseExamples {

  val eoriDetailsExample = EORIDetails("eori", "trader-name", "line1", "line2", "line3", "postcode", "country")
  val contactExample = Contact("name", "email", "phone")
  val applicationExample = models.BTIApplication(eoriDetailsExample, contactExample, None, false, "Laptop", "Personal Computer", None, None, None, None, None, None, false, false)
  val caseExample = Case("1", "OPEN", ZonedDateTime.now(), ZonedDateTime.now(), None, None, None, None, applicationExample, None, Seq())

}
