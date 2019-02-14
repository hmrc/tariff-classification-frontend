package uk.gov.hmrc.tariffclassificationfrontend.models

import uk.gov.hmrc.play.test.UnitSpec

class AppealStatusTest extends UnitSpec {

  "Appeal format" should {
    "format 'In Progress'" in {
      AppealStatus.format(Some(AppealStatus.IN_PROGRESS)) shouldBe "Under appeal"
    }

    "format 'allowed'" in {
      AppealStatus.format(Some(AppealStatus.ALLOWED)) shouldBe "Appeal allowed"
    }

    "format 'dismissed'" in {
      AppealStatus.format(Some(AppealStatus.DISMISSED)) shouldBe "Appeal dismissed"
    }

    "format 'none'" in {
      AppealStatus.format(None) shouldBe "None"
    }
  }

}
