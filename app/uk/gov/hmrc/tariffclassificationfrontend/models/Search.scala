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

import play.api.data.Form
import play.api.mvc.QueryStringBindable
import uk.gov.hmrc.tariffclassificationfrontend.forms.SearchForm
import uk.gov.hmrc.tariffclassificationfrontend.models.PseudoCaseStatus.PseudoCaseStatus

case class Search
(
  traderName: Option[String] = None,
  commodityCode: Option[String] = None,
  decisionDetails: Option[String] = None,
  status: Option[Set[PseudoCaseStatus]] = None,
  keywords: Option[Set[String]] = None
) {

  def isEmpty: Boolean = {
    // Live rulings only omitted intentionally as it is a post-search filter
    traderName.isEmpty && commodityCode.isEmpty && decisionDetails.isEmpty && keywords.isEmpty
  }

  def isDefined: Boolean = !isEmpty
}

object Search {

  implicit def binder(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[Search] = new QueryStringBindable[Search] {

    override def bind(string: String, requestParams: Map[String, Seq[String]]): Option[Either[String, Search]] = {
      val filteredParams: Map[String, Seq[String]] = requestParams
        .mapValues(_.map(_.trim).filter(_.nonEmpty))
        .filter(_._2.nonEmpty)

      val form: Form[Search] = SearchForm.formWithoutValidation.bindFromRequest(filteredParams)

      if (form.hasErrors) Some(Right(Search()))
      else Some(Right(form.get))
    }

    override def unbind(string: String, search: Search): String = {
      val data: Map[String, String] = SearchForm.formWithoutValidation.fill(search).data
      data.toSeq.map(f => stringBinder.unbind(f._1, f._2)).mkString("&")
    }

  }

}
