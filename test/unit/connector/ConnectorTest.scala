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

package connector

import akka.actor.ActorSystem
import base.SpecBase
import config.AppConfig
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import play.api.libs.ws.WSClient
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import utils.{ResourceFiles, WiremockTestServer}

trait ConnectorTest extends SpecBase with WiremockTestServer with ResourceFiles with BeforeAndAfterAll {

  private val actorSystem = ActorSystem.create("testActorSystem")

  protected val mockAppConfig: AppConfig = mock[AppConfig]

  protected val fakeAuthToken = "AUTH_TOKEN"

  protected val wsClient: WSClient = ws

  protected val authenticatedHttpClient =
    new AuthenticatedHttpClient(realConfig, realHttpAudit, wsClient, mockAppConfig, actorSystem)
  protected val standardHttpClient = new DefaultHttpClient(realConfig, realHttpAudit, wsClient, actorSystem)

  override def beforeAll(): Unit = {
    super.beforeAll()

    when(mockAppConfig.maxUriLength) thenReturn 2048
    when(mockAppConfig.fileStoreUrl) thenReturn getUrl
    when(mockAppConfig.bindingTariffClassificationUrl) thenReturn getUrl
    when(mockAppConfig.rulingUrl) thenReturn getUrl

    when(mockAppConfig.emailUrl) thenReturn getUrl
    when(mockAppConfig.emailRendererUrl) thenReturn getUrl
    when(mockAppConfig.pdfGeneratorUrl) thenReturn getUrl

    when(mockAppConfig.apiToken) thenReturn fakeAuthToken
  }

}
