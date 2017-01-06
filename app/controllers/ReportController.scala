package controllers

import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
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

  def getReportsDyna2(lipidClass:String, organ:String) = Action.async{
      repo.lipidsByLipidClassAndOrgan(lipidClass,organ).map{lipidReports =>
         Ok(Json.toJson(
		lipidReports.map{
		   case (lipid,organ,report) => Json.obj(
		      "lipidMolec" -> report.lipidMolec,
		      "fa" -> report.fa,
		      "faGroupKey" -> report.faGroupKey,
		      "calcMass"   -> report.calcMass.toString,
		      "formula"	  -> report.formula,
		      "baseRt"	  -> report.baseRt.toString,
		      "mainIon"	  -> report.mainIon,
		      "mainAreaC"  -> report.mainAreaC,
		      "lipidClass" -> lipid.name,
		      "organ"	   -> organ.name
		   )
		}
	 ))
      
      }
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
  
  def getReportsLipidDyna(lipidClass:String) = Action.async{
      println("get lipid dyna:" + lipidClass)
      repo.lipidsWithLipidClass(lipidClass).map{ lipidReports =>
         Ok(Json.toJson(
		lipidReports.map{
		   case (lipid,organ,report) => Json.obj(
		      "lipidMolec" -> report.lipidMolec,
		      "fa" -> report.fa,
		      "faGroupKey" -> report.faGroupKey,
		      "calcMass"   -> report.calcMass.toString,
		      "formula"	  -> report.formula,
		      "baseRt"	  -> report.baseRt.toString,
		      "mainIon"	  -> report.mainIon,
		      "mainAreaC"  -> report.mainAreaC,
		      "lipidClass" -> lipid.name,
		      "organ"	   -> organ.name
		   )
		}
	 ))
      }
  }

  def getReportsLipidMolec(lipidMolec:String) = Action.async{
     println("get lipid molec dyna:" + lipidMolec)
      repo.lipidsWithLipidMolec(URLDecoder.decode(lipidMolec,"UTF-8")).map{ lipidReports =>
         Ok(Json.toJson(
		lipidReports.map{
		   case (lipid,organ, report) => Json.obj(
		      "lipidMolec" -> report.lipidMolec,
		      "fa" -> report.fa,
		      "faGroupKey" -> report.faGroupKey,
		      "calcMass"   -> report.calcMass.toString,
		      "formula"	  -> report.formula,
		      "baseRt"	  -> report.baseRt.toString,
		      "mainIon"	  -> report.mainIon,
		      "mainAreaC"  -> report.mainAreaC,
		      "lipidClass" -> lipid.map(x => x.name),
		      "organ"	   -> organ.map(x => x.name)
		   )
		}
	 ))
      } 
  }
  

  def getReports = Action.async{
     repo.list().map{ reports =>
        Ok(Json.toJson(reports))
     }
     
  }

  def getLipids = Action.async {
      repo.lipidClassList().map{ lipid =>
        Ok(Json.toJson( lipid ) )
      }
  }

  def getOrgans = Action.async {
      repo.organList().map{ organ =>
        Ok(Json.toJson(organ))
      }
  }

  def getReportsWithOrgan(org:String) = Action.async {
      repo.lipidsWithOrgan(org).map{ report =>
        Ok(Json.toJson(report))
      }
  }

  
  def autoCompleteLipidClass(q:String) = Action.async{
      println("autocomplete:lipidClass:q="+q)
      repo.lipidStartsWith(q).map{ lipids =>
        Ok(Json.toJson(
		lipids.map{ l => Json.obj(
	              "label" -> l.name,
		      "value" -> l.name
		   )
	           
		}
	))
      }
  }

  def autoCompleteLipidMolec(q:String) = Action.async{
      println("autocomplete:lipidMolec:value:" + q)
      repo.lipidMolecStartsWith(q).map{ lipids =>
        Ok(Json.toJson(
		lipids.map{ l => Json.obj(
		      "value" -> l.lipidMolec
		   )
	           
		}
	))
      }
  }

  def autoCompleteLipidClassLipidMolec(q:String) = Action.async{
      println("autocomplete:lipidClass+lipidMolec:" + q)
      repo.lipidClassOrLipidMolecStartsWith(q).map{ results =>
            Ok(results match{
                  case (lipidClass, report) => 
		     Json.toJson((lipidClass ++ report).map{ result =>
                           result match{
		              case x: LipidClass => Json.obj(
			                              "name" -> x.name,
						      "value" -> x.name,
						      "type" -> 0
                                                    )
			      case x: Report => Json.obj(
                                                   "name" -> x.lipidMolec,
						   "value" -> x.lipidMolec,
						   "type" -> 1
 						)
		           }
		        }
		     )
	       }
	   )
      }
  }
 
}
