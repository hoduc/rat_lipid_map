package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.LipidClass
import models.Organ
import models.Report
import models.Percentage

import scala.concurrent.{ Future, ExecutionContext }



/**
 * A repository for Lipid Report.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class ReportRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
   * Tables
   */

  class LipidClassTable(tag: Tag) extends Table[LipidClass](tag, "lipid_class") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Int]("id",O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")
    
    def * = (id, name) <> ((LipidClass.apply _).tupled, LipidClass.unapply)
  }


  class OrganTable(tag: Tag) extends Table[Organ](tag, "organ") {

    /** The ID column, which is the primary key, and auto incremented */
    def id = column[Int]("id",O.PrimaryKey, O.AutoInc)

    /** The name column */
    def name = column[String]("name")
    
    def * = (id, name) <> ((Organ.apply _).tupled, Organ.unapply)
  }
  
  val lipids = TableQuery[LipidClassTable]
  val organs = TableQuery[OrganTable]

  class PercentageTable(tag: Tag) extends Table[Percentage](tag, "percentage"){
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def lipidClassId = column[Int]("lipid_class_id")
    def organId = column[Int]("organ_id")
    def numSpecies = column[Int]("n_species")
    def percent = column[Double]("percent")

    def lipidClass = foreignKey("lipid_class_fk", lipidClassId, lipids)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def organ = foreignKey("organ_fk", organId, organs)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, lipidClassId, organId, numSpecies, percent) <> ((Percentage.apply _).tupled, Percentage.unapply)
  }
  
  class ReportTable(tag: Tag) extends Table[Report](tag, "report") {

    def id = column[Int]("id",O.PrimaryKey, O.AutoInc)
    def lipidMolec = column[String]("lipid_molec")
    def fa = column[String]("fa")
    def faGroupKey = column[String]("fa_group_key")
    def calcMass = column[Double]("calc_mass")
    def formula = column[String]("formula")
    def baseRt = column[Double]("base_rt")
    def mainIon = column[String]("main_ion")
    def mainAreaC = column[String]("main_area_c")
    def lipidClassId = column[Int]("lipid_class_id")
    def organId = column[Int]("organ_id")
    
    def lipidClass = foreignKey("lipid_class_fk", lipidClassId, lipids)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def organ = foreignKey("organ_fk", organId, organs)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, lipidMolec, fa, faGroupKey, calcMass, formula, baseRt, mainIon, mainAreaC, lipidClassId, organId) <> ((Report.apply _).tupled, Report.unapply)
  }

  val percents = TableQuery[PercentageTable]
  val reports = TableQuery[ReportTable]
  
  /**
   * List all the reports in the data
   base.
   */
  def list(): Future[Seq[Report]] = db.run {
    reports.result
  }

  def lipidClassList(): Future[Seq[LipidClass]] = db.run {
    lipids.result
  }
  def organList(): Future[Seq[Organ]] = db.run {
    organs.result
  }

  
  def lipidsPercentByOrgan(org: String): Future[Seq[(LipidClass,Organ,Percentage)]] = db.run{
    val q = for{
    	((p,o),lc) <- (percents join organs.filter(_.name === org) on (_.organId === _.id)) join lipids on (_._1.lipidClassId === _.id)
    }yield (lc,o,p)
      q.result
  }
  
  def lipidsByLipidClassAndOrgan(lipidClass:String, organ:String): Future[Seq[(LipidClass,Organ,Report)]] = db.run{
      val q = for{
      	  ((r,o),lc) <- (reports join organs.filter(_.name === organ) on (_.organId === _.id)) join lipids.filter(_.name === lipidClass) on (_._1.lipidClassId === _.id)
      }yield (lc,o,r)
      q.result
  }

  def lipidsWithOrgan(org: String): Future[Seq[Report]] = db.run {
      println("in report w org=" + org)
      val q = for {
      	  (o, r) <- organs.filter(_.name === org) join reports on (_.id === _.organId)
      } yield (r)
      q.result
  }

  

  def lipidsWithLipidClass(lipidClass:String): Future[Seq[(LipidClass,Organ,Report)]] = db.run {
      val q = for{
      	((r,lc),o) <- (reports join lipids.filter(_.name === lipidClass) on (_.lipidClassId === _.id)) join organs on (_._1.organId === _.id)	 
      } yield (lc,o,r)
      q.result
  }

  def lipidsWithLipidMolec(lipidMolec:String): Future[Seq[(Option[LipidClass],Option[Organ],Report)]] = db.run {
      val q = for{
      	((r,lc),o) <- (reports.filter(_.lipidMolec === lipidMolec ) joinLeft lipids on (_.lipidClassId === _.id)) joinLeft organs on (_._1.organId === _.id)
      } yield (lc,o,r)
      q.result
  }

  def lipidStartsWith(q:String): Future[Seq[LipidClass]] = db.run {
      lipids.filter(_.name.startsWith(q)).result
  }

  def lipidMolecStartsWith(q:String): Future[Seq[Report]] = db.run {
      reports.filter(_.lipidMolec.startsWith(q)).result
  }

  def lipidClassOrLipidMolecStartsWith(q:String): Future[(Seq[LipidClass],Seq[Report])] = db.run {
      val q1 = lipids.filter(_.name.startsWith(q))
      val q2 = reports.filter(_.lipidMolec.startsWith(q))
      q1.result zip q2.result
  }

}
