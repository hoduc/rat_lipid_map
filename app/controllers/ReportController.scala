package controllers

import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import play.api.libs.json._
import models._
import dal._

import scala.concurrent.{ ExecutionContext, Future }

import javax.inject._

import java.net.URLEncoder
import java.net.URLDecoder

class ReportController @Inject() (repo: ReportRepository, val messagesApi: MessagesApi)
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport{

  def ratLipid() = Action{ implicit request =>
      println("home-page")
      Ok(views.html.rat_lipid.render())
  }
  
  
  def getLipidsPercentByOrgan(org:String) = Action.async{
      println("get lipids percents:" + org)
      repo.lipidsPercentByOrgan(org).map{
	lipidReports =>
         Ok(Json.toJson(
		lipidReports.map{
		   case (lipid,organ,percentage) => Json.obj(
		      "percent" -> percentage.percent,
		      "lipidClass" -> lipid.name,
		      "organ"	   -> organ.name
		   )
		}
	 ))
      }
  }

  def getLipidsPercentByOrganCanvasJs(org:String) = Action.async{
      println("get lipids percents:" + org)
      repo.lipidsPercentByOrgan(org).map{
	lipidReports =>
         Ok(Json.toJson(
		lipidReports.map{
		   case (lipid,organ,percentage) => Json.obj(
		      "y" -> percentage.percent.toString,
		      "label" -> lipid.name,
		      "name" -> lipid.name,
		      "organ" -> organ.name
		   )
		}
	 ))
      }
  }
  
  def att(att:String, value:String): String = {
     att + "=" + "\"" + value + "\""
  }
  
  def tag(name:String, value:String, attrs:String*): String = {
     var s = ""
     for(attr <- attrs){
        s += " " + attr
     }
     "<" + name + " " + s + ">" + value + "</" + name + ">"
  }

  //generate anchor with onclick
  def anchor(href:String, value:String): String = {
      tag("a",value, att("href",href), att("onclick", "event.preventDefault();clickRelation($(this));"))
  }

  def dynatableFormat(lipidClass:LipidClass,lipidMolecule:LipidMolecule,lipidMoleculePercent:LipidMoleculePercent,organ:Organ): JsObject = {
     Json.obj(
	              "lipidMolec" -> anchor("/percents/lm/" + lipidMolecule.lipidMolec,lipidMolecule.lipidMolec),
		      "fa" -> lipidMolecule.fa,
		      "faGroupKey" -> lipidMolecule.faGroupKey,
		      "calcMass"   -> lipidMolecule.calcMass.toString,
		      "formula"	  -> lipidMolecule.formula,
		      "baseRt"	  -> lipidMoleculePercent.baseRt.toString,
		      "mainIon"	  -> lipidMolecule.mainIon,
		      "mainAreaC"  -> lipidMoleculePercent.mainAreaC,
		      "percent" -> lipidMoleculePercent.percent,
		      "lipidClass" -> anchor("/percents/lc/" + lipidClass.name, lipidClass.name),
		      "organ"	   -> organ.name
    )
  }
  def getLipids(lipidClass:String) = Action.async{
      println("get lipids Given Lipid Class:" + lipidClass)
      repo.lipids(lipidClass).map{ results =>
         Ok(Json.toJson( results.map{
	       case(lipidClass,lipidMolecule,lipidMoleculePercent,organ) => dynatableFormat(lipidClass,lipidMolecule,lipidMoleculePercent,organ)
            }
         ))							      
      }
  }
  
  def getLipidClassOrganPercent(lipidClass:String) = Action.async{
      println("get lipid class organ percent given lipid class:" + lipidClass)
      repo.lipidClassOrganPercent(lipidClass).map{ results =>
         Ok(Json.toJson( results.map{
	       case(lipidClass,lipidClassPercent,organ) => Json.obj(
		  "organ"      -> organ.name,
		  "y"    -> lipidClassPercent.percent,
		  "label" -> organ.name
	       )
	    }
	 ))
      }
  }

  def getLipidMoleculeOrganPercent(lipidMolecule:String) = Action.async{
      println("get lipid molecule  organ percent given lipid class:" + lipidMolecule)
      repo.lipidMoleculeOrganPercent(lipidMolecule).map{ results =>
         Ok(Json.toJson( results.map{
	       case(lipidMolecule,lipidMoleculePercent,organ) => Json.obj(
	          "lipidMolec" -> lipidMolecule.lipidMolec,
		  "label"      -> organ.name,
		  "y"    -> lipidMoleculePercent.percent
	       )
	    }
	 ))
      }
  }

  def getLipidMolecules(lipidClass:String, organ:String) = Action.async{
      println("get lipidMolecules :" + (lipidClass,organ))
      repo.lipidMolecules(lipidClass,organ).map{ results =>
         Ok(Json.toJson( results.map{
	       case(lipidClass,lipidMolecule, lipidMoleculePercent, organ) => dynatableFormat(lipidClass, lipidMolecule, lipidMoleculePercent, organ)
	    }
	 ))
      }
  }

  def getLipidMolecules1(lipidClass:String) = Action.async{
      println("get lipidMolecules1 :" + URLDecoder.decode(lipidClass,"UTF-8"))
      repo.lipidMolecules1(URLDecoder.decode(lipidClass,"UTF-8")).map{ results =>
         Ok(Json.toJson( results.map{
	       case(lipidClass,lipidMolecule, lipidMoleculePercent, organ) => dynatableFormat(lipidClass, lipidMolecule, lipidMoleculePercent, organ)
	    }
	 ))
      }
  }

   def getLipidMolecules2(lipidMolecule:String) = Action.async{
      println("get lipidMolecules2 :" + URLDecoder.decode(lipidMolecule,"UTF-8"))
      repo.lipidMolecules2(URLDecoder.decode(lipidMolecule,"UTF-8")).map{ results =>
         Ok(Json.toJson( results.map{
	       case(lipidClass,lipidMolecule, lipidMoleculePercent, organ) => dynatableFormat(lipidClass, lipidMolecule, lipidMoleculePercent, organ)
	    }
	 ))
      }
  }

  def autoCompleteLipidClassLipidMolecule(q:String) = Action.async{
      println("autocomplete:lipidclass+lipidMolecule:" + q)
      repo.lipidClassOrLipidMoleculeStartsWith(q).map{ results =>
         Ok( results match{
                case (lipidClass, lipidMolecByOrganWithPercent) =>
		   Json.toJson((lipidClass ++ lipidMolecByOrganWithPercent).map{ result =>
		      result match{
                         case x: LipidClass => Json.obj(
						   "name" -> x.name,
					       	   "value" -> x.name,
					    	   "type" -> 0
   		   	   	      	 	)
 			 case (lm: LipidMolecule, lmp: LipidMoleculePercent, o: Organ) => Json.obj(
			      	 		   "name" -> lm.lipidMolec,
						   "value" -> lm.lipidMolec,
						   "type" -> 1
			      	 		)
			case _ => Json.obj("name" -> "empty")
		      }
		   })
         })	      
     }
  }
}
