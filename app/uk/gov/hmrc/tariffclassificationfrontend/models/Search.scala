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

package uk.gov.hmrc.tariffclassificationfrontend.models

import java.time.{Clock, Instant}

import play.api.mvc.QueryStringBindable

import scala.util.Try

case class Search
(
  traderName: Option[String] = None,
  commodityCode: Option[String] = None,
  includeInProgress: Option[Boolean] = None
) {
  def isEmpty: Boolean = {
    traderName.isEmpty && commodityCode.isEmpty && includeInProgress.isEmpty
  }

  def isDefined: Boolean = !isEmpty
}

object Search {

  implicit def frontEndBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[Search] = new QueryStringBindable[Search] {

    private val traderNameKey = "trader_name"
    private val commodityCodeKey = "commodity_code"
    private val includeInProgressKey = "include_in_progress"

    private def bindBoolean: String => Option[Boolean] = v => Try(v.toBoolean).toOption

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Search]] = {
      def param(name: String): Option[String] = stringBinder.bind(name, params).filter(_.isRight).map(_.right.get.trim).filter(_.nonEmpty)

      Some(Right(Search(
        traderName = param(traderNameKey),
        commodityCode = param(commodityCodeKey).map(_.replaceAll(" ", "")),
        includeInProgress = param(includeInProgressKey).flatMap(bindBoolean)
      )))
    }

    override def unbind(key: String, search: Search): String = {
      val bindings: Seq[Option[String]] = Seq(
        search.traderName.map(stringBinder.unbind(traderNameKey, _)),
        search.commodityCode.map(stringBinder.unbind(commodityCodeKey, _)),
        search.includeInProgress.map(v => stringBinder.unbind(includeInProgressKey, s"$v"))
      )
      bindings.filter(_.isDefined).map(_.get).mkString("&")
    }
  }

  def bindingTariffClassificationBindable(implicit clock: Clock, stringBinder: QueryStringBindable[String]): QueryStringBindable[Search] = new QueryStringBindable[Search] {

    private val traderNameKey = "trader_name"
    private val commodityCodeKey = "commodity_code"
    private val minDecisionEndKey = "min_decision_end"

    private def bindInstant(key: String): Option[Instant] = Try(Instant.parse(key)).toOption

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Search]] = {
      def param(name: String): Option[String] = stringBinder.bind(name, params).filter(_.isRight).map(_.right.get.trim).filter(_.nonEmpty)

      Some(Right(Search(
        traderName = param(traderNameKey),
        commodityCode = param(commodityCodeKey).map(_.replaceAll(" ", "")),
        includeInProgress = param(minDecisionEndKey).map(_ => true)
      )))
    }

    override def unbind(key: String, search: Search): String = {
      val bindings: Seq[Option[String]] = Seq(
        search.traderName.map(stringBinder.unbind(traderNameKey, _)),
        search.commodityCode.map(stringBinder.unbind(commodityCodeKey, _)),
        search.includeInProgress.filter(v => v).map(_ => stringBinder.unbind(minDecisionEndKey, Instant.now(clock).toString))
      )
      bindings.filter(_.isDefined).map(_.get).mkString("&")
    }
  }
}
