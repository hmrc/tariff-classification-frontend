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

package service

import cats.data.OptionT
import controllers.Tab
import models.ApplicationType
import models.cache.CacheMap
import play.api.libs.json.JsString

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TabCacheService @Inject() (
  dataCacheConnector: DataCacheService
)(implicit ec: ExecutionContext) {

  private def cacheKey(reference: String, appType: ApplicationType) =
    s"$reference-${appType.toString}"

  def getActiveTab(requestId: String, reference: String, appType: ApplicationType): Future[Option[Tab]] = {
    val activeTab = for {
      cacheMap  <- OptionT(dataCacheConnector.fetch(requestId))
      _         <- OptionT.liftF[Future, Unit](clearActiveTab(requestId, reference, appType, cacheMap))
      entry     <- OptionT.fromOption[Future](cacheMap.getEntry[String](cacheKey(reference, appType)))
      activeTab <- OptionT.fromOption[Future](Tab.fromValue(entry))
    } yield activeTab

    activeTab.value
  }

  def clearActiveTab(requestId: String, reference: String, appType: ApplicationType): Future[Unit] =
    dataCacheConnector.fetch(requestId).flatMap {
      case Some(cacheMap) =>
        clearActiveTab(requestId, reference, appType, cacheMap)
      case None =>
        Future.successful(())
    }

  def clearActiveTab(
    requestId: String,
    reference: String,
    appType: ApplicationType,
    cacheMap: CacheMap
  ): Future[Unit] = {
    val cachedData      = cacheMap.data
    val updatedCacheMap = CacheMap(requestId, cachedData - cacheKey(reference, appType))
    dataCacheConnector.save(updatedCacheMap).map(_ => ())
  }

  def setActiveTab(requestId: String, reference: String, appType: ApplicationType, activeTab: Tab): Future[Unit] =
    dataCacheConnector.fetch(requestId).flatMap { maybeCacheMap =>
      val cachedData      = maybeCacheMap.map(_.data).getOrElse(Map.empty)
      val tabMapping      = cacheKey(reference, appType) -> JsString(activeTab.name)
      val updatedCacheMap = CacheMap(requestId, cachedData + tabMapping)
      dataCacheConnector.save(updatedCacheMap).map(_ => ())
    }
}
