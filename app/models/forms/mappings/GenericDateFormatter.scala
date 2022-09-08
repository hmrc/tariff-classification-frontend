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

import play.api.data.FormError

import java.util.regex.Pattern
import java.util.regex.Pattern._

import scala.annotation.tailrec

private[mappings] trait GenericDateFormatter extends Formatters with Constraints {

  val filters: Seq[Pattern] = Seq(
    compile("<script>(.*?)</script>", CASE_INSENSITIVE),
    compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", CASE_INSENSITIVE | MULTILINE | DOTALL),
    compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", CASE_INSENSITIVE | MULTILINE | DOTALL),
    compile("<script(.*?)>", CASE_INSENSITIVE | MULTILINE | DOTALL),
    compile("</script>", CASE_INSENSITIVE),
    compile("eval\\((.*?)\\)", CASE_INSENSITIVE | MULTILINE | DOTALL),
    compile("expression\\((.*?)\\)", CASE_INSENSITIVE | MULTILINE | DOTALL),
    compile("javascript:", CASE_INSENSITIVE),
    compile("vbscript:", CASE_INSENSITIVE),
    compile("onload(.*?)=", CASE_INSENSITIVE | MULTILINE | DOTALL)
  )

  val fieldKeys: List[String]

  protected def getKey(key: String, value: String): String = if (key == null || key.isEmpty) value else s"$key.$value"

  def keyWithError(id: String, error: String): String =
    getKey(id, error)

  val fields: (String, Map[String, String]) => Map[String, Option[String]] = (key, data) =>
    fieldKeys.map(field => field -> data.get(getKey(key, field)).filter(_.nonEmpty).map(f => filter(f))).toMap

  lazy val missingFields: (String, Map[String, String]) => List[String] = (key, data) =>
    fields(key, data)
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

  lazy val illegalFields: (String, Map[String, String]) => List[String] = (key, data) =>
    fields(key, data)
      .withFilter(_._2.getOrElse("").matches("""^(.*[^\d].*)+$"""))
      .map(_._1)
      .toList

  lazy val illegalZero: (String, Map[String, String]) => List[String] = (key, data) =>
    fields(key, data)
      .withFilter(_._2.getOrElse("").matches("""^[0]+$"""))
      .map(_._1)
      .toList

  lazy val illegalErrors
    : (String, Map[String, String], String, Seq[String], (String, Map[String, String]) => List[String]) => Option[
      FormError
    ] =
    (key, data, invalidKey, args, validate) =>
      validate(key, data) match {
        case emptyList if emptyList.isEmpty => None
        case foundErrors =>
          Some(FormError(keyWithError(key, validate(key, data).head), invalidKey, foundErrors ++ args))
      }

  def leftErrors(
    key: String,
    data: Map[String, String],
    missingMessage: String,
    invalidMessage: String,
    args: Seq[String]
  ): Left[Seq[FormError], Nothing] =
    Left(
      List(
        FormError(keyWithError(key, missingFields(key, data).head), missingMessage, missingFields(key, data) ++ args)
      )
        ++ illegalErrors(key, data, invalidMessage, args, illegalFields) ++ illegalErrors(
        key,
        data,
        invalidMessage,
        args,
        illegalZero
      )
    )

  def filter(input: String): String = {
    @tailrec
    def applyFilters(filters: Seq[Pattern], sanitizedOuput: String): String = filters match {
      case Nil            => sanitizedOuput.filterNot(_ == '|')
      case filter :: tail => applyFilters(tail, filter.matcher(sanitizedOuput).replaceAll(""))
    }
    applyFilters(filters, input)
  }
}
