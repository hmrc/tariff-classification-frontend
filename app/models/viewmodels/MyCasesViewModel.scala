package models.viewmodels

import models.Case

case class MyCasesViewModel(atars : Seq[Case], liabilities : Seq[Case])

object MyCasesViewModel {

  def apply(allCasesAssignedToMe : Seq[Case]): MyCasesViewModel = {

    val atars = allCasesAssignedToMe.filter(_.application.isBTI)

    val liabilities = allCasesAssignedToMe.filter(_.application.isLiabilityOrder)

    MyCasesViewModel(atars, liabilities)
  }

}
