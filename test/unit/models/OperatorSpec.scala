package models

class OperatorSpec extends ModelsBaseSpec {
  "Operator 'manager'" should {

    "return true for an operator with manager role" in {
      val op = Operator("1", role = Role.CLASSIFICATION_MANAGER)
      op.manager shouldBe true
    }

    "return false for an operator without manager role" in {
      val op = Operator("1", role = Role.CLASSIFICATION_OFFICER)
      op.manager shouldBe false
    }

  }

}
