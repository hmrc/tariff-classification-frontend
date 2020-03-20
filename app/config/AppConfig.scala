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

package config

import java.time.Clock

import javax.inject.{Inject, Singleton}
import play.api.Mode.Mode
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig


@Singleton
class AppConfig @Inject()(
  config: Configuration,
  environment: Environment,
  servicesConfig: ServicesConfig
) {

//  override protected def mode: Mode = environment.mode

  private lazy val contactHost = config.getOptional[String]("contact-frontend.host").getOrElse("")
  private lazy val contactFormServiceIdentifier = config.get[String]("appName")

  lazy val assetsPrefix: String = config.get[String]("assets.url") + config.get[String]("assets.version")
  lazy val analyticsToken: String = config.get[String]("google-analytics.token")
  lazy val analyticsHost: String = config.get[String]("google-analytics.host")
  lazy val managerEnrolment: String = config.get[String]("auth.enrolments.manager")
  lazy val teamEnrolment: String = config.get[String]("auth.enrolments.team")
  lazy val readOnlyEnrolment: String = config.get[String]("auth.enrolments.read-only")
  lazy val checkEnrolment: Boolean = config.get[String]("auth.enrolments.enabled").toBoolean
  lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val bindingTariffClassificationUrl: String = servicesConfig.baseUrl("binding-tariff-classification")
  lazy val rulingUrl: String = servicesConfig.baseUrl("binding-tariff-ruling-frontend")
  lazy val emailUrl: String = servicesConfig.baseUrl("email")
  lazy val emailRendererUrl: String = servicesConfig.baseUrl("hmrc-email-renderer")
  lazy val fileStoreUrl: String = servicesConfig.baseUrl("binding-tariff-filestore")
  lazy val decisionLifetimeYears: Int = config.get[Int]("app.decision-lifetime-years")
  lazy val fileUploadMaxSize: Int = config.get[String]("fileupload.maxSize").toInt
  lazy val fileUploadMimeTypes: Set[String] = config.get[String]("fileupload.mimeTypes").split(",").map(_.trim).toSet
  lazy val apiToken: String = config.get[String]("auth.api-token")
  lazy val pdfGeneratorUrl: String = servicesConfig.baseUrl("pdf-generator-service")
  lazy val activeDaysElapsedSlaLimit: Int = config.get[Int]("app.active-days-elapsed-sla-limit")
  lazy val commodityCodePath: String = config.get[String]("app.commodity-code-path")
  lazy val shutterFlag: Boolean = config.get[String]("shutter.enabled").toBoolean
  lazy val shutterExcludedUrls: String = config.get[String]("shutter.urls.excluded")
  lazy val newLiabilityDetails: Boolean = config.get[String]("toggle.new-liability-details").toBoolean


  lazy val clock: Clock = Clock.systemUTC()

}
