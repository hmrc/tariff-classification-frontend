package models.viewmodels

import java.time.Instant

import models.Case

case class MessagesTabViewModel(
    name: String,
    date: Instant,
    message: String
)

object MessagesTabViewModel {
  def fromCase(cse: Case): MessagesTabViewModel = {
    ???
    val correspondenceApplication = cse.application.asCorrespondence
  }
}
