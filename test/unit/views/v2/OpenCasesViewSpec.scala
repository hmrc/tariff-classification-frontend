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

package views.v2

import models.{ApplicationType, Paged, Queues}
import models.viewmodels.CasesTabViewModel
import utils.Cases
import views.ViewMatchers.containElementWithID
import views.ViewSpec
import views.html.v2.open_cases_view

class OpenCasesViewSpec extends ViewSpec {

  def commonCasesView: open_cases_view = injector.instanceOf[open_cases_view]

  val iwantaseqofcase = Paged(Seq(Cases.btiCaseExample, Cases.btiCaseExample))

  "OpenCasesViewSpec" should {

    "render successfully with the default tab" in {
      val doc = view(
        commonCasesView(
          CasesTabViewModel.forApplicationType(
            ApplicationType.ATAR,
            Queues.allAtarQueues,
            Seq(Cases.btiCaseExample, Cases.btiCaseExample)
          )
        )
      )

      doc should containElementWithID("open-cases-tabs")
    }
  }

  "contains appropriate queue tabs for ATaR" in {
    val doc = view(
      commonCasesView(
        CasesTabViewModel.forApplicationType(
          ApplicationType.ATAR,
          Queues.allAtarQueues,
          Seq(Cases.btiCaseExample, Cases.btiCaseExample)
        )
      )
    )

    doc should containElementWithID("act_tab")
    doc should containElementWithID("car_tab")
    doc should containElementWithID("elm_tab")
    doc should containElementWithID("flex_tab")
    doc should containElementWithID("tta_tab")
    doc should containElementWithID("ttb_tab")
    doc should containElementWithID("ttc_tab")
  }

  "contain a heading for ATaR" in {
    val doc = view(
      commonCasesView(
        CasesTabViewModel.forApplicationType(
          ApplicationType.ATAR,
          Queues.allAtarQueues,
          Seq(Cases.btiCaseExample, Cases.btiCaseExample)
        )
      )
    )

    doc should containElementWithID("common-cases-heading")
  }

  "contain open-cases-sub-nav for ATaR" in {
    val doc = view(
      commonCasesView(
        CasesTabViewModel.forApplicationType(
          ApplicationType.ATAR,
          Queues.allAtarQueues,
          Seq(Cases.btiCaseExample, Cases.btiCaseExample)
        )
      )
    )

    doc should containElementWithID("open-cases-sub-nav")
  }

  "contains appropriate queue tabs for Liability" in {
    val doc = view(
      commonCasesView(
        CasesTabViewModel.forApplicationType(
          ApplicationType.LIABILITY,
          Queues.allLiabilityQueues,
          Seq(Cases.liabilityCaseExample, Cases.liabilityCaseExample)
        )
      )
    )

    doc should containElementWithID("act_tab")
    doc should containElementWithID("cap_tab")
    doc should containElementWithID("elm_tab")
    doc should containElementWithID("flex_tab")
    doc should containElementWithID("tta_tab")
    doc should containElementWithID("ttb_tab")
    doc should containElementWithID("ttc_tab")
  }

  "contain a heading for Liability" in {
    val doc = view(
      commonCasesView(
        CasesTabViewModel.forApplicationType(
          ApplicationType.LIABILITY,
          Queues.allLiabilityQueues,
          Seq(Cases.liabilityCaseExample, Cases.liabilityCaseExample)
        )
      )
    )

    doc should containElementWithID("common-cases-heading")
  }

  "contain open-cases-sub-nav in Liability tab" in {
    val doc = view(
      commonCasesView(
        CasesTabViewModel.forApplicationType(
          ApplicationType.LIABILITY,
          Queues.allLiabilityQueues,
          Seq(Cases.liabilityCaseExample, Cases.liabilityCaseExample)
        )
      )
    )

    doc should containElementWithID("open-cases-sub-nav")
  }

  "contains appropriate queue tabs for Correspondence" in {
    val doc = view(
      commonCasesView(
        CasesTabViewModel.forApplicationType(
          ApplicationType.CORRESPONDENCE,
          Queues.allCorresMiscQueues,
          Seq(Cases.corrCaseExample, Cases.corrCaseExample)
        )
      )
    )

    doc should containElementWithID("act_tab")
    doc should containElementWithID("elm_tab")
    doc should containElementWithID("flex_tab")
    doc should containElementWithID("tta_tab")
    doc should containElementWithID("ttb_tab")
    doc should containElementWithID("ttc_tab")
  }

  "contain a heading for Correspondence" in {
    val doc = view(
      commonCasesView(
        CasesTabViewModel.forApplicationType(
          ApplicationType.CORRESPONDENCE,
          Queues.allCorresMiscQueues,
          Seq(Cases.corrCaseExample, Cases.corrCaseExample)
        )
      )
    )

    doc should containElementWithID("common-cases-heading")
  }

  "contain open-cases-sub-nav in Correspondence tab" in {
    val doc = view(
      commonCasesView(
        CasesTabViewModel.forApplicationType(
          ApplicationType.CORRESPONDENCE,
          Queues.allCorresMiscQueues,
          Seq(Cases.corrCaseExample, Cases.corrCaseExample)
        )
      )
    )

    doc should containElementWithID("open-cases-sub-nav")
  }

  "contains appropriate queue tabs for Miscellaneous" in {
    val doc = view(
      commonCasesView(
        CasesTabViewModel.forApplicationType(ApplicationType.MISCELLANEOUS, Queues.allCorresMiscQueues, Seq.empty)
      )
    )

    doc should containElementWithID("act_tab")
    doc should containElementWithID("elm_tab")
    doc should containElementWithID("flex_tab")
    doc should containElementWithID("tta_tab")
    doc should containElementWithID("ttb_tab")
    doc should containElementWithID("ttc_tab")
  }

  "contain a heading for Miscellaneous" in {
    val doc = view(
      commonCasesView(
        CasesTabViewModel.forApplicationType(ApplicationType.MISCELLANEOUS, Queues.allCorresMiscQueues, Seq.empty)
      )
    )

    doc should containElementWithID("common-cases-heading")
  }

  "contain open-cases-sub-nav in Miscellaneous tab" in {
    val doc = view(
      commonCasesView(
        CasesTabViewModel.forApplicationType(ApplicationType.MISCELLANEOUS, Queues.allCorresMiscQueues, Seq.empty)
      )
    )

    doc should containElementWithID("open-cases-sub-nav")
  }

}
