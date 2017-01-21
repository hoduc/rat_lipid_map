package models

import play.api.libs.json._

case class LipidMoleculeOrgan(id: Int, baseRt: Double, mainAreaC: String, lipidMolecId: Int, organId: Int)

object LipidMoleculeOrgan{
  implicit val LipidMoleculeOrganFormat = Json.format[LipidMoleculeOrgan]
}