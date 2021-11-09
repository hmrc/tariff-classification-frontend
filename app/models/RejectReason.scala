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

package models

object RejectReason extends Enumeration {
  type RejectReason = Value
  val APPLICATION_WITHDRAWN, ATAR_RULING_ALREADY_EXISTS, DUPLICATE_APPLICATION, NO_INFO_FROM_TRADER, OTHER = Value

  def format(reason: RejectReason): String =
    reason match {
      case APPLICATION_WITHDRAWN      => "Application withdrawn"
      case ATAR_RULING_ALREADY_EXISTS => "ATaR ruling already exists"
      case DUPLICATE_APPLICATION      => "Duplicate application"
      case NO_INFO_FROM_TRADER        => "No information from trader"
      case OTHER                      => "Other"
    }
}
