package uk.gov.hmrc.tariffclassificationfrontend.models

import java.time.{Clock, Instant}

case class CommodityCode
(
  code: String,
  expiry: Option[Instant] = None
) {
  def isExpired(implicit clock: Clock): Boolean = expiry.exists(_.isBefore(clock.instant()))
}
