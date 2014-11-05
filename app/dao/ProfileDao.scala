package dao

import com.mongodb.casbah.Imports._
import java.security.MessageDigest
import models.ProfileTO
import converters.ProfileMongoConverters
import org.mindrot.jbcrypt.BCrypt

object ProfileDao {
  
	var mongodbName: String = "lecarton"
	var profileCollectionName: String = "profiles"
  var anonUserId = new ObjectId("54485f901adee7b53870bacb")
	
	/**
	 * Creates a brand new profile.
	 */
    def createUserProfile(username: String, password: String, email: String): MongoDBObject = {
        val mongoConnection = MongoConnection()
        val collection = mongoConnection(mongodbName)(profileCollectionName)
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
      val mongoConnection = MongoConnection()
      val collection = mongoConnection(mongodbName)(profileCollectionName)
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
      val mongoConnection = MongoConnection()
      val collection = mongoConnection(mongodbName)(profileCollectionName)
      ProfileMongoConverters.convertFromMongoObject(
        collection.findOne(query) match {
          case Some(value) => value
          case None => null
        }
      )

    }

  /**
   * The base query for multiple profiles
   * @param query Query for multiple objects
   * @return The Pastes found or an empty list.
   */
    def queryMultipleProfilesBase(query: MongoDBObject): List[ProfileTO] = {
      val mongoConnection = MongoConnection()
      val collection = mongoConnection(mongodbName)(profileCollectionName)
      collection.find(query).map(x => ProfileMongoConverters.convertFromMongoObject(x)).toList
    }
    
    /**
     * A blanket exception to be thrown.
     */
    def mongoFail = throw new MongoException("Document not found")
}