package models

object KeywordStatus extends Enumeration {
  type KeywordStatus = Value
  val APPROVED, REJECTED, REPLACED = Value

  def format(status: KeywordStatus): String =
    status match {
      case APPROVED => "Approved"
      case REJECTED => "Rejected"
      case REPLACED => "Renamed"
    }
}
