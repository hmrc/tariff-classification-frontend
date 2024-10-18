/*
 * Copyright 2024 HM Revenue & Customs
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
import utils.Cases.{aCase, btiApplicationExample, btiCaseExample, expiredRuling}
import views.html.templates.cover_letter_template

import java.io.File
import java.nio.file.{Files, Paths}
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.io.Source

class PdfGeneratorServiceSpec extends SpecBase with ScalaFutures {

  private val env: Environment       = injector.instanceOf[Environment]
  private val fopFactory: FopFactory = injector.instanceOf[FopFactory]
  private val countriesService = injector.instanceOf[CountriesService]
  private val coverLetterTemplate: Html =
    injector.instanceOf[cover_letter_template].apply(
      aCase(_ => btiCaseExample),
      expiredRuling,
      _ => countriesService.getAllCountriesById.get("UY").map(_.countryName)
    )(messages)
  private val coverLetterTemplateWithSamples: Html =
    injector.instanceOf[cover_letter_template].apply(
      aCase(_ => btiCaseExample.copy(application = btiApplicationExample.copy(sampleToBeProvided = true))),
      expiredRuling,
      _ => countriesService.getAllCountriesById.get("UY").map(_.countryName)
    )(messages)

  private val pdfGeneratorService: PdfGeneratorService = new PdfGeneratorService(fopFactory, env)

  private val xlsTransformer = Source.fromResource("cover_letter_template.xml").mkString

  "render" must {

    def test(
              pdfType: String,
              template: Html,
              visibleContent: Seq[String],
              hiddenContent: Seq[String]
            ): Unit = {
      s"create a PDF $pdfType" in {
        val result = Await.result(pdfGeneratorService.render(template, xlsTransformer), Duration.Inf)

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

          lines(2) shouldBe "Advance Tariff Ruling"
          lines      should contain allElementsOf visibleContent
          lines should contain noElementsOf hiddenContent
        } finally document.close()

      }
    }

    val headings: Seq[String] = Seq(
      "About this decision",
      "What to do if you disagree with this decision",
      "If you want to appeal to an independent tribunal",
      "More information about appeals and reviews",
      "Important information about communicating by email"
    )

    val input: Seq[(String, Html, Seq[String], Seq[String])] = Seq(
      ("withoutSamples", coverLetterTemplate, headings ++ Seq("Asking for a review with HMRC"), Seq("Your samples have been kept")),
      ("withSamples", coverLetterTemplateWithSamples, headings ++ Seq("Asking for a review with HMRC", "Your samples have been kept"), Seq.empty)
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
