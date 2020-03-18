package models.viewmodels

import models.CaseStatus
import uk.gov.hmrc.play.test.UnitSpec
import utils.Cases

class LiabilityViewModelSpec extends UnitSpec {

  "fromCase" should {

    "create a cancelled view model" in {

      val c = Cases.btiCaseExample.copy(status = CaseStatus.CANCELLED)

      println(LiabilityViewModel.fromCase(c))

    }

  }

}
