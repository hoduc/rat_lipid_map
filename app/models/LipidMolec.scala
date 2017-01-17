package models

import play.api.libs.json._

case class LipidMolec(id: Int, lipidMolec: String, fa: String, faGroupKey: String, calcMass: Double, formula: String, mainIon: String)

object LipidMolec{
  implicit val LipidMolecFormat = Json.format[LipidMolec]
}