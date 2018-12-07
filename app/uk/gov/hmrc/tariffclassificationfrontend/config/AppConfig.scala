/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.Mode.Mode
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.ServicesConfig

@Singleton
class AppConfig @Inject()(val runModeConfiguration: Configuration, environment: Environment) extends ServicesConfig {

  override protected def mode: Mode = environment.mode

  private def loadConfig(key: String) = runModeConfiguration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private val contactHost = runModeConfiguration.getString("contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "MyService"

  def assetsPrefix: String = loadConfig("assets.url") + loadConfig("assets.version")
  def analyticsToken: String = loadConfig("google-analytics.token")
  def analyticsHost: String = loadConfig("google-analytics.host")
  def reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  def reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  def bindingTariffClassificationUrl: String = baseUrl("binding-tariff-classification")

  lazy val whitelistDestination: String = getString("whitelist.destination")
  lazy val whitelistedIps: Seq[String] = {
    getString("whitelist.allowedIps")
      .split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
  }
  lazy val whitelistedExcludedPaths: Seq[String] = {
    getString("whitelist.excluded")
      .split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
  }

}
