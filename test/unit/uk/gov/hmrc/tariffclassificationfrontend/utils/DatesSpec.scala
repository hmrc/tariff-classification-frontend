package uk.gov.hmrc.tariffclassificationfrontend.utils

import java.time.LocalDate

import org.scalatest.FlatSpec

class DatesSpec extends FlatSpec {

  "Format" should "convert date to string" in {
    val date = LocalDate.of(2018,1,1)
    val output = Dates.format(date)

    assert(output == "01 Jan 2018")
  }

}
