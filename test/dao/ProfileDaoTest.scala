package dao

import com.mongodb.casbah.Imports._
import models.ProfileTO
import org.bson.types.ObjectId
import org.scalatest._

class ProfileDaoTest extends FunSpec with Matchers with BeforeAndAfter {

  var profileDao: ProfileDao = new ProfileDao

  before {
    profileDao.mongodbName = "lecartontest"
    selfDestructButton
  }

  after {
    selfDestructButton
  }

  describe("ProfileDao") {
    it("should be able to create a new profile") {
      profileDao.mongodbName = "lecartontest"
      selfDestructButton
      val originalDocCount = getDocumentCount
      val newObject = insertSampleDocument
      val newId = newObject.getAs[ObjectId]("_id").toString()
      assert(originalDocCount < getDocumentCount)
      assert(newId != null)
    }

    it("should be able to create a new profile with selected id") {
      val originalDocCount = getDocumentCount
      val randVal = new ObjectId()
      val newObject = insertSampleDocumentWithForcedId(randVal.toString)
      val newId = newObject.getAs[ObjectId]("_id").get
      assert(newId == randVal)
    }

    it("should be able to retrieve user profile by _id") {
      insertSampleDocument
      val randVal = new ObjectId()
      insertSampleDocumentWithForcedId(randVal.toString)
      val searchId = randVal
      val query = ProfileTO(_id = searchId, null, null, null)
      val results = profileDao.queryUserProfileById(query)
      results should not be null
      results._id.toString should equal(searchId.toString)
    }

    it("should fail to retrieve user profile by _id if profile doesn't exist") {
      val searchId = new ObjectId("544123901adee7b53870bacb")
      val query = ProfileTO(_id = searchId, null, null, null)
      val results = profileDao.queryUserProfileById(query)
      results should be(null)
    }

    it("should be able to retrieve user profile by username") {
      insertSampleDocument
      val randVal = new ObjectId()
      insertSampleDocumentWithForcedId(randVal.toString)
      val query = ProfileTO(null, "AAAA", null, null)
      val results = profileDao.queryUserProfileByUsername(query)
      results should not be null
      results.username should equal("AAAA")
    }

    it("should fail to retrieve user profile by username if there isn't an entry") {
      val query = ProfileTO(null, "AAAA", null, null)
      val results = profileDao.queryUserProfileByUsername(query)
      results should be(null)
    }
  }

  describe("ProfileConverter") {
    it("should throw Mongo Exception if a field is missing") {
      insertDocumentWithMissingField
      val query = ProfileTO(null, "asdf", null, null)
      intercept[MongoException] {
        profileDao.queryUserProfileByUsername(query)
      }
    }
  }

  def insertDocumentWithMissingField: MongoDBObject = {
    val mongoConnection = MongoConnection()
    val collection = mongoConnection(profileDao.mongodbName)(profileDao.profileCollectionName)
    val newObject = MongoDBObject(
      "username" -> "asdf",
      "email" -> "asdf"
    )
    collection += newObject
    return newObject
  }

	def insertSampleDocument = profileDao.createUserProfile("AAAA", "hurtzip", "sample@email.com")
  def insertSampleDocument2 = profileDao.createUserProfile("AAAA", "hurtzip", "sample@gmail.com")
  def insertSampleDocumentWithForcedId(id: String) = profileDao.createUserProfile("AAAB", "hurtzip", "sample@email.com", new ObjectId(id))
  def getDocumentCount = MongoConnection()(profileDao.mongodbName)(profileDao.profileCollectionName).count(MongoDBObject.empty)
  def selfDestructButton = MongoConnection()(profileDao.mongodbName)(profileDao.profileCollectionName).remove(MongoDBObject.empty)
}