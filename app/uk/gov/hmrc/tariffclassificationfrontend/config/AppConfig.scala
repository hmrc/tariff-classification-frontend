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

import java.time.ZoneId

import javax.inject.{Inject, Singleton}
import org.apache.commons.lang3.StringUtils.isNoneBlank
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
  lazy val authEnrolment: Option[String] = Some(loadConfig("auth.enrolment")).filter(isNoneBlank(_))
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val bindingTariffClassificationUrl: String = baseUrl("binding-tariff-classification")
  lazy val emailUrl: String = baseUrl("email")
  lazy val emailRendererUrl: String = baseUrl("hmrc-email-renderer")
  lazy val fileStoreUrl: String = baseUrl("binding-tariff-filestore")
  lazy val decisionLifetimeYears: Int = getInt("app.decision-lifetime-years")
  lazy val zoneId: ZoneId = ZoneId.of("UTC")

  def runningAsDev: Boolean = {
    runModeConfiguration
      .getString("run.mode")
      .map(_.equals(Mode.Dev.toString))
      .getOrElse(Mode.Dev.equals(mode))
  }

}
