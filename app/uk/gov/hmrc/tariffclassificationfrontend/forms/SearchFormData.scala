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

package uk.gov.hmrc.tariffclassificationfrontend.forms

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.tariffclassificationfrontend.models.Search

case class SearchFormData(traderName: String = "")

object SearchForm {

  val form = Form(
    mapping(
      "trader_name" -> nonEmptyText
    )(SearchFormData.apply)(SearchFormData.unapply)
  )

  def fill(search: Search): Form[SearchFormData] = {
    SearchForm.form.fill(
      SearchFormData(
        search.traderName.getOrElse("")
      )
    )
  }

}
