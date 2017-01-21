package models

import play.api.libs.json._

case class LipidMoleculePercent(id: Int, percent: Double, lipidMolecId: Int, organId: Int)

object LipidMoleculePercent{
  implicit val LipidMoleculePercentFormat = Json.format[LipidMoleculePercent]
}