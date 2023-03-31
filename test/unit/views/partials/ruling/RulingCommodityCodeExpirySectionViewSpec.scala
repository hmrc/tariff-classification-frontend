/*
 * Copyright 2023 HM Revenue & Customs
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

package views.partials.ruling

import models.viewmodels.atar.RulingTabViewModel
import models.{CaseStatus, CommodityCode}
import utils.Cases._
import views.ViewMatchers._
import views.ViewSpec
import views.html.partials.ruling.ruling_commodity_code_expiry_section

import java.time.Instant

// scalastyle:off magic.number
class RulingCommodityCodeExpirySectionViewSpec extends ViewSpec {

  "Ruling Expiry Section" should {

    "render nothing" when {
      "commodity code is none" in {
        val c         = aCase(withDecision(bindingCommodityCode = "123"))
        val rulingTab = RulingTabViewModel.fromCase(c).copy(bindingCommodityCode = None)

        // When
        val doc = view(ruling_commodity_code_expiry_section(rulingTab))

        // Then
        doc shouldNot containElementWithID("ruling_commodity_code_expiry_section")
      }

      "commodity code has no expiry" in {
        val c         = aCase()
        val rulingTab = RulingTabViewModel.fromCase(c).copy(bindingCommodityCode = Some(CommodityCode("123")))

        // When
        val doc = view(ruling_commodity_code_expiry_section(rulingTab))

        // Then
        doc shouldNot containElementWithID("ruling_commodity_code_expiry_section")
      }
    }

    "render 'expired'" when {
      "commodity code expiry is in the past" in {
        val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision(bindingCommodityCode = "123"))
        val rulingTab = RulingTabViewModel
          .fromCase(c)
          .copy(bindingCommodityCode = Some(CommodityCode("123", Some(Instant.now.minusSeconds(60)))))

        // When
        val doc =
          view(ruling_commodity_code_expiry_section(rulingTab))

        // Then
        doc should containElementWithID("ruling_commodity_code_expiry_section")
        doc should containElementWithID("ruling_commodity_code_expiry_section-warning_expired")
        doc should containElementWithID("ruling_commodity_code_expiry_section-message_expired")
      }
    }

    "render 'expiring'" when {
      "commodity code expiry is in the future" in {
        val c = aCase(withStatus(CaseStatus.COMPLETED), withDecision(bindingCommodityCode = "123"))
        val rulingTab = RulingTabViewModel
          .fromCase(c)
          .copy(bindingCommodityCode = Some(CommodityCode("123", Some(Instant.now.plusSeconds(60)))))

        // When
        val doc = view(ruling_commodity_code_expiry_section(rulingTab))

        // Then
        doc should containElementWithID("ruling_commodity_code_expiry_section")
        doc should containElementWithID("ruling_commodity_code_expiry_section-warning_expiring")
        doc should containElementWithID("ruling_commodity_code_expiry_section-message_expiring")
      }
    }

    "not render expiration message" when {
      "case is CANCELLED" in {
        val c = aCase(withStatus(CaseStatus.CANCELLED), withDecision(bindingCommodityCode = "123"))
        val rulingTab = RulingTabViewModel
          .fromCase(c)
          .copy(bindingCommodityCode = Some(CommodityCode("123", Some(Instant.now.minusSeconds(60)))))

        // When
        val doc = view(ruling_commodity_code_expiry_section(rulingTab))

        // Then
        doc should containElementWithID("ruling_commodity_code_expiry_section")
        doc should containElementWithID("ruling_commodity_code_expiry_section-warning_expired")
        doc shouldNot containElementWithID("ruling_commodity_code_expiry_section-message_expired")
      }

      "case is OPEN" in {
        val c = aCase(withStatus(CaseStatus.OPEN), withDecision(bindingCommodityCode = "123"))
        val rulingTab = RulingTabViewModel
          .fromCase(c)
          .copy(bindingCommodityCode = Some(CommodityCode("123", Some(Instant.now.minusSeconds(60)))))

        // When
        val doc = view(ruling_commodity_code_expiry_section(rulingTab))

        // Then
        doc should containElementWithID("ruling_commodity_code_expiry_section")
        doc should containElementWithID("ruling_commodity_code_expiry_section-warning_expired")
        doc shouldNot containElementWithID("ruling_commodity_code_expiry_section-message_expired")
      }
    }

  }
}
