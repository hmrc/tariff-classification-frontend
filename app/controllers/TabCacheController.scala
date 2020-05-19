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

import connector.DataCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TabCacheController @Inject()(
                                    dataCacheConnector: DataCacheConnector,
                                    identify: IdentifierAction,
                                    getData: DataRetrievalAction,
                                    verify: RequestActions,
                                    mcc: MessagesControllerComponents,
                                  ) extends FrontendController(mcc) {

  def post(reference: String, itemType: String): Action[AnyContent] =
    (identify andThen getData).async {
      implicit request =>
        val anchor = request.body.asText.getOrElse("")
        if (anchor.trim.nonEmpty) {
          val key = reference + itemType.toLowerCase
          val map: Map[String, JsValue] = Map(key -> Json.toJson(anchor))
          val cacheMap = new CacheMap(request.internalId, map)
          dataCacheConnector.save(cacheMap)
        }

        Future.successful(Ok("Ok"))
    }

  def get(reference: String, itemType: String): Action[AnyContent] =
    (verify.authenticated andThen verify.casePermissions(reference) andThen identify andThen getData).async {
      implicit request => {

        lazy val defaultTab: String = if (itemType.toLowerCase.equals("liability")) "#c592_tab" else ""

        dataCacheConnector.fetch(request.internalId).map {
          case Some(value) => Ok(value.getEntry[String](reference + itemType.toLowerCase).getOrElse(defaultTab))
          case _ => Ok(defaultTab)
        }
      }
    }

}