package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.LipidClass
import models.Organ
import models.Percentage
import models.LipidMolecule
import models.LipidMoleculeOrgan
import models.LipidClassPercent
import models.LipidMoleculePercent

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

  class LipidMolecTable(tag: Tag) extends Table[LipidMolecule](tag, "lipid_molecule") {

    def id = column[Int]("id",O.PrimaryKey, O.AutoInc)
    def lipidMolec = column[String]("lipid_molec")
    def fa = column[String]("fa")
    def faGroupKey = column[String]("fa_group_key")
    def calcMass = column[Double]("calc_mass")
    def formula = column[String]("formula")
    def mainIon = column[String]("main_ion")
    def lipidClassId = column[Int]("lipid_class_id")

    def lipidClass = foreignKey("lipid_class_fk", lipidClassId, lipids)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, lipidMolec, fa, faGroupKey, calcMass, formula, mainIon, lipidClassId) <> ((LipidMolecule.apply _).tupled, LipidMolecule.unapply)
  }
  

  class LipidMolecOrganTable(tag: Tag) extends Table[LipidMoleculeOrgan](tag, "lipid_molecule_organ") {

    def id = column[Int]("id",O.PrimaryKey, O.AutoInc)    
    def baseRt = column[Double]("base_rt")
    def mainAreaC = column[String]("main_area_c")
    def lipidMolecId = column[Int]("lipid_molec_id")
    def organId = column[Int]("organ_id")
    
    def lipidMolec = foreignKey("lipid_molec_fk", lipidMolecId, lipidMolecs)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def organ = foreignKey("organ_fk", organId, organs)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, baseRt, mainAreaC,lipidMolecId, organId) <> ((LipidMoleculeOrgan.apply _).tupled, LipidMoleculeOrgan.unapply)
  }


  class LipidMolecPercentTable(tag: Tag) extends Table[LipidMoleculePercent](tag, "lipid_molecule_percent") {

    def id = column[Int]("id",O.PrimaryKey, O.AutoInc)    
    def percent = column[Double]("percent")
    def lipidMolecId = column[Int]("lipid_molec_id")
    def organId = column[Int]("organ_id")
    
    def lipidMolec = foreignKey("lipid_molec_fk", lipidMolecId, lipidMolecs)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def organ = foreignKey("organ_fk", organId, organs)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, percent, lipidMolecId, organId) <> ((LipidMoleculePercent.apply _).tupled, LipidMoleculePercent.unapply)
  }
  
  class LipidClassPercentTable(tag: Tag) extends Table[LipidClassPercent](tag, "lipid_class_percent"){
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def lipidClassId = column[Int]("lipid_class_id")
    def organId = column[Int]("organ_id")
    def percent = column[Double]("percent")

    def lipidClass = foreignKey("lipid_class_fk", lipidClassId, lipids)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def organ = foreignKey("organ_fk", organId, organs)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (id, lipidClassId, organId, percent) <> ((LipidClassPercent.apply _).tupled, LipidClassPercent.unapply)
  }
  
  val lipidMolecs = TableQuery[LipidMolecTable]
  val lipidMolecOrgans = TableQuery[LipidMolecOrganTable]
  val lipidMolecPercents = TableQuery[LipidMolecPercentTable]
  val lipidClassPercents = TableQuery[LipidClassPercentTable]
  val percents = TableQuery[PercentageTable]


  
  def lipidsPercentByOrgan(org: String): Future[Seq[(LipidClass,Organ,Percentage)]] = db.run{
    val q = for{
    	((p,o),lc) <- (percents join organs.filter(_.name === org) on (_.organId === _.id)) join lipids on (_._1.lipidClassId === _.id)
    }yield (lc,o,p)
      q.result
  }

  def lipids(lipidClass:String): Future[Seq[(LipidClass, LipidMolecule, LipidMoleculeOrgan, Organ)]] = db.run {
      val q = for{
         (((l,lm),lmo),o) <- ((lipids.filter(_.name === lipidClass) join lipidMolecs on(_.id === _.lipidClassId)) join lipidMolecOrgans on (_._2.id === _.lipidMolecId)) join organs on (_._2.organId === _.id)
      }yield(l,lm,lmo,o)
      q.result
  }

  def lipidClassOrLipidMoleculeStartsWith(q:String): Future[(Seq[LipidClass],Seq[LipidMolecule])] = db.run{
      val q1 = lipids.filter(_.name.startsWith(q))
      val q2 = lipidMolecs.filter(_.lipidMolec.startsWith(q)) 
      q1.result zip q2.result
  }


  def lipidClassOrganPercent(lipidClass:String): Future[Seq[(LipidClass,LipidClassPercent, Organ)]] = db.run{
      val q = for{
      	 ((lc,lcp),o) <- (lipids.filter(_.name === lipidClass) join lipidClassPercents on (_.id === _.lipidClassId)) join organs on ( _._2.organId === _.id)
      } yield (lc,lcp,o)
      q.result
  }

  def lipidMoleculeOrganPercent(lipidMolecule:String, organ:String): Future[Seq[(LipidMolecule, LipidMoleculePercent, Organ)]] = db.run{
      val q = for{
      	  ((lm,lmp),o) <- (lipidMolecs.filter(_.lipidMolec === lipidMolecule) join lipidMolecPercents on (_.id === _.lipidMolecId)) join organs on (_._2.organId === _.id)
      } yield (lm,lmp,o)
      q.result
  }
  
  
  def lipidMolecules(lipidClass:String, organ:String): Future[Seq[(LipidClass,LipidMolecule, LipidMoleculeOrgan, Organ)]] = db.run{
     val q = for{
     	 (((lc,lm),lmo),o) <- ((lipids.filter(_.name === lipidClass) join lipidMolecs on (_.id === _.lipidClassId)) join lipidMolecOrgans on (_._2.id === _.lipidMolecId)) join organs.filter(_.name === organ) on (_._2.organId === _.id)
     } yield (lc,lm,lmo,o)

     q.result
  }
  

  def lipidMolecules1(lipidClass:String): Future[Seq[(LipidClass,LipidMolecule, LipidMoleculeOrgan, Organ)]] = db.run{
       val q = for{
     	 (((lc,lm),lmo),o) <- ((lipids.filter(_.name === lipidClass) join lipidMolecs on (_.id === _.lipidClassId)) join lipidMolecOrgans on (_._2.id === _.lipidMolecId)) join organs on (_._2.organId === _.id)
     } yield (lc,lm,lmo,o)

     q.result
  }

  
  def lipidMolecules2(lipidMolecule:String): Future[Seq[(LipidClass,LipidMolecule, LipidMoleculeOrgan, Organ)]] = db.run{
       val q = for{
     	 (((lc,lm),lmo),o) <- ((lipids join lipidMolecs.filter(_.lipidMolec === lipidMolecule)  on (_.id === _.lipidClassId)) join lipidMolecOrgans on (_._2.id === _.lipidMolecId)) join organs on (_._2.organId === _.id)
     } yield (lc,lm,lmo,o)

     q.result
  } 
}
