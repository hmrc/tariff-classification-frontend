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

import javax.inject.Singleton
import models.Queue
import models.Queues._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class QueuesService {

  def getAll: Future[Seq[Queue]] = Future.successful(Seq(gateway, act, cap, cars, elm))

  def getNonGateway: Future[Seq[Queue]] = getAll map(_.filterNot (_ == gateway))

  def getOneBySlug(slug: String): Future[Option[Queue]] = {
    getAll.map(_.find(_.slug == slug))
  }

  def getOneById(id: String): Future[Option[Queue]] = {
    getAll.map(_.find(_.id == id))
  }

}