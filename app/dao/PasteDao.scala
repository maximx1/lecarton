package dao

import com.mongodb.casbah.Imports._
import scala.util.Random
import models.PasteTO
import converters.PasteMongoConverters

object PasteDao {
  
	var mongodbName: String = "lecarton"
	var pasteCollectionName: String = "pastes"
	
	/**
	 * Creates a brand new paste.
	 */
    def createPaste(owner: ObjectId, title: String, message: String, isPrivate: Boolean): MongoDBObject = {
        val mongoConnection = MongoConnection()
        val collection = mongoConnection(mongodbName)(pasteCollectionName)
        val newObject = MongoDBObject(
            "pasteId" -> generateRandomString(8),
            "owner" -> owner,
            "title" -> title,
            "content" -> message,
            "isPrivate" -> isPrivate
        )
        collection += newObject
        return newObject
    }
    
	/**
	 * Retrieves a paste based on the owner's id and if it's private or not.
	 */
    def queryPastesOfOwner(pasteTO: PasteTO): List[PasteTO] = {
    	val mongoConnection = MongoConnection()
    	val collection = mongoConnection(mongodbName)(pasteCollectionName)
    	val query = MongoDBObject("owner" -> pasteTO.owner, "isPrivate" -> pasteTO.isPrivate)
    	
    	collection.find(query).map(x => PasteMongoConverters.convertFromMongoObject(x)).toList
    }
    
    /**
     * Gets one paste from the database.
     */
    def queryPasteByPasteId(pasteTO: PasteTO): PasteTO = {
		val mongoConnection = MongoConnection()
    	val collection = mongoConnection(mongodbName)(pasteCollectionName)
    	val query = MongoDBObject("pasteId" -> pasteTO.pasteId)
    	
    	PasteMongoConverters.convertFromMongoObject(
    		collection.findOne(query) match {
    		  case Some(value) => value
    		  case None => null
    		}
    	)
    }
    
    /**
     * A blanket exception to be thrown.
     */
    def mongoFail = throw new MongoException("Document not found")
    
    /**
     * Generates a sting of n length.
     */
    def generateRandomString(length: Int) = Random.alphanumeric.take(length).mkString
}