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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import java.time.LocalDate

import javax.inject.{Inject, Singleton}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.Case
import uk.gov.hmrc.tariffclassificationfrontend.views

import scala.concurrent.Future

@Singleton
class CasesController @Inject()(val messagesApi: MessagesApi, implicit val appConfig: AppConfig) extends FrontendController with I18nSupport {

  val case1 = Case("1", "Laptops", "Pol's PCs", LocalDate.of(2018,10,1), "OPEN", "BTI", 1)
  val case2 = Case("2", "Pizza", "Ed's Eatery", LocalDate.of(2018,10,5), "DRAFT", "BTI", 10)
  val case3 = Case("3", "Pasta", "Stefano's Supermarket", LocalDate.of(2018,10,15), "OPEN", "BTI", 5)

  val gateway = Action.async {
    implicit request => Future.successful(Ok(views.html.gateway_cases(Seq(case1, case2, case3))))
  }

}
