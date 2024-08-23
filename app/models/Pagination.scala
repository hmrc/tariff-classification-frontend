/*
 * Copyright 2024 HM Revenue & Customs
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

import cats.syntax.all._
import play.api.mvc.QueryStringBindable

trait Pagination {
  val page: Int
  val pageSize: Int

  def withPage(page: Int): Pagination
}

object Pagination {

  val unlimited: Int = Integer.MAX_VALUE

  implicit val paginationQueryStringBindable: QueryStringBindable[Pagination] = new QueryStringBindable[Pagination] {
    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Pagination]] =
      (
        BinderUtil.bind[Int](1)(key, "page", params),
        BinderUtil.bind[Int](50)(key, "page_size", params)
      ).mapN(SearchPagination.apply).value
    def unbind(key: String, value: Pagination): String =
      Seq(
        BinderUtil.unbind[Int](key, "page", value.page),
        BinderUtil.unbind[Int](key, "page_size", value.pageSize)
      ).filterNot(_.isEmpty).mkString("&")
  }
}

case class SearchPagination(
  override val page: Int = 1,
  override val pageSize: Int = 50
) extends Pagination {
  override def withPage(page: Int): Pagination = copy(page = page)
}

case class NoPagination(
  override val page: Int = 1,
  override val pageSize: Int = Pagination.unlimited
) extends Pagination {
  override def withPage(page: Int): Pagination = copy(page = page)
}
