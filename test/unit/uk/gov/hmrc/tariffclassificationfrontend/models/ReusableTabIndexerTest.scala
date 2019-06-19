package uk.gov.hmrc.tariffclassificationfrontend.models

import uk.gov.hmrc.play.test.UnitSpec

class ReusableTabIndexerTest extends UnitSpec {

  "ReusableTabIndexer" should {

    "start by default with expected first index" in {
      val tabIndexer = ReusableTabIndexer()
      tabIndexer.nextTabIndex() shouldBe 0
    }

    "increment by default with expected increment" in {
      val tabIndexer = ReusableTabIndexer()
      tabIndexer.nextTabIndex()
      tabIndexer.nextTabIndex()
      tabIndexer.nextTabIndex() shouldBe 2
    }

    "increment by jump with expected increment" in {
      val tabIndexer = ReusableTabIndexer()
      tabIndexer.nextTabIndex()
      tabIndexer.nextTabIndexWithJump(50) shouldBe 50
    }

    "return current index without consuming index when calling current tab index" in {
      val tabIndexer = ReusableTabIndexer(startTabIndex = 5)
      tabIndexer.nextTabIndex()
      tabIndexer.currentTabIndex() shouldBe 5
      tabIndexer.currentTabIndex() shouldBe 5
    }

    "start at specified index when supplied" in {
      val tabIndexer = ReusableTabIndexer(startTabIndex = 10)
      tabIndexer.nextTabIndex() shouldBe 10
    }

    "increment by specified increment when supplied" in {
      val tabIndexer = ReusableTabIndexer(indexIncrement = 100)
      tabIndexer.nextTabIndex()
      tabIndexer.nextTabIndex()
      tabIndexer.nextTabIndex() shouldBe 200
    }

  }

}
