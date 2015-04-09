package models

import play.api.Play.current
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import scala.util.Try

/**
 * Base Model for reference.
 */
trait Model

/**
 * Collection of freebie queries.
 */
trait BaseSlickTrait[E <: Model] {

  /**
   * The table and connection to base off.
   */
  def model: TableQuery[_ <: Table[E]]

  /**
   * Selects all the rows from the current table.
   * @return Retrieves all rows as if 'SELECT *'
   */
  def all = DB withSession { implicit session => model.list }

  /**
   * Gets the count of the current table.
   * @return The count as if 'SELECT COUNT(*)'
   */
  def size = DB withSession { implicit session => model.length.run }

  /**
   * Adds item to the collection.
   * @param e The new entity to add to the table.
   * @return by default returns the number of rows inserted.
   */
  def +=(e: E): Try[Int] = Try { DB withSession { implicit session => model += e } }

  /**
   * Add items to the collection in batch.
   * @param e The entities to add.
   * @return By default returns the number of rows inserted.
   */
  def ++=(e: Seq[E]): Try[Option[Int]] = Try { DB withSession { implicit session => model ++= e }}
}

object TableQueries {
  val profiles = TableQuery[Profiles]
  val pastes  = TableQuery[Pastes]
}