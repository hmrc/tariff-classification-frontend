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

import play.api.mvc.QueryStringBindable

import scala.util.Try

case class Search
(
  traderName: Option[String] = None,
  commodityCode: Option[String] = None,
  liveRulingsOnly: Option[Boolean] = None,
  keywords: Option[Set[String]] = None
) {
  def isEmpty: Boolean = {
    traderName.isEmpty && commodityCode.isEmpty && liveRulingsOnly.isEmpty && keywords.isEmpty
  }

  def isDefined: Boolean = !isEmpty
}

object Search {

  implicit def binder(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[Search] = new QueryStringBindable[Search] {

    private val traderNameKey = "trader_name"
    private val commodityCodeKey = "commodity_code"
    private val liveRulingsOnlyKey = "live_rulings_only"
    private val keywordsKey = "keyword"

    private def bindBoolean: String => Option[Boolean] = v => Try(v.toBoolean).toOption

    override def bind(key: String, requestParams: Map[String, Seq[String]]): Option[Either[String, Search]] = {
      def params(name: String): Option[Set[String]] = requestParams
        .get(name)
        .map(
          _.map(_.trim())
            .toSet
            .filter(_.nonEmpty))
        .filter(_.nonEmpty)

      def param(name: String): Option[String] = params(name).map(_.head)


      Some(Right(Search(
        traderName = param(traderNameKey),
        commodityCode = param(commodityCodeKey).map(_.replaceAll(" ", "")),
        liveRulingsOnly = param(liveRulingsOnlyKey).flatMap(bindBoolean),
        keywords = params(keywordsKey)
      )))
    }

    override def unbind(key: String, search: Search): String = {
      val bindings: Seq[Option[String]] = Seq(
        search.traderName.map(stringBinder.unbind(traderNameKey, _)),
        search.commodityCode.map(stringBinder.unbind(commodityCodeKey, _)),
        search.liveRulingsOnly.map(v => stringBinder.unbind(liveRulingsOnlyKey, s"$v")),
        search.keywords.map(_.map(s => stringBinder.unbind(keywordsKey, s)).mkString("&"))
      )
      bindings.filter(_.isDefined).map(_.get).mkString("&")
    }
  }
}
