package models

import play.api.libs.json._

case class LipidMolecule(id: Int, lipidMolec: String, fa: String, faGroupKey: String, calcMass: Double, formula: String, mainIon: String, lipidClassId: Int)

object LipidMolecule{
  implicit val LipidMoleculeFormat = Json.format[LipidMolecule]
}