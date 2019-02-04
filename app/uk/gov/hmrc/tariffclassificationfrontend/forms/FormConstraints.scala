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

package uk.gov.hmrc.tariffclassificationfrontend.forms

import play.api.data.validation.{Constraint, Invalid, Valid}

object FormConstraints {

  val validCommodityCode: Constraint[String] = Constraint("constraints.commoditycode")({
    case s: String if s.matches("[0-9]{6,22}") => Valid
    case _: String => Invalid("Format must be empty or numeric between 6 and 22 digits")
  })

  val numeric: Constraint[String] = Constraint("constraints.non-numeric")({
    case s: String if s forall Character.isDigit => Valid
    case _ => Invalid("Must be numerical")
  })

  def minLength(length: Int): Constraint[String] = Constraint("constraints.minlength")({
    case s: String if s.length >= length => Valid
    case _: String => Invalid(s"Must be at least $length characters")
  })

  def emptyOr(c: Constraint[String]*): Seq[Constraint[String]] = c.map { c =>
    Constraint[String]("constraints.empty")({
      case s: String if s.isEmpty => Valid
      case s: String => c.apply(s)
    })
  }

}
