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

package uk.gov.hmrc.tariffclassificationfrontend.controllers

import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.BDDMockito._
import org.mockito.Mockito
import org.mockito.Mockito.{never, verify}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.tariffclassificationfrontend.config.AppConfig
import uk.gov.hmrc.tariffclassificationfrontend.forms.{CommodityCodeConstraints, DecisionForm, DecisionFormData, DecisionFormMapper}
import uk.gov.hmrc.tariffclassificationfrontend.models.{Case, CaseStatus}
import uk.gov.hmrc.tariffclassificationfrontend.service.{CasesService, CommodityCodeService, FileStoreService}
import uk.gov.tariffclassificationfrontend.utils.Cases

import scala.concurrent.Future

class RulingControllerSpec extends WordSpec with Matchers with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach with ControllerCommons {

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)
  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private val casesService = mock[CasesService]
  private val fileService = mock[FileStoreService]
  private val mapper = mock[DecisionFormMapper]
  private val commodityCodeService = mock[CommodityCodeService]
  private val decisionForm = new DecisionForm(new CommodityCodeConstraints(commodityCodeService))

  private implicit val hc = HeaderCarrier()

  private val controller = new RulingController(new SuccessfulAuthenticatedAction, casesService, fileService, mapper, decisionForm, messageApi, appConfig)

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(casesService)
  }

  "Edit Ruling" should {
    val caseWithStatusNEW = Cases.btiCaseExample.copy(status = CaseStatus.NEW)
    val caseWithStatusOPEN = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)
    val attachment = Cases.storedAttachment

    "return OK and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(caseWithStatusOPEN)))
      given(fileService.getAttachments(refEq(caseWithStatusOPEN))(any[HeaderCarrier])).willReturn(Future.successful(Seq(attachment)))

      val result = controller.editRulingDetails("reference")(newFakeGETRequestWithCSRF(app))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should (include("Ruling") and include("<form"))
    }

    "redirect to Ruling for non OPEN Statuses" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(caseWithStatusNEW)))

      val result = controller.editRulingDetails("reference")(newFakeGETRequestWithCSRF(app))
      status(result) shouldBe Status.SEE_OTHER
      contentType(result) shouldBe None
      charset(result) shouldBe None
      redirectLocation(result) shouldBe Some("/tariff-classification/cases/reference/ruling")
    }

    "return Not Found and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(None))

      val result = controller.editRulingDetails("reference")(newFakeGETRequestWithCSRF(app))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

  }

  "Update Ruling" should {
    val caseWithStatusNEW = Cases.btiCaseExample.copy(status = CaseStatus.NEW)
    val caseWithStatusOPEN = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)
    val updatedCase = Cases.btiCaseExample.copy(status = CaseStatus.OPEN)
    val attachment = Cases.storedAttachment

    val aValidForm = newFakePOSTRequestWithCSRF(app, Map(
      "bindingCommodityCode" -> "",
      "goodsDescription" -> "",
      "methodSearch" -> "",
      "justification" -> "",
      "methodCommercialDenomination" -> "",
      "methodExclusion" -> "",
      "attachments" -> "[]")
    )

    "return OK and HTML content type" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(caseWithStatusOPEN)))
      given(casesService.updateCase(any[Case])(any[HeaderCarrier])).willReturn(Future.successful(updatedCase))
      given(fileService.getAttachments(refEq(updatedCase))(any[HeaderCarrier])).willReturn(Future.successful(Seq(attachment)))

      val result = controller.updateRulingDetails("reference")(aValidForm)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("Ruling")
      contentAsString(result) shouldNot include("<form")
    }

    "redirect back to edit ruling on Form Error" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(caseWithStatusOPEN)))
      given(fileService.getAttachments(refEq(caseWithStatusOPEN))(any[HeaderCarrier])).willReturn(Future.successful(Seq(attachment)))

      val result = controller.updateRulingDetails("reference")(newFakePOSTRequestWithCSRF(app))
      verify(casesService, never()).updateCase(any[Case])(any[HeaderCarrier])
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should (include("Ruling") and include("<form"))
    }

    "redirect to Ruling for non OPEN Statuses" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(Some(caseWithStatusNEW)))
      given(mapper.mergeFormIntoCase(any[Case], any[DecisionFormData])).willReturn(caseWithStatusNEW)

      val result = controller.updateRulingDetails("reference")(aValidForm)
      verify(casesService, never()).updateCase(any[Case])(any[HeaderCarrier])
      status(result) shouldBe Status.SEE_OTHER
      contentType(result) shouldBe None
      charset(result) shouldBe None
      redirectLocation(result) shouldBe Some("/tariff-classification/cases/reference/ruling")
    }

    "return Not Found and HTML content type on missing Case" in {
      given(casesService.getOne(refEq("reference"))(any[HeaderCarrier])).willReturn(Future.successful(None))

      val result = controller.updateRulingDetails("reference")(aValidForm)
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      contentAsString(result) should include("We could not find a Case with reference")
    }

  }
}
