package technology.walrath.m2h.converters

import com.mongodb.DBObject
import technology.walrath.m2h.mongo.models.ProfileTO
import org.bson.types.ObjectId
import com.mongodb.casbah.Imports._

/**
 * Mongo BSON mapping utility for profiles.
 */
object ProfileMongoConverters {
  /**
   * A blanket exception for a missing field.
   */
  def mongoFail = throw new MongoException("Field not found.")

  /**
   * Converts a mongodb object to a ProfileTO.
   */
  def convertFromMongoObject(db: DBObject): ProfileTO = {
    if(db == null) {
      return null
    }
    val _id: ObjectId = db.getAsOrElse[ObjectId]("_id", mongoFail)
    val username: String = db.getAsOrElse[String]("username", mongoFail)
    val password: String = db.getAsOrElse[String]("password", mongoFail)
    val email: String = db.getAsOrElse[String]("email", mongoFail)

    ProfileTO(
      _id = _id,
      username = username,
      password = password,
      email = email
    )
  }
}