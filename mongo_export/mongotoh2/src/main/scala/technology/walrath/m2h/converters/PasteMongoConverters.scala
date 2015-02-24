package technology.walrath.m2h.converters

import com.mongodb.DBObject
import technology.walrath.m2h.mongo.models.PasteTO
import org.bson.types.ObjectId
import com.mongodb.casbah.Imports._

/**
 * Mongo BSON mapping utility for Pastes.
 */
object PasteMongoConverters {
  /**
   * A blanket exception for a missing field.
   */
  def mongoFail = throw new MongoException("Field not found.")

  /**
   * Converts a mongodb object to a PasteTO.
   */
  def convertFromMongoObject(db: DBObject): PasteTO = {
    if(db == null) {
      return null
    }
    val _id: ObjectId = db.getAsOrElse[ObjectId]("_id", mongoFail)
    val pasteId: String = db.getAsOrElse[String]("pasteId", mongoFail)
    val owner: ObjectId = db.getAsOrElse[ObjectId]("owner", mongoFail)
    val title: String = db.getAsOrElse[String]("title", mongoFail)
    val content: String = db.getAsOrElse[String]("content", mongoFail)
    val isPrivate: Boolean = db.getAsOrElse[Boolean]("isPrivate", mongoFail)

    PasteTO(
      _id = _id,
      pasteId = pasteId,
      owner = owner,
      title = title,
      content = content,
      isPrivate = isPrivate
    )
  }
}