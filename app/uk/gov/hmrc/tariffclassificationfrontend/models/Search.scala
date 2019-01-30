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

case class Search
(
  traderName: Option[String] = None
) {
  def isEmpty: Boolean = {
    traderName.isEmpty
  }
}

object Search {
  private val traderNameKey = "traderName"

  implicit def bindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[Search] = new QueryStringBindable[Search] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Search]] = {
      def param(name: String): Option[String] = stringBinder.bind(name, params).filter(_.isRight).map(_.right.get)
      Some(Right(Search(
        traderName = param(traderNameKey)
      )))
    }

    override def unbind(key: String, search: Search): String = {
      val bindings: Seq[Option[String]] = Seq(
        search.traderName.map(v => stringBinder.unbind(traderNameKey, v))
      )
      bindings.filter(_.isDefined).map(_.get).mkString("&")
    }
  }
}
