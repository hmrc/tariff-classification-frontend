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

import org.apache.pekko.stream.scaladsl.Source
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class Paged[T](results: Seq[T], pageIndex: Int, pageSize: Int, resultCount: Int) {
  def map[X](f: T => X): Paged[X]                  = this.copy(results = results.map(f))
  def filter(predicate: T => Boolean): Paged[T]    = this.copy(results = results.filter(predicate))
  def filterNot(predicate: T => Boolean): Paged[T] = this.copy(results = results.filterNot(predicate))
  def size: Int                                    = results.size
  def pageCount: Int                               = Math.ceil(resultCount.toDouble / pageSize).toInt
  def isEmpty: Boolean                             = results.isEmpty
  def nonEmpty: Boolean                            = results.nonEmpty
}

object Paged {
  def stream[T](
    initialPagination: Pagination
  )(fetchPage: Pagination => Future[Paged[T]])(using ec: ExecutionContext): Source[T, _] =
    Source
      .unfoldAsync(initialPagination) { pagination =>
        fetchPage(pagination).map { page =>
          if (page.isEmpty) None
          else Some((pagination.withPage(pagination.page + 1), page))
        }
      }
      .flatMapConcat(page => Source(page.results.toList))

  def empty[T]: Paged[T] = Paged[T](Seq.empty, NoPagination(), 0)

  def apply[T](results: Seq[T], pagination: Pagination, resultCount: Int): Paged[T] =
    Paged(results, pagination.page, pagination.pageSize, resultCount)

  def apply[T](results: Seq[T]): Paged[T] = Paged(results, NoPagination(), results.size)

  implicit def format[T](using fmt: Format[T]): Format[Paged[T]] =
    Format[Paged[T]](Reads[Paged[T]](reads), Writes[Paged[T]](writes))

  private def reads[T](using fmt: Reads[T]): JsValue => JsResult[Paged[T]] =
    js =>
      Try(
        new Paged[T](
          js \ "results" match {
            case JsDefined(JsArray(r)) => r.toSeq.map(jsResult => jsResult.as[T])
            case _                     => throw new IllegalArgumentException("invalid results")
          },
          (js \ "pageIndex").as[Int],
          (js \ "pageSize").as[Int],
          (js \ "resultCount").as[Int]
        )
      ).map(JsSuccess(_))
        .recover { case t: Throwable =>
          JsError(t.getMessage)
        }
        .get

  private def writes[T](using fmt: Writes[T]): Paged[T] => JsValue =
    (paged: Paged[T]) =>
      Json.obj(
        "results"     -> JsArray(paged.results.map(fmt.writes)),
        "pageIndex"   -> JsNumber(paged.pageIndex),
        "pageSize"    -> JsNumber(paged.pageSize),
        "resultCount" -> JsNumber(paged.resultCount)
      )

}
