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

package models.forms

import java.time.{Clock, Instant}

import play.api.data.validation.{Constraint, Invalid, Valid}

object FormConstraints {

  val validCommodityCodeDecision: Constraint[String] = Constraint("constraints.commoditycode")({
    case s: String if s.matches("[0-9]{6,22}") && (s.length % 2 == 0) => Valid
    case _: String => Invalid("Commodity code must be empty or numeric between 6 and 22 digits with an even number of digits")
  })
  val validCommodityCodeSearch: Constraint[String] = Constraint("constraints.commoditycode")({
    case s: String if s.matches("[0-9]{2,22}") => Valid
    case _: String => Invalid("Commodity code must be empty or numeric between 2 and 22 digits")
  })

  def dateMustBeInThePast(error: String = "date.must.be.in.past"): Constraint[Instant] = Constraint(error)({
    case s: Instant if s.isBefore(Instant.now(Clock.systemUTC)) => Valid
    case _ => Invalid(error)
  })

  def emptyOr(c: Constraint[String]*): Seq[Constraint[String]] = c.map { c =>
    Constraint[String]("constraints.empty")({
      case s: String if s.isEmpty => {
        println("_______")
        println("------" * 50)
        println("------" + s)
        Valid
      }
      case s: String => {
        println(" This is the second block")
        println(" ******************** " + s)
        c.apply(s)
      }
    })
  }

  def defined[T](key: String): Constraint[Option[T]] = Constraint {
    case option: Option[T] if option.isDefined => Valid
    case _ => Invalid(key)
  }

}
