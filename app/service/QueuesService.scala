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

package service

import javax.inject.Singleton
import models.{ApplicationType, Queue, Queues}
import scala.concurrent.Future

@Singleton
class QueuesService {
  def getAll: Future[List[Queue]] = Future.successful(Queues.allQueues)

  def getAllById: Future[Map[String, Queue]] = Future.successful(Queues.allQueuesById)

  def getNonGateway: Future[List[Queue]] = Future.successful(Queues.allDynamicQueues)

  def getOneBySlug(slug: String): Future[Option[Queue]] =
    Future.successful(Queues.allQueuesBySlug.get(slug))

  def getOneById(id: String): Future[Option[Queue]] =
    Future.successful(Queues.allQueuesById.get(id))

  def getAllForCaseType(applicationType: ApplicationType): Future[List[Queue]] =
    Future.successful(Queues.queuesForType(applicationType))

  def getQueuesById(ids: Seq[String]): Future[Seq[Option[Queue]]] =
    Future.successful(ids.map(id => Queues.queueById(id)))
}
