/*
 * Copyright 2026 HM Revenue & Customs
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

package filters

import org.apache.pekko.stream.Materializer
import com.google.inject.{Inject, Singleton}
import play.api.i18n.MessagesApi
import play.api.mvc.Results.ServiceUnavailable
import play.api.mvc._
import _root_.config.AppConfig
import views.html.shutterPage

import scala.concurrent.Future

@Singleton
class ShutteringFilter @Inject() (
  appConfig: AppConfig,
  messagesApi: MessagesApi,
  shutterPage: shutterPage
)(implicit val mat: Materializer)
    extends Filter {

  private val shuttered: Boolean = appConfig.shutterFlag

  private val excludedPaths: Seq[Call] = appConfig.shutterExcludedUrls
    .split(",")
    .toIndexedSeq
    .map(path => Call("GET", path.trim))

  private lazy val assetPathPrefixes: Seq[String] = Seq(
    controllers.routes.Assets.at("").url,
    uk.gov.hmrc.hmrcfrontend.controllers.routes.Assets.at("").url
  )

  private def toCall(rh: RequestHeader): Call =
    Call(rh.method, rh.uri)

  private def isAsset(rh: RequestHeader): Boolean =
    rh.method == "GET" && assetPathPrefixes.exists(rh.path.startsWith)

  override def apply(next: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] =
    if (shuttered && !excludedPaths.contains(toCall(rh)) && !isAsset(rh))
      Future.successful(ServiceUnavailable(shutterPage()(rh, messagesApi.preferred(rh))))
    else next(rh)
}
