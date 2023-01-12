/*
 * Copyright 2023 HM Revenue & Customs
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

package repositories

import java.time.{LocalDateTime, ZoneOffset}
import java.util.concurrent.TimeUnit

import javax.inject.{Inject, Singleton}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.{IndexModel, IndexOptions, ReplaceOptions}
import org.mongodb.scala.model.Sorts.ascending
import play.api.libs.json.{Format, JsValue, Json, OFormat}
import play.api.Configuration
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import scala.concurrent.{ExecutionContext, Future}

case class DatedCacheMap(
  id: String,
  data: Map[String, JsValue],
  lastUpdated: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
)

object DatedCacheMap {
  implicit val dateFormat: Format[LocalDateTime] = MongoJavatimeFormats.localDateTimeFormat
  implicit val formats: OFormat[DatedCacheMap]   = Json.format[DatedCacheMap]

  def apply(cacheMap: CacheMap): DatedCacheMap = DatedCacheMap(cacheMap.id, cacheMap.data)
}

@Singleton
class SessionRepository @Inject() (config: Configuration, mongoComponent: MongoComponent)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[DatedCacheMap](
      mongoComponent = mongoComponent,
      collectionName = "api-cache",
      domainFormat   = DatedCacheMap.formats,
      indexes = Seq(
        IndexModel(
          keys = ascending("lastUpdated"),
          indexOptions = IndexOptions()
            .name("userAnswersExpiry")
            .expireAfter(config.get[Int]("mongodb.timeToLiveInSeconds"), TimeUnit.SECONDS)
        )
      )
    ) {

  def upsert(cm: CacheMap): Future[Boolean] =
    collection
      .replaceOne(
        filter      = byId(cm.id),
        replacement = DatedCacheMap(cm),
        options     = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_.wasAcknowledged())

  def get(id: String): Future[Option[CacheMap]] =
    collection.find(byId(id)).map(x => CacheMap(x.id, x.data)).headOption()

  def remove(cm: CacheMap): Future[Boolean] =
    collection.deleteOne(byId(cm.id)).toFuture().map(_.wasAcknowledged())

  private def byId(value: String): Bson =
    equal("id", value)
}
