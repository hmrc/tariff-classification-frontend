/*
 * Copyright 2025 HM Revenue & Customs
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

package services

import base.SpecBase
import org.apache.fop.apps.FopFactory
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.scalatest.concurrent.ScalaFutures
import play.api.Environment
import play.twirl.api.Html
import utils.Cases.{aCase, btiApplicationExample, btiCaseExample, decisionWithExclusion, expiredRuling, liabilityCaseWithDecisionExample}
import views.html.templates.{cover_letter_template, decision_template, ruling_template}

import java.io.File
import java.nio.file.{Files, Paths}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

class PdfGeneratorServiceSpec extends SpecBase with ScalaFutures {

  private val env: Environment       = injector.instanceOf[Environment]
  private val fopFactory: FopFactory = injector.instanceOf[FopFactory]
  private val countriesService       = injector.instanceOf[CountriesService]
  private val coverLetterTemplate: Html =
    injector
      .instanceOf[cover_letter_template]
      .apply(
        aCase(_ => btiCaseExample),
        expiredRuling.copy(explanation =
          Some(
            "The UK operate within the World Customs Organization (WCO) Harmonized System (HS), the worldwidesystem for uniform classification of goods. The UK is aware that WCO member(s), the European Union (EU) (orother Customs Authorities. The item has been classified in accordance with the Harmonised SystemExplanatory Notes (HSEN)s to heading 9503 and the General Interpretative Rules 1-6. I have taken these intoaccount when reaching my decision, to ensure the HS is applied consistently amongst WCO members. I haveresearched and taken account of rulings issued by Great Britain for rulings made by other contracting parties ofthe Harmonized System (HS), which includes European Union member states. Through this research, I havefound that similar products have also been classified to 9503007000 Tricycles, scooters, pedal cars and similarwheeled toys; dolls' carriages; dolls; other toys; reduced size ('scale') models and similar recreational models,working or not; puzzles of all kinds Examples of previous decisions are always carefully considered as part ofour classification process to ensure consistency in how the HS is applied."
          )
        ),
        _ => countriesService.getAllCountriesById.get("UY").map(_.countryName)
      )(messages)
  private val coverLetterTemplateWithSamples: Html =
    injector
      .instanceOf[cover_letter_template]
      .apply(
        aCase(_ => btiCaseExample.copy(application = btiApplicationExample.copy(sampleToBeProvided = true))),
        expiredRuling.copy(explanation =
          Some(
            "The UK operate within the World Customs Organization (WCO) Harmonized System (HS), the worldwidesystem for uniform classification of goods. The UK is aware that WCO member(s), the European Union (EU) (orother Customs Authorities. The item has been classified in accordance with the Harmonised SystemExplanatory Notes (HSEN)s to heading 9503 and the General Interpretative Rules 1-6. I have taken these intoaccount when reaching my decision, to ensure the HS is applied consistently amongst WCO members. I haveresearched and taken account of rulings issued by Great Britain for rulings made by other contracting parties ofthe Harmonized System (HS), which includes European Union member states. Through this research, I havefound that similar products have also been classified to 9503007000 Tricycles, scooters, pedal cars and similarwheeled toys; dolls' carriages; dolls; other toys; reduced size ('scale') models and similar recreational models,working or not; puzzles of all kinds Examples of previous decisions are always carefully considered as part ofour classification process to ensure consistency in how the HS is applied."
          )
        ),
        _ => countriesService.getAllCountriesById.get("UY").map(_.countryName)
      )(messages)
  private val applicationCertificate: Html =
    injector
      .instanceOf[ruling_template]
      .apply(
        aCase(_ =>
          btiCaseExample.copy(
            reference = "600000034",
            application = btiApplicationExample.copy(sampleToBeProvided = true, goodName = "Snow man jacket"),
            keywords = Set("jacket", "snow", "products")
          )
        ),
        expiredRuling.copy(
          goodsDescription =
            "Termo produced in Uruguay with stamps from Norway and legal justification asdkjasjoijasjdajsdaoida oiajsd oaijda oijadsd jasdioajso jasdja sod asidjdwofjewofjevvds vsdjsd ofjsd jsdofj sdf",
          justification =
            "Termo produced in Uruguay with stamps from Norway and legal justification justification asdkjasjoijasjdajsdaoida oiajsd oaijda oijadsd jasdioajso jasdja sod asidjdwofjewofjevvds vsdjsd ofjsd jsdofj sdf justification asdkjasjoijasjdajsdaoida oiajsd oaijda oijadsd jasdioajso jasdja sod asidjdwofjewofjevvds vsdjsd ofjsd jsdofj sdf"
        ),
        _ => countriesService.getAllCountriesById.get("UY").map(_.countryName)
      )(messages)
  private val decisionLetter: Html =
    injector
      .instanceOf[decision_template]
      .apply(
        aCase(_ => liabilityCaseWithDecisionExample),
        decisionWithExclusion.copy(
          goodsDescription = "Termo produced in Uruguay with stamps from Norway and legal justification",
          justification = "Termo produced in Uruguay with stamps from Norway and legal justification justification"
        )
      )(messages)

  private val pdfGeneratorService: PdfGeneratorService = new PdfGeneratorService(fopFactory, env)

  private val xlsTransformer = Source.fromResource("cover_letter_template.xml").mkString

  private val xlsRulingTransformer = Source.fromResource("ruling_template.xml").mkString

  private val xlsDecisionTransformer = Source.fromResource("decision_template.xml").mkString

  "render" must {

    def test(
      pdfType: String,
      template: Html,
      transformer: String,
      visibleContent: Seq[String],
      hiddenContent: Seq[String]
    ): Unit = {
      s"create a PDF $pdfType" in {
        val result = Await.result(pdfGeneratorService.render(template, transformer), Duration.Inf)

        val fileName = s"test/resources/fop/$pdfType-test.pdf"
        Files.write(Paths.get(fileName), result)
      }

      s"write a valid $pdfType PDF file" in {

        val file: File           = new File(s"test/resources/fop/$pdfType-test.pdf")
        val document: PDDocument = PDDocument.load(file)

        try {
          val textStripper: PDFTextStripper = new PDFTextStripper
          val text: String                  = textStripper.getText(document)
          val lines: List[String]           = text.split("\n").toList.map(_.trim)

          lines should contain allElementsOf visibleContent
          lines should contain noElementsOf hiddenContent
        } finally document.close()

      }
    }

    val headings: Seq[String] = Seq(
      "Advance Tariff Ruling",
      "About this decision",
      "What to do if you disagree with this decision",
      "If you want to appeal to an independent tribunal",
      "More information about appeals and reviews",
      "Important information about communicating by email"
    )

    val rulingCertificateHeadings: Seq[String] = Seq(
      "Advance Tariff Ruling Certificate",
      "Holder details Ruling details",
      "Legal information about this ruling"
    )

    val decisionLetterHeadings: Seq[String] = Seq(
      "Liability details from C592",
      "Information about this decision"
    )

    val input: Seq[(String, Html, String, Seq[String], Seq[String])] = Seq(
      (
        "withoutSamples",
        coverLetterTemplate,
        xlsTransformer,
        headings ++ Seq("Asking for a review with HMRC"),
        Seq("Your samples have been kept")
      ),
      (
        "withSamples",
        coverLetterTemplateWithSamples,
        xlsTransformer,
        headings ++ Seq("Asking for a review with HMRC", "Samples"),
        Seq.empty
      ),
      ("applicationCertificate", applicationCertificate, xlsRulingTransformer, rulingCertificateHeadings, Seq.empty),
      ("decisionLetter", decisionLetter, xlsDecisionTransformer, decisionLetterHeadings, Seq.empty)
    )

    input.foreach(args => (test _).tupled(args))
  }

  "resolve" must {

    def test(
      description: String,
      hrefToResolve: String,
      path: String
    ): Unit =
      s"construct XLS file references when the required file $description" in {
        val filePath = Paths.get(path)
        val file     = filePath.toFile

        if (!file.exists()) {
          Files.createFile(filePath)
        }

        try {
          val result = pdfGeneratorService.resolve(hrefToResolve, file.getParent)
          result.getSystemId.replace("./", "") shouldBe file.getCanonicalFile.toURI.toString
        } finally file.delete()
      }

    val input: Seq[(String, String, String)] = Seq(
      ("is in '/conf' (JAR environment)", "*/file.xls", "conf/file.xls"),
      ("is not in '/conf' (local environment)", "*/file.xls", "app/views/components/fop/file.xls"),
      ("does not start with a custom resolver '*/'", "test/resources/fop/file.xls", "test/resources/fop/file.xls")
    )

    input.foreach(args => (test _).tupled(args))
  }
}
