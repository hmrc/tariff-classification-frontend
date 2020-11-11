/*
 * Copyright 2020 HM Revenue & Customs
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

package service

import controllers.Tab
import connector.DataCacheConnector
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import models.ApplicationType
import uk.gov.hmrc.http.cache.client.CacheMap
import play.api.libs.json.JsString

@Singleton
class TabCacheService @Inject() (
  dataCacheConnector: DataCacheConnector
)(implicit ec: ExecutionContext) {

  def cacheKey(requestId: String, appType: ApplicationType.Value) =
    s"$requestId-${appType.toString}"

  def getActiveTab(requestId: String, appType: ApplicationType.Value): Future[Option[Tab]] =
    dataCacheConnector.getEntry[String](requestId, cacheKey(requestId, appType)).map { maybeValue =>
      maybeValue.flatMap(Tab.fromValue)
    }

  def clearActiveTab(requestId: String, appType: ApplicationType.Value): Future[Unit] =
    dataCacheConnector.fetch(requestId).flatMap {
      case Some(cacheMap) =>
        val cachedData      = cacheMap.data
        val updatedCacheMap = CacheMap(requestId, cachedData - cacheKey(requestId, appType))
        dataCacheConnector.save(updatedCacheMap).map(_ => ())
      case None =>
        Future.successful(())
    }

  def setActiveTab(requestId: String, appType: ApplicationType.Value, activeTab: Tab): Future[Unit] =
    dataCacheConnector.fetch(requestId).flatMap { maybeCacheMap =>
      val cachedData      = maybeCacheMap.map(_.data).getOrElse(Map.empty)
      val tabMapping      = cacheKey(requestId, appType) -> JsString(activeTab.name)
      val updatedCacheMap = CacheMap(requestId, cachedData + tabMapping)
      dataCacheConnector.save(updatedCacheMap).map(_ => ())
    }
}
