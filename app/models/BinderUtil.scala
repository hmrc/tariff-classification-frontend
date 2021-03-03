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

package models

import java.time.Instant

import cats.data.EitherT
import models.CaseStatus.CaseStatus
import models.PseudoCaseStatus.PseudoCaseStatus
import models.SortDirection.SortDirection
import play.api.mvc.QueryStringBindable

import scala.util.Try

object BinderUtil {
  val Top = ""

  def bindApplicationType(value: String): Option[ApplicationType] =
    ApplicationType.values.find(_.name == value)

  def bindCaseStatus(value: String): Option[CaseStatus] =
    CaseStatus.values.find(_.toString == value)

  def bindPseudoCaseStatus(value: String): Option[PseudoCaseStatus] =
    PseudoCaseStatus.values.find(_.toString == value)

  def bindSortDirection(value: String): Option[SortDirection] =
    SortDirection.values.find(_.toString == value)

  def bindInstant(value: String): Option[Instant] = Try(Instant.parse(value)).toOption

  def params(name: String)(implicit requestParams: Map[String, Seq[String]]): Option[Set[String]] =
    requestParams.get(name).map(_.flatMap(_.split(",")).toSet).filterNot(_.exists(_.isEmpty))

  def orderedParams(name: String)(implicit requestParams: Map[String, Seq[String]]): Option[Seq[String]] =
    requestParams.get(name).map(_.flatMap(_.split(","))).filterNot(_.exists(_.isEmpty))

  def param(name: String)(implicit requestParams: Map[String, Seq[String]]): Option[String] =
    params(name).map(_.head)

  def bindable[T](implicit binder: QueryStringBindable[T]) = binder

  def subKey(parentKey: String, childKey: String) =
    Seq(parentKey, childKey).filterNot(_.isEmpty).mkString("_")

  def bind[T](parentKey: String, childKey: String, params: Map[String, Seq[String]])(implicit binder: QueryStringBindable[T]) =
    EitherT(binder.bind(subKey(parentKey, childKey), params))

  def bind[T](default: => T)(parentKey: String, childKey: String, params: Map[String, Seq[String]])(implicit binder: QueryStringBindable[T]) =
    EitherT(binder.bind(subKey(parentKey, childKey), params).orElse(Some(Right(default))))

  def unbind[T](parentKey: String, childKey: String, value: T)(implicit binder: QueryStringBindable[T]) =
    binder.unbind(subKey(parentKey, childKey), value)
}
