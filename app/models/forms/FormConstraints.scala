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

package models.forms

import java.time.{Clock, Instant, LocalDateTime, ZoneId}

import play.api.data.validation.{Constraint, Invalid, Valid}

import scala.util.matching.Regex

object FormConstraints {

  val numbersOnlyRegex: Regex       = """^\d+$""".r
  val numbersAndLettersRegex: Regex = """[A-Za-z0-9]+""".r
  val btiRefRegex: Regex            = """[0-9]{6,22}""".r

  val validCommodityCodeSearch: Constraint[String] = Constraint("constraints.commoditycode")({
    case s: String if s.matches("[0-9]{2,22}") => Valid
    case _: String                             => Invalid("Commodity code must be empty or numeric between 2 and 22 digits")
  })

  def dateMustBeInThePast(error: String): Constraint[Instant] =
    Constraint(error)({
      case s: Instant if s.isBefore(Instant.now(Clock.systemUTC)) => Valid
      case _                                                      => Invalid(error)
    })

  def dateLowerBound(error: String, minimumValidYear: Int): Constraint[Instant] =
    Constraint(error, minimumValidYear.toString)({
      case s: Instant if LocalDateTime.ofInstant(s, ZoneId.systemDefault()).getYear >= minimumValidYear => Valid
      case _                                                                                            => Invalid(error, minimumValidYear.toString)
    })

  def btiReferenceIsCorrectFormat(): Constraint[String] =
    regexp(btiRefRegex, "case.v2.liability.c592.details_edit.bti_reference_error")

  def entryNumberIsNumbersAndLettersOnly(): Constraint[String] =
    regexp(numbersAndLettersRegex, "case.liability.error.entry-number")

  def dvrNumberIsNumberAndLettersOnly(): Constraint[String] =
    regexp(numbersAndLettersRegex, "case.liability.error.dvr-number")

  def emptyOr(c: Constraint[String]*): Seq[Constraint[String]] = c.map { c =>
    Constraint[String]("constraints.empty")({
      case s: String if s.isEmpty => Valid
      case s: String              => c.apply(s)
    })
  }

  def defined[T](key: String): Constraint[Option[T]] = Constraint {
    case option: Option[T] if option.isDefined => Valid
    case _                                     => Invalid(key)
  }

  def regexp(regex: Regex, errorKey: String): Constraint[String] =
    Constraint {
      case str if regex.pattern.matcher(str).matches() =>
        Valid
      case _ =>
        Invalid(errorKey, regex.pattern.pattern())
    }

}
