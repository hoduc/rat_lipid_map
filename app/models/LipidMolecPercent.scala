package models

import play.api.libs.json._

case class LipidMolecPercent(id: Int, baseRt: Double, mainAreaC: String, percent: Double, lipidClassId: Int, organId: Int)

object LipidMolecPercent{
  implicit val LipidMoleculePercentFormat = Json.format[LipidMoleculePercent]
}