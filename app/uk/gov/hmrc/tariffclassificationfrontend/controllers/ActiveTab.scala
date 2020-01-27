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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import play.api.mvc.QueryStringBindable

sealed case class ActiveTab(name: String)

object ActiveTab {

  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[ActiveTab] = new QueryStringBindable[ActiveTab] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, ActiveTab]] = {
      for {
        tN <- stringBinder.bind(key, params)
      } yield {
        tN match {
          case Right(tabName) if ActiveTab.allowed.contains(ActiveTab(tabName)) => Right(ActiveTab(tabName))
          case _ => Left("Unable to bind Active Tab to valid value")
        }
      }
    }

    override def unbind(key: String, value: ActiveTab): String = {
      stringBinder.unbind("activeTab", value.name)
    }
  }

  val allowed = Set(Applicant, Item, Sample, Attachments, Activity, Keywords, Ruling, Appeals, Liability)


  object Applicant extends ActiveTab("tab-item-Applicant")

  object Item extends ActiveTab("tab-item-Item")

  object Sample extends ActiveTab("tab-item-Sample")

  object Attachments extends ActiveTab("tab-item-Attachments")

  object Activity extends ActiveTab("tab-item-Activity")

  object Keywords extends ActiveTab("tab-item-Keywords")

  object Ruling extends ActiveTab("tab-item-Ruling")

  object Appeals extends ActiveTab("tab-item-Appeals")

  object Liability extends ActiveTab("tab-item-Liability")



}
