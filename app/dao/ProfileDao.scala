package dao

import com.mongodb.casbah.Imports._
import java.security.MessageDigest
import com.mongodb.casbah.MongoURI
import models.ProfileTO
import converters.ProfileMongoConverters
import org.mindrot.jbcrypt.BCrypt
import play.api.Play

class ProfileDao {
  
	var mongodbName: String = "lecarton"
	var profileCollectionName: String = "profiles"
  var anonUserId = new ObjectId("54485f901adee7b53870bacb")

  def getConnection = {
    val mongoUri = MongoClientURI(Play.current.configuration.getString("mongo.url").get)
    MongoClient(mongoUri)(mongoUri.database.get)
  }

	/**
	 * Creates a brand new profile.
	 */
    def createUserProfile(username: String, password: String, email: String): MongoDBObject = {
        val mongoDb = getConnection
        val collection = mongoDb(profileCollectionName)
        val newObject = MongoDBObject(
            "username" -> username,
            "password" -> BCrypt.hashpw(password, BCrypt.gensalt(4)),
            "email" -> email
        )
        collection += newObject
        return newObject
    }

  /**
   * Creates a brand new profile while forcing the objectId.
   */
    def createUserProfile(username: String, password: String, email: String, forcedId: ObjectId): MongoDBObject = {
      val mongoDb = getConnection
      val collection = mongoDb(profileCollectionName)
      val newObject = MongoDBObject(
        "_id" -> forcedId,
        "username" -> username,
        "password" -> BCrypt.hashpw(password, BCrypt.gensalt(4)),
        "email" -> email
      )
      collection += newObject
      return newObject
    }

	/**
	 * Retrieves a profile using the username.
	 */
    def queryUserProfileByUsername(profileTO: ProfileTO): ProfileTO = {
    	val query = MongoDBObject("username" -> profileTO.username)
    	querySingleProfileBase(query)
    }

  /**
   * Retrieves a profile by _id.
   */
  def queryUserProfileById(profileTO: ProfileTO): ProfileTO = {
    val query = MongoDBObject("_id" -> profileTO._id)
    querySingleProfileBase(query)
  }

  /**
   *  The base query for a single profile.
   * @param query Query for a single object.
   * @return The paste found or null.
   */
    def querySingleProfileBase(query: MongoDBObject): ProfileTO = {
      val mongoDb = getConnection
      val collection = mongoDb(profileCollectionName)
      ProfileMongoConverters.convertFromMongoObject(
        collection.findOne(query) match {
          case Some(value) => value
          case None => null
        }
      )
    }
}