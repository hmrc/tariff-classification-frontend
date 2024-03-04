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

package models.response

sealed abstract class UploadError(
  val errorCode: String,
  val errorMessageKey: String
) extends Product
    with Serializable

case object FileTooLarge extends UploadError("EntityTooLarge", "cases.attachment.upload.error.restrictionSize")

case object NoFileSelected extends UploadError("InvalidArgument", "cases.attachment.upload.error.mustSelect")

case class Other(
  override val errorCode: String
) extends UploadError(errorCode, "cases.attachment.upload.error")

object UploadError {
  private val knownErrors: Set[UploadError] = Set(FileTooLarge, NoFileSelected)

  def fromErrorCode(errorCode: String): UploadError =
    knownErrors.find(_.errorCode == errorCode).getOrElse(Other(errorCode))
}
