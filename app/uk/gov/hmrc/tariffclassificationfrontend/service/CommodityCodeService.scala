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

package uk.gov.hmrc.tariffclassificationfrontend.service

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.models.CommodityCode

import scala.io.Source

@Singleton
class CommodityCodeService @Inject()(appConfig: AppConfig) {

  private lazy val dateTimeFormatter = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss")
  private lazy val padLimit = 10

  def find(commodityCode: String): Option[CommodityCode] = {
    val canonicalCode: String = padTo10Digits(commodityCode)

    commodityCodesFromFile.find(_.code == canonicalCode)
  }

  private def padTo10Digits(input: String): String = {
    val trimmed = input.trim
    if (trimmed.length > padLimit) trimmed.substring(0, padLimit)
    else trimmed.padTo[Char, String](padLimit, '0').mkString
  }

  private lazy val commodityCodesFromFile: Seq[CommodityCode] = {
    val url = getClass.getClassLoader.getResource(appConfig.commodityCodePath)
    val lines = Source.fromURL(url, "UTF-8").getLines()

    val byHeader: Map[String, Int] = split(lines.next()).zipWithIndex.toMap

    lines
      .map(split)
      .filter(columns => columns(byHeader("leaf")) == "1")
      .map { columns =>
        val commodityCode = columns(byHeader("goods_nomenclature_item_id"))
        val expiry = columns(byHeader("validity_end_date"))
        if(expiry.nonEmpty) {
          val date = LocalDateTime.parse(expiry, dateTimeFormatter).atZone(appConfig.clock.getZone).toInstant
          CommodityCode(commodityCode, Some(date))
        } else {
          CommodityCode(commodityCode)
        }
      }.toSeq
  }

  private def split(string: String): Array[String] = {
    string.split(";").map(_.replaceAll("\"", ""))
  }

}
