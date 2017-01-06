package models

import play.api.libs.json._

case class Organ(id: Int, name: String)

object Organ{
  implicit val OrganFormat = Json.format[Organ]
}