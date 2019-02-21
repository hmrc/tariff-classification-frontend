/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.service

import javax.inject.Singleton

import scala.concurrent.Future
import scala.io.Source

@Singleton
class CommodityCodeService () {

  val padLimit = 10

  def checkIfCodeExists(commodityCode: String): Future[Boolean] = {
    val canonicalCode = commodityCode match {
      case code if code.length > padLimit => code.substring(0, padLimit)
      case _ => commodityCode.trim.padTo(padLimit, "0").mkString
    }

    Future.successful(commodityCodesFromFile.contains(canonicalCode))
  }

  lazy val commodityCodesFromFile: Seq[String] = {
      val url = getClass.getClassLoader.getResource("commodityCodes.txt")
      (for (line <- Source.fromURL(url, "UTF-8").getLines()) yield line).toSeq
  }


}
