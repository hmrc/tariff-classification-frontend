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

package controllers.v2

import controllers.{ControllerBaseSpec, RequestActionsWithPermissions}
import models.forms.{CommodityCodeConstraints, DecisionForm}
import models.{Case, Event, Permission}
import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import service._
import utils.Cases.{aCase, withLiabilityApplication, withReference}
import views.html.v2.atar_view

class AtarControllerSpec extends ControllerBaseSpec with BeforeAndAfterEach {
  private val queueService             = mock[QueuesService]
  private val eventService             = mock[EventsService]
  private val fileService              = mock[FileStoreService]
  private val event                    = mock[Event]
  private val commodityCodeConstraints = mock[CommodityCodeConstraints]
  private val decisionForm             = new DecisionForm(commodityCodeConstraints)
  private val keywordsService          = mock[KeywordsService]
  private val countriesService         = mock[CountriesService]
  private val atarView                 = mock[atar_view]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(queueService)
    reset(fileService)
    reset(event)
    reset(atarView)
  }

  private def controller(c: Case, permission: Set[Permission]) = new AtarController(
    new RequestActionsWithPermissions(playBodyParsers, permission, c = c),
    eventService,
    queueService,
    fileService,
    keywordsService,
    countriesService,
    decisionForm,
    mcc,
    redirectService,
    atarView,
    realAppConfig
  )

  "Atar Controller" should {

    "redirect to correct controller when case is different application type" in {
      val c = aCase(withReference("reference"), withLiabilityApplication())

      val result = await(controller(c, Set(Permission.EDIT_CORRESPONDENCE)))
        .displayAtar("reference")(newFakeGETRequestWithCSRF())
      status(result) shouldBe Status.SEE_OTHER
      locationOf(result) shouldBe Some(
        controllers.v2.routes.LiabilityController.displayLiability("reference").path
      )
    }
  }

}
