package models

import play.api.libs.json._

case class Report(id: Int, lipidMolec: String, fa: String, faGroupKey: String, calcMass: Double, formula: String, baseRt: Double, mainIon: String, mainAreaC: String, lipidClassId: Int, organId: Int)

object Report{
  implicit val ReportFormat = Json.format[Report]
}