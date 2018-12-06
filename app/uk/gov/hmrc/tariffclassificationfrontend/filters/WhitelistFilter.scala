/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.filters

import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.http.HttpVerbs.GET
import play.api.mvc.Results.{Forbidden, Redirect}
import play.api.mvc.{Call, RequestHeader, Result}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.whitelist.AkamaiWhitelistFilter

import scala.concurrent.Future

@Singleton
class WhitelistFilter @Inject()(val appConfig: AppConfig, implicit val mat: Materializer) extends AkamaiWhitelistFilter {

  override lazy val whitelist: Seq[String] = appConfig.whitelistedIps

  override lazy val destination: Call = Call(GET, appConfig.whitelistDestination)

  override lazy val excludedPaths: Seq[Call] = {
    Logger.info("=> Stefano")
    appConfig.whitelistedExcludedPaths.map(Call(GET, _))
  }










//
//  override def apply
//  (f: (RequestHeader) => Future[Result])
//  (rh: RequestHeader): Future[Result] =
//    if (excludedPaths contains toCall(rh)) {
//      f(rh)
//    } else {
//      rh.headers.get(trueClient) map {
//        ip =>
//          if (whitelist.contains(ip))
//            f(rh)
//          else if (isCircularDestination(rh))
//            Future.successful(Forbidden)
//          else
//            Future.successful(Redirect(destination))
//      } getOrElse noHeaderAction(f, rh)
//    }

  // TO DELETE
  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    Logger.info(s"=> \n\nWhitelistFilter.apply() - ${toCall(rh)} \n\n")
    Logger.info(s"=> \n\nWhitelistFilter.apply() - ${rh.headers} \n\n")

    // it does not work because you need to have the "True-Client-IP" header in the request

    super.apply(f)(rh)
  }

  private def toCall(rh: RequestHeader): Call = {
    Call(rh.method, rh.uri)
  }

}
