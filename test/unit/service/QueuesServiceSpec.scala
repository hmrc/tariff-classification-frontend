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

import models.Queue

class QueuesServiceSpec extends ServiceSpecBase {

  private val service = injector.instanceOf[QueuesService]

  "Get All Queues" should {

    "retrieve queues" in {
      await(service.getAll).size shouldBe 5
    }
  }

  "Get Non Gateway" should {

    "retrieve queues" in {
      await(service.getNonGateway).size shouldBe 4
    }
  }

  "Get Queue By Slug" should {

    "find relevant queue" in {
      await(service.getOneBySlug("gateway")) shouldBe Some(Queue("1", "gateway", "Gateway"))
    }

    "not find unknown queue" in {
      await(service.getOneBySlug("unknown")) shouldBe None
    }
  }

  "Get Queue By Id" should {

    "find relevant queue" in {
      await(service.getOneById("1")) shouldBe Some(Queue("1", "gateway", "Gateway"))
    }

    "not find unknown queue" in {
      await(service.getOneById("0")) shouldBe None
    }
  }

}
