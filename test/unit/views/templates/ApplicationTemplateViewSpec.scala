/*
 * Copyright 2021 HM Revenue & Customs
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

package views.templates

import models.{Case, StoredAttachment}
import org.jsoup.nodes.Document
import utils.{Cases, Dates}
import views.ViewMatchers._
import views.ViewSpec
import views.html.templates.application_template

class ApplicationTemplateViewSpec extends ViewSpec {

  private val caseWithoutAgent = Cases.simpleCaseExample
  private val caseWithAgent    = Cases.btiCaseExample

  private def createView(c: Case, attachments: Seq[StoredAttachment]) =
    application_template(c, attachments, None, s => Some("dummy country name"))(
      authenticatedFakeRequest,
      messages,
      appConfig
    )

  "Application pdf view" should {

    "contain the details for a trader" in {
      val doc = view(createView(caseWithoutAgent, Seq.empty))
      doc shouldNot containElementWithID("pdf.application.section.applyingFor.heading")
    }

    "contain the details for an agent" in {
      val doc = view(createView(caseWithAgent, Seq.empty))
      containsCommonSections(doc)
      doc.getElementById("pdf.application.section.applyingFor.heading") should containText(
        "Details of the business, organisation or individual you represent"
      )
    }

    "contain the details for re-issued BTI" in {
      val doc = view(
        createView(
          caseWithoutAgent
            .copy(application = Cases.btiApplicationExample.copy(reissuedBTIReference = Some("REISSUE1234"))),
          Seq.empty
        )
      )
      doc.getElementById("application.reissuedBTIReference").text() shouldBe "REISSUE1234"
    }

    "contain the details for uploaded files" in {
      view(createView(caseWithoutAgent, Seq.empty)).getElementById("application.attachments").text() shouldBe "None"

      val docFiles = view(
        createView(
          caseWithoutAgent,
          Seq(
            Cases.storedAttachment.copy(id = "FILE_ID_PDF", fileName = "pdfFile.pdf", mimeType = "application/pdf"),
            Cases.storedAttachment.copy(id = "FILE_ID_JPG", fileName = "image.jpg", mimeType   = "image/jpg")
          )
        )
      )

      docFiles.getElementById("application.attachments").text() shouldBe "pdfFile.pdf image.jpg"
    }

    "contain the details for related BTI case" in {
      val doc = view(
        createView(
          caseWithoutAgent
            .copy(application = Cases.btiApplicationExample.copy(relatedBTIReference = Some("RELATED1234"))),
          Seq.empty
        )
      )

      doc.getElementById("application.relatedBTIReference").text() shouldBe "RELATED1234"
    }

    "contain the details for legal problems" in {
      val doc = view(
        createView(
          caseWithoutAgent
            .copy(application = Cases.btiApplicationExample.copy(knownLegalProceedings = Some("Legal problems"))),
          Seq.empty
        )
      )

      doc.getElementById("application.knownLegalProceedings").text() shouldBe "Legal problems"
    }

    "contain the details for other information" in {
      val doc = view(
        createView(
          caseWithoutAgent
            .copy(application = Cases.btiApplicationExample.copy(otherInformation = Some("Other information"))),
          Seq.empty
        )
      )

      doc.getElementById("application.otherInformation").text() shouldBe "Other information"
    }
  }

  private def containsCommonSections(doc: Document) = {
    doc.getElementById("application.submitted").text()     shouldBe s"${Dates.format(Cases.btiCaseExample.createdDate)}"
    doc.getElementById("application.casereference").text() shouldBe s"${Cases.btiCaseExample.reference}"

    doc should containElementWithID("pdf.application.section.applicant.heading")
    doc should containElementWithID("pdf.application.section.aboutItem.heading")
    doc should containElementWithID("pdf.application.section.other.heading")
  }

}
