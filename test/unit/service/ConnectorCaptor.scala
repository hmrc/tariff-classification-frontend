/*
 * Copyright 2020 HM Revenue & Customs
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

package service

import connector.BindingTariffClassificationConnector
import models.Case
import models.request.NewEventRequest
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.verify
import uk.gov.hmrc.http.HeaderCarrier

trait ConnectorCaptor {


  protected def theEventCreatedFor(connector: BindingTariffClassificationConnector, c: Case): NewEventRequest = {
    val captor: ArgumentCaptor[NewEventRequest] = ArgumentCaptor.forClass(classOf[NewEventRequest])
    verify(connector).createEvent(refEq(c), captor.capture())(any[HeaderCarrier])
    captor.getValue
  }

  protected def theCaseUpdating(connector: BindingTariffClassificationConnector): Case = {
    val captor: ArgumentCaptor[Case] = ArgumentCaptor.forClass(classOf[Case])
    verify(connector).updateCase(captor.capture())(any[HeaderCarrier])
    captor.getValue
  }

}
