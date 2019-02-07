package uk.gov.hmrc.tariffclassificationfrontend.utils

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.play.test.UnitSpec

class FormUtilsTest extends UnitSpec {

  "Form Utils 'Array Length'" should {
    case class Data(array: Seq[String] = Seq.empty, text: String = "")

    val form = Form(
      mapping(
        "array" -> seq(text),
        "text" -> text
      )(Data.apply)(Data.unapply)
    )

    "Calculate Length of non array field" in {
      FormUtils.arrayLength("text", form) shouldBe 0
      FormUtils.arrayLength("text", form.fill(Data(text = "value"))) shouldBe 0
    }

    "Calculate Length of array field" in {
      FormUtils.arrayLength("array", form) shouldBe 0
      FormUtils.arrayLength("array", form.fill(Data(array = Seq("value")))) shouldBe 1
      FormUtils.arrayLength("array", form.fill(Data(array = Seq("value", "value")))) shouldBe 2
    }
  }
}
