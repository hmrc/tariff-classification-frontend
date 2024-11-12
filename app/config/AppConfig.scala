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

package config

import play.api.{Configuration, Mode}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

@Singleton
class AppConfig @Inject() (config: Configuration, servicesConfig: ServicesConfig) {

  lazy val managerEnrolment: String               = config.get[String]("auth.enrolments.manager")
  lazy val teamEnrolment: String                  = config.get[String]("auth.enrolments.team")
  lazy val readOnlyEnrolment: String              = config.get[String]("auth.enrolments.read-only")
  lazy val checkEnrolment: Boolean                = config.get[String]("auth.enrolments.enabled").toBoolean
  lazy val bindingTariffClassificationUrl: String = servicesConfig.baseUrl("binding-tariff-classification")
  lazy val emailUrl: String                       = servicesConfig.baseUrl("email")
  lazy val emailRendererUrl: String               = servicesConfig.baseUrl("hmrc-email-renderer")
  lazy val fileStoreUrl: String                   = servicesConfig.baseUrl("binding-tariff-filestore")
  lazy val pdfGeneratorUrl: String                = servicesConfig.baseUrl("pdf-generator-service")
  lazy val rulingUrl: String                      = servicesConfig.baseUrl("binding-tariff-ruling-frontend")
  lazy val decisionLifetimeYears: Int             = config.get[Int]("app.decision-lifetime-years")
  lazy val decisionLifetimeDays: Int              = config.get[Int]("app.decision-lifetime-days")
  lazy val fileUploadMaxSize: Int                 = config.get[String]("fileupload.maxSize").toInt
  lazy val apiToken: String                       = config.get[String]("auth.api-token")
  lazy val activeDaysElapsedSlaLimit: Int         = config.get[Int]("app.active-days-elapsed-sla-limit")
  lazy val commodityCodePath: String              = config.get[String]("app.commodity-code-path")
  lazy val shutterFlag: Boolean                   = config.get[String]("shutter.enabled").toBoolean
  lazy val shutterExcludedUrls: String            = config.get[String]("shutter.urls.excluded")
  lazy val entryDateYearLowerBound: Int           = 2010
  lazy val dateOfReceiptYearLowerBound: Int       = 2010

  lazy val maxUriLength: Long = config.underlying.getBytes("pekko.http.parsing.max-uri-length")

  lazy val downloadMaxRetries: Int = config.getOptional[Int]("download.max-retries").getOrElse(3)

  lazy val downloadRetryInterval: FiniteDuration = {
    if (config.has("download.interval"))
      FiniteDuration(config.underlying.getDuration("download.interval").toMillis, TimeUnit.MILLISECONDS)
    else
      3.seconds
  }

  lazy val keywordsCacheExpiration: FiniteDuration = {
    val javaDuration = config.underlying.getDuration("keywords-cache.expiration")
    FiniteDuration(javaDuration.toMillis, TimeUnit.MILLISECONDS)
  }

  lazy val clock: Clock = Clock.systemUTC()

  def runningAsDev: Boolean =
    config
      .getOptional[String]("run.mode")
      .getOrElse("Dev")
      .equals(Mode.Dev.toString)

  lazy val host: String = config.get[String]("platform-url.host")
}
