/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.tariffclassificationfrontend.views.templates

import org.jsoup.nodes.Document
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, StoredAttachment}
import uk.gov.hmrc.tariffclassificationfrontend.utils.Dates
import uk.gov.hmrc.tariffclassificationfrontend.views.ViewSpec
import uk.gov.hmrc.tariffclassificationfrontend.views.html.templates.application_template
import uk.gov.tariffclassificationfrontend.utils.Cases

class ApplicationTemplateViewSpec extends ViewSpec {

  private val caseWithoutAgent = Cases.simpleCaseExample
  private val caseWithAgent = Cases.btiCaseExample

  private def createView(c: Case, attachments: Seq[StoredAttachment]) = application_template(c, attachments)(authenticatedFakeRequest, messages, appConfig)

  "Application pdf view" must {

    "contain the details for a trader" in {
      val doc = asDocument(createView(caseWithoutAgent, Seq.empty))

      containsCommonSections(doc)
      assertNotRenderedById(doc, "pdf.application.section.applyingFor.heading")
    }

    "contain the details for an agent" in {
      val doc = asDocument(createView(caseWithAgent, Seq.empty))

      containsCommonSections(doc)
      assertRenderedById(doc, "pdf.application.section.applyingFor.heading")
    }

    "contain the details for re-issued BTI" in {
      val doc = asDocument(createView(caseWithoutAgent.copy(application = Cases.btiApplicationExample.copy(reissuedBTIReference = Some("REISSUE1234"))), Seq.empty))
      doc.getElementById("application.reissuedBTIReference").text() shouldBe "REISSUE1234"
    }

    "contain the details for confidential information" in {
      val doc = asDocument(createView(caseWithoutAgent.copy(application = Cases.btiApplicationExample.copy(
        confidentialInformation = Some("Confidential information"))), Seq.empty))

      doc.getElementById("application.confidentialInformation").text() shouldBe "Confidential information"
    }

    "contain the details for uploaded files" in {
      asDocument(createView(caseWithoutAgent, Seq.empty)).getElementById("application.attachments").text() shouldBe "None"

      val docFiles = asDocument(createView(caseWithoutAgent, Seq(
        Cases.storedAttachment.copy(id = "FILE_ID_PDF", fileName = "pdfFile.pdf", mimeType = "application/pdf"),
        Cases.storedAttachment.copy(id = "FILE_ID_JPG", fileName = "image.jpg", mimeType = "image/jpg"))))

      docFiles.getElementById("application.attachments").text() shouldBe "pdfFile.pdf image.jpg"
    }

    "contain the details for related BTI case" in {
      val doc = asDocument(createView(caseWithoutAgent.copy(application = Cases.btiApplicationExample.copy(
        relatedBTIReference = Some("RELATED1234"))), Seq.empty))

      doc.getElementById("application.relatedBTIReference").text() shouldBe "RELATED1234"
    }

    "contain the details for legal problems" in {
      val doc = asDocument(createView(caseWithoutAgent.copy(application = Cases.btiApplicationExample.copy(
        knownLegalProceedings = Some("Legal problems"))), Seq.empty))

      doc.getElementById("application.knownLegalProceedings").text() shouldBe "Legal problems"
    }

    "contain the details for other information" in {
      val doc = asDocument(createView(caseWithoutAgent.copy(application = Cases.btiApplicationExample.copy(
        otherInformation = Some("Other information"))), Seq.empty))

      doc.getElementById("application.otherInformation").text() shouldBe "Other information"
    }
  }

  private def containsCommonSections(doc: Document) = {
    doc.getElementById("application.submitted").text() shouldBe s"Application submitted on: ${Dates.format(Cases.btiCaseExample.createdDate)}"
    doc.getElementById("application.casereference").text() shouldBe s"Application reference number: ${Cases.btiCaseExample.reference}"
    assertRenderedById(doc, "pdf.application.section.applicant.heading")
    assertRenderedById(doc, "pdf.application.section.applicationType.heading")
    assertRenderedById(doc, "pdf.application.section.aboutItem.heading")
    assertRenderedById(doc, "pdf.application.section.other.heading")
  }

}
