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

package controllers

import connector.{DataCacheConnector, MongoCacheConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future.successful

@Singleton
class TabCacheController @Inject()(
                                    dataCacheConnector: DataCacheConnector,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    requireData: DataRequiredAction,
                                    mcc: MessagesControllerComponents,
                                  ) extends FrontendController(mcc) {

  def post(reference: String, anchor: String): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val map: Map[String, JsValue] = Map(reference -> Json.toJson(anchor))
      val cacheMap = new CacheMap(request.internalId, map)
      dataCacheConnector.save(cacheMap)
      Ok("OK")
  }

  def get(reference: String): Action[AnyContent] = Action.async {
    successful(Ok("OK"))
  }

}