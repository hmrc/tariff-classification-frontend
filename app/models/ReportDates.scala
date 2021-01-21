package models

import java.time.Instant
import cats.syntax.either._
import models.PseudoDateRange.PseudoDateRange
import play.api.mvc.PathBindable

sealed trait ReportDates {
  val `type`: ReportDatesType
}

case class RelativeDateRange(relativeDateRange: PseudoDateRange) extends ReportDates
case class CustomDateRange(from: Instant, to: Instant) extends ReportDates
case class CustomDate(from: Instant) extends ReportDates
case class CustomRelativeDateRange(numberOfDays: Int) extends ReportDates

object PseudoDateRange extends Enumeration {
  type PseudoDateRange = Value
  val ALL_TIME, TODAY, YESTERDAY, LAST_SEVEN_DAYS, LAST_THIRTY_DAYS, LAST_CUSTOM_DAYS, THIS_MONTH, LAST_MONTH,
    CUSTOM_DATE, CUSTOM_DATE_RANGE = Value
}

sealed abstract class ReportDatesType(val name: String) extends Product with Serializable {
  def prettyName: String = this match {
    case ReportDatesType.RELATIVE_DATE_RANGE        => "Relative date range"
    case ReportDatesType.CUSTOM_DATE_RANGE          => "Custom date range"
    case ReportDatesType.CUSTOM_DATE                => "Custom date"
    case ReportDatesType.CUSTOM_RELATIVE_DATE_RANGE => "Custom relative date range"
  }
}

object ReportDatesType {
  val values = Set(RELATIVE_DATE_RANGE, CUSTOM_DATE_RANGE, CUSTOM_DATE, CUSTOM_RELATIVE_DATE_RANGE)

  def withName(name: String) = values.find(_.name.equalsIgnoreCase(name)).getOrElse(throw new NoSuchElementException)

  case object RELATIVE_DATE_RANGE extends ReportDatesType("RELATIVE_DATE_RANGE")
  case object CUSTOM_DATE_RANGE extends ReportDatesType("CUSTOM_DATE_RANGE")
  case object CUSTOM_DATE extends ReportDatesType("CUSTOM_DATE")
  case object CUSTOM_RELATIVE_DATE_RANGE extends ReportDatesType("CUSTOM_RELATIVE_DATE_RANGE")

  implicit def reportDatesTypePathBindable(
    implicit stringBindable: PathBindable[String]
  ): PathBindable[ReportDatesType] =
    new PathBindable[ReportDatesType] {
      def bind(key: String, value: String): Either[String, ReportDatesType] =
        Either
          .catchOnly[NoSuchElementException] {
            ReportDatesType.withName(value)
          }
          .leftMap(_ => "Invalid report dates type")
      def unbind(key: String, value: ReportDatesType): String =
        stringBindable.unbind(key, value.name)
    }
}
