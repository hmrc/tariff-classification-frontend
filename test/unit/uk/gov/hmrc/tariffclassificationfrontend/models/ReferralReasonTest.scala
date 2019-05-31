package uk.gov.hmrc.tariffclassificationfrontend.models

import uk.gov.hmrc.play.test.UnitSpec

class ReferralReasonTest extends UnitSpec {

  "format" should {
    "render to String" in {
      ReferralReason.format(ReferralReason.REQUEST_MORE_INFO) shouldBe "To request more information"
      ReferralReason.format(ReferralReason.REQUEST_SAMPLE) shouldBe "To request a sample"
    }
  }

}
