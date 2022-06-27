/*
 * Copyright 2022 HM Revenue & Customs
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

package models.forms.mappings

import play.api.data.validation.{Constraint, Invalid, Valid}
import utils.PostcodeValidator

import java.time.LocalDate
import scala.util.matching.Regex

trait Constraints {

  private val postCodeMaxLength = 8

  private val emailRegex =
    """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint { input =>
      constraints
        .map(_.apply(input))
        .find(_ != Valid)
        .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint { input =>
      import ev._

      if (input >= minimum) {
        Valid
      } else {
        Invalid(errorKey, minimum)
      }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint { input =>
      import ev._

      if (input <= maximum) {
        Valid
      } else {
        Invalid(errorKey, maximum)
      }
    }

  def regexp(regex: Regex, errorKey: String): Constraint[String] =
    Constraint {
      case str if regex.pattern.matcher(str).matches() =>
        Valid
      case _ =>
        Invalid(errorKey, regex.pattern.pattern())
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

  protected def customNonEmpty(errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length > 0 =>
        Valid
      case _ =>
        Invalid(errorKey)
    }

  protected def validEmail(errorKey: String): Constraint[String] =
    Constraint {
      case str if str.isEmpty =>
        Valid
      case email if !email.isEmpty && emailRegex.findFirstMatchIn(email.trim).nonEmpty =>
        Valid
      case _ =>
        Invalid(errorKey)
    }

  protected def optionalPostCodeMaxLength(errorKey: String): Constraint[Option[String]] = {
    optionalMaxLength(postCodeMaxLength, errorKey)
  }

  protected def optionalMaxLength(maximum: Int, errorKey: String): Constraint[Option[String]] = {
    Constraint {
      case None => Valid
      case Some(str: String) if str.length <= maximum => Valid
      case _ => Invalid(errorKey, maximum)
    }
  }

  protected def validPostcode(
                               notValidPostcodeErrorKey: String
                             ): Constraint[Option[String]] = {
    Constraint {
      case None => Valid
      case Some(postCode) if PostcodeValidator.validate(postCode) => Valid
      case _ => Invalid(notValidPostcodeErrorKey)
    }
  }


  protected def maxDate(maximum: LocalDate, errorKey: String, args: Any*): Constraint[LocalDate] =
    Constraint {
      case date if date.isAfter(maximum) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }
}
