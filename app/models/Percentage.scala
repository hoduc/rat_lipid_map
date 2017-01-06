package models

import play.api.libs.json._

case class Percentage(id: Int, lipidClassId: Int, organId: Int, numSpecies: Int, percent: Double)

object Percentage{
  implicit val PercentageFormat = Json.format[Percentage]
}