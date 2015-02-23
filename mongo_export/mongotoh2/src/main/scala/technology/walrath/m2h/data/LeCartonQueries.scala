package technology.walrath.m2h.data

import com.mongodb.casbah.Imports._
import technology.walrath.m2h.mongo.models.PasteTO
import technology.walrath.m2h.mongo.models.ProfileTO
import technology.walrath.m2h.converters._


/**
 * Created by justin on 2/22/15.
 */
class LeCartonQueries {
  var mongodbName: String = "lecarton"

  def queryPastesOfOwner(pasteTO: PasteTO): List[PasteTO] = {
    val query = MongoDBObject("owner" -> pasteTO.owner, "isPrivate" -> pasteTO.isPrivate)
    val mongoConnection = MongoConnection()
    val collection = mongoConnection(mongodbName)("pastes")
    collection.find(query).map(x => PasteMongoConverters.convertFromMongoObject(x)).toList
  }
  
  def queryOwners(): List[ProfileTO] = {
    val mongoConnection = MongoConnection()
    val collection = mongoConnection(mongodbName)("profiles")
    collection.find.map(x => ProfileMongoConverters.convertFromMongoObject(x)).toList
  }
}
