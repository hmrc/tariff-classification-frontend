package uk.gov.hmrc.tariffclassificationfrontend.forms.mappings

import play.api.data.FormError
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.CancelReason

class FormMappingsTest extends UnitSpec {

  "One of" should {

    "return valid val if part of enum" in {
      val mapping = FormMappings.oneOf("My Error", CancelReason)
      val result = mapping.bind(Map("" -> "INVALIDATED_CODE_CHANGE"))

      result shouldBe Right("INVALIDATED_CODE_CHANGE")
    }

    "return form error if value is invalid" in {
      val mapping = FormMappings.oneOf("My Error", CancelReason)
      val result = mapping.bind(Map("" -> "CABBAGE"))

      result shouldBe Left(Seq(FormError("", "My Error")))
    }

    "return form error if value is missing" in {
      val mapping = FormMappings.oneOf("My Error", CancelReason)
      val result = mapping.bind(Map())

      result shouldBe Left(Seq(FormError("", "My Error")))
    }

    "return form error if value is set to nothing" in {
      val mapping = FormMappings.oneOf("My Error", CancelReason)
      val result = mapping.bind(Map("" -> ""))

      result shouldBe Left(Seq(FormError("", "My Error")))
    }
  }

  "field non empty" should {
    "return valid val if some text present" in {
      val mapping = FormMappings.fieldNonEmpty("My Error")
      val result = mapping.bind(Map("" -> "Some value"))

      result shouldBe Right("Some value")
    }

    "return form error if value missing" in {
      val mapping = FormMappings.fieldNonEmpty("My Error")
      val result = mapping.bind(Map())

      result shouldBe Left(Seq(FormError("", "My Error")))
    }
  }

  "text non empty" should {
    "return valid val if some text present" in {
      val mapping = FormMappings.textNonEmpty("My Error")
      val result = mapping.bind(Map("" -> "Some value"))

      result shouldBe Right("Some value")
    }

    "return form error if value present but empty" in {
      val mapping = FormMappings.textNonEmpty("My Error")
      val result = mapping.bind(Map("" -> ""))

      result shouldBe Left(Seq(FormError("", "My Error")))
    }

    "return form error if value missing" in {
      val mapping = FormMappings.textNonEmpty("My Error")
      val result = mapping.bind(Map())

      result shouldBe Left(Seq(FormError("", "My Error")))
    }
  }
}
