/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.data.Forms.of
import play.api.data.format.Formatter
import play.api.data.{FieldMapping, FormError}

object FormMappings {

  def fieldNonEmpty(errorKey: String = "error.required"): FieldMapping[String] =
    of(new Formatter[String] {
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
        data.get(key) match {
          case None    => Left(Seq(FormError(key, errorKey)))
          case Some(s) => Right(s)
        }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)
    })

  def textNonEmpty(errorKey: String = "error.required"): FieldMapping[String] =
    of(new Formatter[String] {
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
        data.get(key) match {
          case None | Some("") => Left(Seq(FormError(key, errorKey)))
          case Some(s)         => Right(s)
        }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)
    })

  def oneOf(errorKey: String = "error.required", enumeration: Enumeration): FieldMapping[String] =
    of(new Formatter[String] {
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
        data.get(key) match {
          case Some(s) if enumeration.values.exists(_.toString == s) => Right(s)
          case _                                                     => Left(Seq(FormError(key, errorKey)))
        }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)
    })

  def oneFromList(errorKey: String = "error.required", list: List[String]): FieldMapping[String] =
    of(new Formatter[String] {
      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
        data.get(key) match {
          case Some(s) if list.contains(s) => Right(s)
          case _                           => Left(Seq(FormError(key, errorKey)))
        }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)
    })
}
