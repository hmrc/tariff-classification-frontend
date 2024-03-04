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

import java.nio.charset.StandardCharsets
import java.util.Base64

object Base64Utils {

  private val encoder: Base64.Encoder = Base64.getEncoder
  private val decoder: Base64.Decoder = Base64.getDecoder

  def encode(text: String): String = encoder.encodeToString(text.getBytes(StandardCharsets.UTF_8))
  def decode(text: String): String = new String(decoder.decode(text))

}
