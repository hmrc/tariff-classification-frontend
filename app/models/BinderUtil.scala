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

package models

import cats.data.EitherT
import models.LiabilityStatus.LiabilityStatus
import models.PseudoCaseStatus.PseudoCaseStatus
import models.SortDirection.SortDirection
import play.api.mvc.QueryStringBindable

import java.time.Instant
import scala.util.Try

object BinderUtil {

  def bindApplicationType(value: String): Option[ApplicationType] =
    ApplicationType.values.find(_.name == value)

  def bindPseudoCaseStatus(value: String): Option[PseudoCaseStatus] =
    PseudoCaseStatus.values.find(_.toString == value)

  def bindLiabilityStatus(value: String): Option[LiabilityStatus] =
    LiabilityStatus.values.find(_.toString.equalsIgnoreCase(value))

  def bindSortDirection(value: String): Option[SortDirection] =
    SortDirection.values.find(_.toString == value)

  def bindInstant(value: String): Option[Instant] = Try(Instant.parse(value)).toOption

  def params(name: String)(implicit requestParams: Map[String, Seq[String]]): Option[Set[String]] =
    requestParams.get(name).map(_.flatMap(_.split(",")).toSet).filterNot(_.exists(_.isEmpty))

  def orderedParams(name: String)(implicit requestParams: Map[String, Seq[String]]): Option[List[String]] =
    requestParams.get(name).map(_.flatMap(_.split(",")).toList).filterNot(_.exists(_.isEmpty))

  def param(name: String)(implicit requestParams: Map[String, Seq[String]]): Option[String] =
    params(name).map(_.head)

  def bindable[T](implicit binder: QueryStringBindable[T]): QueryStringBindable[T] = binder

  private def subKey(parentKey: String, childKey: String) =
    Seq(parentKey, childKey).filterNot(_.isEmpty).mkString("_")

  def bind[T](parentKey: String, childKey: String, params: Map[String, Seq[String]])(implicit
    binder: QueryStringBindable[T]
  ): EitherT[Option, String, T] =
    EitherT(binder.bind(subKey(parentKey, childKey), params))

  def bind[T](
    default: => T
  )(parentKey: String, childKey: String, params: Map[String, Seq[String]])(implicit
    binder: QueryStringBindable[T]
  ): EitherT[Option, String, T] =
    EitherT(binder.bind(subKey(parentKey, childKey), params).orElse(Some(Right(default))))

  def unbind[T](parentKey: String, childKey: String, value: T)(implicit binder: QueryStringBindable[T]): String =
    binder.unbind(subKey(parentKey, childKey), value)
}
