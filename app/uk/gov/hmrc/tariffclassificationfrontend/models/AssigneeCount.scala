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

package uk.gov.hmrc.tariffclassificationfrontend.models

case class AssigneeCount(op: Operator, count: Int) {
  def name: String = op.safeName
  def firstName: String = name.split(" ").head
  def lastName: String = name.split(" ").last
}

object AssigneeCount{

  def apply(cases: Seq[Case]): Seq[AssigneeCount] = {
    cases
      .filter(_.assignee.isDefined)
      .groupBy(c => c.assignee.get).map(x => AssigneeCount(x._1, x._2.size))
      .toSeq
      .sortBy(a =>(a.lastName, a.firstName))
  }
}
