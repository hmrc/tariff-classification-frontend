package uk.gov.hmrc.tariffclassificationfrontend

import scala.io.Source

trait ResourceFiles {

  def fromFile(path: String): String = {
    Source.fromFile(path).getLines().mkString
  }

}
