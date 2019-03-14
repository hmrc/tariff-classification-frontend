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

package uk.gov.hmrc.tariffclassificationfrontend.service

import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.tariffclassificationfrontend.models.Queue

class QueuesServiceSpec extends UnitSpec {

  private val service = new QueuesService()

  "Get All Queues" should {

    "retrieve queues" in {
      service.getAll.size shouldBe 5
    }
  }

  "Get Non Gateway" should {

    "retrieve queues" in {
      service.getNonGateway.size shouldBe 4
    }
  }

  "Get Queue By Slug" should {

    "find relevant queue" in {
      service.getOneBySlug("gateway") shouldBe Some(Queue("1", "gateway", "Gateway"))
    }

    "not find unknown queue" in {
      service.getOneBySlug("unknown") shouldBe None
    }
  }

  "Get Queue By Id" should {

    "find relevant queue" in {
      service.getOneById("1") shouldBe Some(Queue("1", "gateway", "Gateway"))
    }

    "not find unknown queue" in {
      service.getOneById("0") shouldBe None
    }
  }

  "Get queue name" should {
    "return the queue name Gateway" in {
      service.queueNameForId("1") shouldBe "Gateway"
    }
    "return the queue name ACT" in {
      service.queueNameForId("2") shouldBe "ACT"
    }
    "return the queue name CAP" in {
      service.queueNameForId("3") shouldBe "CAP"
    }
    "return the queue name Cars" in {
      service.queueNameForId("4") shouldBe "Cars"
    }
    "return the queue name ELM" in {
      service.queueNameForId("5") shouldBe "ELM"
    }
    "return the queue name unknown" in {
      service.queueNameForId("unknown") shouldBe "unknown"
    }
  }

}
