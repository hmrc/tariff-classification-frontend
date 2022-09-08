/*
 * Copyright 2022 HM Revenue & Customs
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

package views.v2

import models._
import models.forms._
import models.response.{FileStoreInitiateResponse, UpscanFormTemplate}
import models.viewmodels._
import models.viewmodels.atar.AttachmentsTabViewModel
import models.viewmodels.miscellaneous.DetailsViewModel
import play.api.data.Form
import utils.Cases
import utils.Cases._
import views.ViewMatchers.{containElementWithID, containText}
import views.ViewSpec
import views.html.v2.miscellaneous_view

import java.time.Instant

class MiscellaneousViewSpec extends ViewSpec {

  private val sampleStatusTabViewModel = SampleStatusTabViewModel(
    "caseReference",
    isSampleBeingSent = false,
    Some(SampleSend.AGENT),
    None,
    "location",
    sampleActivity = Paged.empty[Event]
  )

  private val detailsTab: DetailsViewModel = DetailsViewModel(
    "1",
    "summary",
    Some("Case Contact name"),
    "some desc",
    Some("detailed description"),
    "2 Jan 2005",
    Some("SOC/554/2015/JN")
  )

  private val attachmentsTab: AttachmentsTabViewModel = AttachmentsTabViewModel(
    "1",
    "Bob",
    Seq.empty,
    Seq.empty
  )

  private val activityTab: ActivityViewModel =
    ActivityViewModel("1", None, None, Instant.now, Paged.empty, Seq.empty, "misc")

  private val activityForm: Form[ActivityFormData] = ActivityForm.form

  private val contact: Contact = Contact("Bob Dilan", "bob@gmail.com", Some("545353"))

  private val address: Address = Address("Street building", "Sofia", None, Some("NE2 8PN"));

  private val exampleMessages =
    List(Message("name", Instant.now(), "message"), Message("name2", Instant.now(), "message2"))

  private val messagesTab: MessagesTabViewModel = MessagesTabViewModel("reference", exampleMessages)

  private val messageForm: Form[MessageFormData] = MessageForm.form

  def miscellaneousView: miscellaneous_view = injector.instanceOf[miscellaneous_view]

  def uploadAttachmentForm: Form[String] = UploadAttachmentForm.form

  val initiateResponse = FileStoreInitiateResponse(
    id              = "id",
    upscanReference = "ref",
    uploadRequest = UpscanFormTemplate(
      "http://localhost:20001/upscan/upload",
      Map("key" -> "value")
    )
  )

  def keywordForm: Form[String] = KeywordForm.form

  val emptyKeywordsTabViewModel: KeywordsTabViewModel = KeywordsTabViewModel("", Set.empty[String], Nil)

  "Miscellaneous View" should {

    "render with case reference" in {
      val c = aMiscellaneousCase(withReference("reference"), withMiscellaneousApplication)
      val doc = view(
        miscellaneousView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          detailsTab,
          messagesTab,
          messageForm,
          sampleStatusTabViewModel,
          attachmentsTab,
          uploadAttachmentForm,
          initiateResponse,
          activityTab,
          activityForm,
          Seq.empty
        )
      )
      doc.getElementById("case-reference") should containText(c.reference)
    }

    "render Case Details tab" in {
      val c = aMiscellaneousCase(withReference("reference"), withMiscellaneousApplication)
      val doc = view(
        miscellaneousView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          detailsTab,
          messagesTab,
          messageForm,
          sampleStatusTabViewModel,
          attachmentsTab,
          uploadAttachmentForm,
          initiateResponse,
          activityTab,
          activityForm,
          Seq.empty
        )
      )
      doc should containElementWithID("case_details_tab")
    }

    "render Messages details" in {
      val c = aMiscellaneousCase(withReference("reference"), withMiscellaneousApplication)
      val doc = view(
        miscellaneousView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          detailsTab,
          messagesTab,
          messageForm,
          sampleStatusTabViewModel,
          attachmentsTab,
          uploadAttachmentForm,
          initiateResponse,
          activityTab,
          activityForm,
          Seq.empty
        )
      )
      doc should containElementWithID("messages_tab")
    }

    "render Sample Details tab" in {
      val c = aMiscellaneousCase(withReference("reference"), withMiscellaneousApplication)
      val doc = view(
        miscellaneousView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          detailsTab,
          messagesTab,
          messageForm,
          sampleStatusTabViewModel,
          attachmentsTab,
          uploadAttachmentForm,
          initiateResponse,
          activityTab,
          activityForm,
          Seq.empty
        )
      )
      doc should containElementWithID("sample_status_tab")
    }

    "render Attachments Details tab" in {
      val c = aMiscellaneousCase(withReference("reference"), withMiscellaneousApplication)
      val doc = view(
        miscellaneousView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          detailsTab,
          messagesTab,
          messageForm,
          sampleStatusTabViewModel,
          attachmentsTab,
          uploadAttachmentForm,
          initiateResponse,
          activityTab,
          activityForm,
          Seq.empty
        )
      )
      doc should containElementWithID("attachments_tab")
    }

    "render Activity tab" in {
      val c = aMiscellaneousCase(withReference("reference"), withMiscellaneousApplication)
      val doc = view(
        miscellaneousView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          detailsTab,
          messagesTab,
          messageForm,
          sampleStatusTabViewModel,
          attachmentsTab,
          uploadAttachmentForm,
          initiateResponse,
          activityTab,
          activityForm,
          Seq.empty
        )
      )
      doc should containElementWithID("activity_tab")
    }

  }
}
