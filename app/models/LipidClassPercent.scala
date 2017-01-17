package models

import play.api.libs.json._

case class LipidClassPercent(id: Int, lipidClassId: Int, organId: Int, percent: Double)

object LipidClassPercent{
  implicit val LipidClassPercent = Json.format[LipidClassPercent]
}