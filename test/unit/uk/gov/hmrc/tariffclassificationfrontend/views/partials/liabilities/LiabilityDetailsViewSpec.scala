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

package uk.gov.hmrc.tariffclassificationfrontend.views.partials.liabilities

import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import play.api.data.validation.{Constraint, Valid}
import uk.gov.hmrc.tariffclassificationfrontend.forms.{CommodityCodeConstraints, DecisionForm, LiabilityDetailsForm}
import uk.gov.hmrc.tariffclassificationfrontend.models.CaseStatus.CaseStatus
import uk.gov.hmrc.tariffclassificationfrontend.models.{CaseStatus, _}
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewMatchers._
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views
import uk.gov.tariffclassificationfrontend.utils.Cases._

class LiabilityDetailsViewSpec extends ViewSpec with MockitoSugar {

  private val constraints = mock[CommodityCodeConstraints]
  private val form = new DecisionForm(constraints)

  private val caseIsCompletedStatuses: Seq[CaseStatus] = Seq(CaseStatus.COMPLETED, CaseStatus.CANCELLED)

  "Liability Details" should {
    given(constraints.commodityCodeExistsInUKTradeTariff) willReturn Constraint[String]("code")(_ => Valid)

    "Not render edit details button" when {
      for (status: CaseStatus <- CaseStatus.values.filterNot(_ == CaseStatus.NEW)) {
        s"case is status $status" in {
          // Given
          val c = aCase(
            withStatus(status),
            withLiabilityApplication()
          )
          val d = c.decision.getOrElse(Decision())
          val l = c.application.asLiabilityOrder

          // When
          val doc = view(
            views.html.partials.liabilities.liability_details(c = c, liabilityForm = LiabilityDetailsForm.liabilityDetailsForm(l), decisionForm = form.liabilityCompleteForm(d)
            )(requestWithPermissions(), messages, appConfig))

          // Then
          doc shouldNot containElementWithID("edit-liability-details")
        }
      }
    }

    "Render edit details button" when {

      "operator has permission" in {
        // Given
        val c = aCase(
          withStatus(CaseStatus.OPEN),
          withLiabilityApplication()
        )
        val d = c.decision.getOrElse(Decision())
        val l = c.application.asLiabilityOrder

        // When
        val doc = view(
          views.html.partials.liabilities.liability_details(c = c, liabilityForm = LiabilityDetailsForm.liabilityDetailsForm(l), decisionForm = form.liabilityCompleteForm(d)
          )(requestWithPermissions(Permission.EDIT_LIABILITY), messages, appConfig))

        // Then
        doc should containElementWithID("edit-liability-details")
      }
    }

    "Not render edit decision button" when {
      "Not permitted" in {
        // Given
        val c = aCase(
          withStatus(CaseStatus.OPEN),
          withLiabilityApplication()
        )
        val d = c.decision.getOrElse(Decision())
        val l = c.application.asLiabilityOrder

        // When
        val doc = view(
          views.html.partials.liabilities.liability_details(c = c, liabilityForm = LiabilityDetailsForm.liabilityDetailsForm(l), decisionForm = form.liabilityCompleteForm(d)
          )(requestWithPermissions(), messages, appConfig))

        // Then
        doc shouldNot containElementWithID("liability-decision-edit")
      }
    }

    "Render edit decision button if permitted" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication()
      )
      val d = c.decision.getOrElse(Decision())
      val l = c.application.asLiabilityOrder

      // When
      val doc = view(
        views.html.partials.liabilities.liability_details(c = c, liabilityForm = LiabilityDetailsForm.liabilityDetailsForm(l), decisionForm = form.liabilityCompleteForm(d)
        )(requestWithPermissions(Permission.EDIT_RULING), messages, appConfig))

      // Then
      doc should containElementWithID("liability-decision-edit")
    }

    "Not render liability details if empty" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication(),
        withoutDecision()
      )
      val d = c.decision.getOrElse(Decision())
      val l = c.application.asLiabilityOrder

      // When
      val doc = view(
        views.html.partials.liabilities.liability_details(c = c, liabilityForm = LiabilityDetailsForm.liabilityDetailsForm(l), decisionForm = form.liabilityCompleteForm(d)
        )(requestWithPermissions(), messages, appConfig))

      // Then
      doc shouldNot containElementWithID("liability_details-decision")
    }

    "Render liability details if present" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication(),
        withDecision(
          bindingCommodityCode = "code",
          justification = "justification",
          goodsDescription = "description",
          methodSearch = Some("search"),
          methodExclusion = Some("exclusion")
        )
      )
      val d = c.decision.getOrElse(Decision())
      val l = c.application.asLiabilityOrder

      // When
      val doc = view(
        views.html.partials.liabilities.liability_details(c = c, liabilityForm = LiabilityDetailsForm.liabilityDetailsForm(l), decisionForm = form.liabilityCompleteForm(d)
        )(requestWithPermissions(), messages, appConfig))

      // Then
      doc should containElementWithID("liability_details-decision")
      doc.getElementById("liability-decision-code") should containText("code")
      doc.getElementById("liability-decision-description") should containText("description")
      doc.getElementById("liability-decision-justification") should containText("justification")
      doc.getElementById("liability-decision-searches") should containText("search")
      doc.getElementById("liability-decision-exclusions") should containText("exclusion")
    }

    "Disallow Case completion" when {

      "User is not permitted" in {
        val c = aCase(
          withStatus(CaseStatus.OPEN),
          withLiabilityApplication(),
          withDecision(
            bindingCommodityCode = "code",
            justification = "",
            goodsDescription = "",
            methodSearch = None,
            methodExclusion = None
          )
        )
        val d = c.decision.getOrElse(Decision())
        val l = c.application.asLiabilityOrder

        // When
        val doc = view(
          views.html.partials.liabilities.liability_details(c = c, liabilityForm = LiabilityDetailsForm.liabilityDetailsCompleteForm(l), decisionForm = form.liabilityCompleteForm(d)
          )(requestWithPermissions(), messages, appConfig))

        doc shouldNot containElementWithID("liability-complete-heading")
        doc shouldNot containElementWithID("liability-complete-button")
      }

      "Decision is invalid" in {
        // Given
        val c = aCase(
          withStatus(CaseStatus.OPEN),
          withLiabilityApplication(),
          withDecision(
            bindingCommodityCode = "code",
            justification = "",
            goodsDescription = "",
            methodSearch = None,
            methodExclusion = None
          )
        )
        val d = c.decision.getOrElse(Decision())
        val l = c.application.asLiabilityOrder

        // When
        val doc = view(
          views.html.partials.liabilities.liability_details(c = c, liabilityForm = LiabilityDetailsForm.liabilityDetailsCompleteForm(l), decisionForm = form.liabilityCompleteForm(d)
          )(requestWithPermissions(Permission.COMPLETE_CASE), messages, appConfig))

        // Then
        doc should containElementWithID("liability_details-decision")
        doc shouldNot containElementWithID("liability-complete_details-heading")
        doc should containElementWithID("liability-complete_decision-heading")
        doc should containElementWithID("constraint-justification")
        doc should containElementWithID("constraint-goodsDescription")
        doc should containElementWithID("constraint-methodSearch")
        doc shouldNot containElementWithID("constraint-methodExclusion")
        doc.getElementById("liability-complete-button") should haveAttribute("disabled", "disabled")
      }

      "Application is invalid" in {
        // Given
        val c = aCase(
          withStatus(CaseStatus.OPEN),
          withLiabilityApplication(
            contact = Contact(name = "", email = "")
          ),
          withDecision(
            bindingCommodityCode = "code",
            justification = "justification",
            goodsDescription = "goods",
            methodSearch = Some("search"),
            methodExclusion = Some("exclusion")
          )
        )
        val d = c.decision.getOrElse(Decision())
        val l = c.application.asLiabilityOrder

        // When
        val doc = view(
          views.html.partials.liabilities.liability_details(c = c, liabilityForm = LiabilityDetailsForm.liabilityDetailsCompleteForm(l), decisionForm = form.liabilityCompleteForm(d)
          )(requestWithPermissions(Permission.COMPLETE_CASE), messages, appConfig))

        // Then
        doc should containElementWithID("liability_details-decision")
        doc should containElementWithID("liability-complete_details-heading")
        doc shouldNot containElementWithID("liability-complete_decision-heading")
        doc should containElementWithID("constraint-contactName")
        doc.getElementById("liability-complete-button") should haveAttribute("disabled", "disabled")
      }
    }

    "Allow Case completion" in {
      // Given
      val c = aCase(
        withStatus(CaseStatus.OPEN),
        withLiabilityApplication(),
        withDecision(
          bindingCommodityCode = "code",
          justification = "justification",
          goodsDescription = "description",
          methodSearch = Some("search"),
          methodExclusion = Some("exclusion")
        )
      )
      val d = c.decision.getOrElse(Decision())
      val l = c.application.asLiabilityOrder

      // When
      val doc = view(
        views.html.partials.liabilities.liability_details(c = c, liabilityForm = LiabilityDetailsForm.liabilityDetailsForm(l), decisionForm = form.liabilityCompleteForm(d)
        )(requestWithPermissions(Permission.COMPLETE_CASE), messages, appConfig))

      // Then
      doc should containElementWithID("liability_details-decision")
      doc shouldNot containElementWithID("liability-complete_decision-heading")
      doc.getElementById("liability-complete-button") shouldNot haveAttribute("disabled", "disabled")
    }

  }

  "Render view PDF link" when {
    for (status: CaseStatus <- caseIsCompletedStatuses) {
      s"Case status is $status" in {
        val c = aCase(
          withStatus(status),
          withLiabilityApplication()
        )
        val d = c.decision.getOrElse(Decision())
        val l = c.application.asLiabilityOrder

        // When
        val doc = view(
          views.html.partials.liabilities.liability_details(c = c, liabilityForm = LiabilityDetailsForm.liabilityDetailsCompleteForm(l),
            decisionForm = form.liabilityCompleteForm(d))(requestWithPermissions(), messages, appConfig))

        doc should containElementWithID("liability-ruling-certificate-link")
      }
    }
  }

  "Not render view PDF link" when {
    for (status: CaseStatus <- CaseStatus.values.filterNot(caseIsCompletedStatuses.contains(_))) {
      s"Case status is $status" in {
        val c = aCase(
          withStatus(status),
          withLiabilityApplication()
        )
        val d = c.decision.getOrElse(Decision())
        val l = c.application.asLiabilityOrder

        // When
        val doc = view(
          views.html.partials.liabilities.liability_details(c = c, liabilityForm = LiabilityDetailsForm.liabilityDetailsCompleteForm(l),
            decisionForm = form.liabilityCompleteForm(d))(requestWithPermissions(), messages, appConfig))

        doc shouldNot containElementWithID("liability-ruling-certificate-link")
      }
    }
  }

}
