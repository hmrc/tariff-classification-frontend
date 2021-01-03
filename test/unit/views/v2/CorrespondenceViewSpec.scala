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

package views.v2

import java.time.Instant

import models._
import models.forms.{ActivityForm, ActivityFormData, KeywordForm, UploadAttachmentForm}
import models.viewmodels.atar.AttachmentsTabViewModel
import models.viewmodels.correspondence.{CaseDetailsViewModel, ContactDetailsTabViewModel, CorrespondenceSampleTabViewModel}
import models.viewmodels.{ActivityViewModel, CaseViewModel, KeywordsTabViewModel}
import play.api.data.Form
import utils.Cases
import utils.Cases._
import views.ViewMatchers.{containElementWithID, containText}
import views.ViewSpec
import views.html.v2.correspondence_view

class CorrespondenceViewSpec extends ViewSpec {

  private val sampleStatusTabViewModel = CorrespondenceSampleTabViewModel(
    "1",
    true,
    false,
    Some("officer"),
    "return",
    "return",
    sampleActivity = Paged.empty[Event]
  )

  private val caseDetailsTab: CaseDetailsViewModel = CaseDetailsViewModel(
    "1",
    "summary",
    "some desc",
    "2 Jan 2005",
    None,
    relatedBTIReferences = List.empty
  )

  private val attachmentsTab: AttachmentsTabViewModel = AttachmentsTabViewModel(
    "1",
    "Bob",
    Seq.empty,
    Seq.empty
  )

  private val activityTab: ActivityViewModel = ActivityViewModel("1", None, None, Instant.now, Paged.empty, Seq.empty, "corr")

  private val activityForm: Form[ActivityFormData] = ActivityForm.form

  private val contact: Contact = Contact("Bob Dilan", "bob@gmail.com", Some("545353"))

  private val address: Address = Address("Street building", "Sofia", None, Some("NE2 8PN"));

  def correspondenceView: correspondence_view = injector.instanceOf[correspondence_view]

  def uploadAttachmentForm: Form[String] = UploadAttachmentForm.form

  def keywordForm: Form[String] = KeywordForm.form

  val contactDetails: ContactDetailsTabViewModel = ContactDetailsTabViewModel("Case source", contact, Some("943534543"), address)

  val emptyKeywordsTabViewModel: KeywordsTabViewModel = KeywordsTabViewModel("", Set.empty[String], Nil)


  "Correspondence View" should {

    "render with case reference" in {
      val c = aCorrespondenceCase(withReference("reference"), withCorrespondenceApplication)
      val doc = view(
        correspondenceView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          caseDetailsTab,
          contactDetails,
          sampleStatusTabViewModel,
          attachmentsTab,
          uploadAttachmentForm,
          activityTab,
          activityForm,
          Seq.empty
        )
      )
      doc.getElementById("case-reference") should containText(c.reference)
    }

    "render Case Details tab" in {
      val c = aCorrespondenceCase(withReference("reference"), withCorrespondenceApplication)
      val doc = view(
        correspondenceView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          caseDetailsTab,
          contactDetails,
          sampleStatusTabViewModel,
          attachmentsTab,
          uploadAttachmentForm,
          activityTab,
          activityForm,
          Seq.empty
        )
      )
      doc should containElementWithID("case_details_tab")
    }

    "render Contact Details tab" in {
      val c = aCorrespondenceCase(withReference("reference"), withCorrespondenceApplication)
      val doc = view(
        correspondenceView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          caseDetailsTab,
          contactDetails,
          sampleStatusTabViewModel,
          attachmentsTab,
          uploadAttachmentForm,
          activityTab,
          activityForm,
          Seq.empty
        )
      )
      doc should containElementWithID("contact_details_tab")
    }

    //    "render Messages details" in {
    //      val c = aCorrCase(withReference("reference"), withCorrespondenceApplication)
    //      val doc = view(
    //        correspondenceView(
    //          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
    //          caseDetailsTab,
    //          contactDetails,
    //          sampleStatusTabViewModel,
    //          attachmentsTab,
    //          uploadAttachmentForm,
    //          activityTab,
    //          activityForm,
    //          Seq.empty
    //        )
    //      )
    //      doc should containElementWithID("messages_tab")
    //    }


    "render Sample Details tab" in {
      val c = aCorrespondenceCase(withReference("reference"), withCorrespondenceApplication)
      val doc = view(
        correspondenceView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          caseDetailsTab,
          contactDetails,
          sampleStatusTabViewModel,
          attachmentsTab,
          uploadAttachmentForm,
          activityTab,
          activityForm,
          Seq.empty
        )
      )
      doc should containElementWithID("samples_tab")
    }


    "render Attachments Details tab" in {
      val c = aCorrespondenceCase(withReference("reference"), withCorrespondenceApplication)
      val doc = view(
        correspondenceView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          caseDetailsTab,
          contactDetails,
          sampleStatusTabViewModel,
          attachmentsTab,
          uploadAttachmentForm,
          activityTab,
          activityForm,
          Seq.empty
        )
      )
      doc should containElementWithID("attachments_tab")
    }

    "render Activity tab" in {
      val c = aCorrespondenceCase(withReference("reference"), withCorrespondenceApplication)
      val doc = view(
        correspondenceView(
          CaseViewModel.fromCase(c, Cases.operatorWithoutPermissions),
          caseDetailsTab,
          contactDetails,
          sampleStatusTabViewModel,
          attachmentsTab,
          uploadAttachmentForm,
          activityTab,
          activityForm,
          Seq.empty
        )
      )
      doc should containElementWithID("activity_tab")
    }

  }
}