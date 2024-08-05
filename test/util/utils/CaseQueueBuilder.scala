/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import models.{ApplicationType, Pagination}

trait CaseQueueBuilder {

  def buildQueryUrl(
    types: Seq[ApplicationType] = ApplicationType.values.toSeq,
    withStatuses: String,
    queueId: String = "",
    assigneeId: String,
    pag: Pagination
  ): String = {
    val sortBy = "application.type,application.status,days-elapsed"
    val queryString = s"/cases?application_type=${types.map(_.name).mkString(",")}&queue_id=$queueId&" +
      s"assignee_id=$assigneeId&status=$withStatuses&sort_by=$sortBy&sort_direction=desc&" +
      s"page=${pag.page}&page_size=${pag.pageSize}"
    queryString
  }

  def buildQueryUrlAllQueues(
    types: Seq[ApplicationType] = ApplicationType.values.toSeq,
    statuses: String,
    queueIds: Seq[String],
    assigneeId: String,
    pagination: Pagination
  ): String = {
    val sortBy = "application.type,application.status,days-elapsed"
    val queryString =
      s"/cases?application_type=${types.map(_.name).mkString(",")}&queue_id=${queueIds.mkString(",")}" +
        s"&assignee_id=$assigneeId&status=$statuses&sort_by=$sortBy&sort_direction=desc&page=" +
        s"${pagination.page}&page_size=${pagination.pageSize}"
    queryString
  }

  case class TestPagination(
    override val page: Int = 1,
    override val pageSize: Int = 2
  ) extends Pagination
}
