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

package views

import play.api.mvc.Call
import controllers.{ActiveTab, routes}

object CancelAction {

  def cancelHref(activeTab: Option[ActiveTab], reference: String): Call =
    activeTab match {
      case Some(ActiveTab.Item)        => routes.CaseController.itemDetails(reference)
      case Some(ActiveTab.Sample)      => routes.CaseController.sampleDetails(reference)
      case Some(ActiveTab.Attachments) => routes.AttachmentsController.attachmentsDetails(reference)
      case Some(ActiveTab.Activity)    => routes.CaseController.activityDetails(reference)
      case Some(ActiveTab.Keywords)    => routes.CaseController.keywordsDetails(reference)
      case Some(ActiveTab.Ruling)      => routes.CaseController.rulingDetails(reference)
      case Some(ActiveTab.Appeals)     => routes.AppealCaseController.appealDetails(reference)
      case Some(ActiveTab.Liability)   => routes.LiabilityController.liabilityDetails(reference)
      case _                           => routes.CaseController.get(reference)
    }

}
