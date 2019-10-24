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

package uk.gov.hmrc.tariffclassificationfrontend.config

import java.time.Clock

import javax.inject.{Inject, Singleton}
import play.api.Mode.Mode
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.config.ServicesConfig

@Singleton
class AppConfig @Inject()(val runModeConfiguration: Configuration, environment: Environment) extends ServicesConfig {

  override protected def mode: Mode = environment.mode

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val contactHost = runModeConfiguration.getString("contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "MyService"

  lazy val assetsPrefix: String = loadConfig("assets.url") + loadConfig("assets.version")
  lazy val analyticsToken: String = loadConfig("google-analytics.token")
  lazy val analyticsHost: String = loadConfig("google-analytics.host")
  lazy val managerEnrolment: String = loadConfig("auth.enrolments.manager")
  lazy val teamEnrolment: String = loadConfig("auth.enrolments.team")
  lazy val readOnlyEnrolment: String = loadConfig("auth.enrolments.read-only")
  lazy val checkEnrolment: Boolean = loadConfig("auth.enrolments.enabled").toBoolean
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val bindingTariffClassificationUrl: String = baseUrl("binding-tariff-classification")
  lazy val rulingUrl: String = baseUrl("binding-tariff-ruling-frontend")
  lazy val emailUrl: String = baseUrl("email")
  lazy val emailRendererUrl: String = baseUrl("hmrc-email-renderer")
  lazy val fileStoreUrl: String = baseUrl("binding-tariff-filestore")
  lazy val decisionLifetimeYears: Int = getInt("app.decision-lifetime-years")
  lazy val fileUploadMaxSize: Int = loadConfig("fileupload.maxSize").toInt
  lazy val fileUploadMimeTypes: Set[String] = loadConfig("fileupload.mimeTypes").split(",").map(_.trim).toSet
  lazy val apiToken: String = loadConfig("auth.api-token")
  lazy val pdfGeneratorUrl: String = baseUrl("pdf-generator-service")
  lazy val activeDaysElapsedSlaLimit: Int = getInt("app.active-days-elapsed-sla-limit")
  lazy val commodityCodePath: String = loadConfig("app.commodity-code-path")
  lazy val shutterFlag: Boolean = loadConfig("shutter.enabled").toBoolean
  lazy val shutterExcludedUrls: String = loadConfig("shutter.urls.excluded")


  lazy val clock: Clock = Clock.systemUTC()

  def runningAsDev: Boolean = {
    runModeConfiguration
      .getString("run.mode")
      .map(_.equals(Mode.Dev.toString))
      .getOrElse(Mode.Dev.equals(mode))
  }

}
