package dao

import com.mongodb.casbah.Imports._
import converters.PasteMongoConverters
import models.{ProfileTO, PasteTO}
import org.bson.types.ObjectId
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.Set

class ProfileDaoTest extends FlatSpec with Matchers {

	"ProfileDao" should "be able to create a new profile" in {
		setUp
		val originalDocCount = getDocumentCount
		val newObject = insertSampleDocument
		val newId = newObject.getAs[ObjectId]("_id").toString()
		assert(originalDocCount < getDocumentCount)
		assert(newId != null)
		tearDown
	}

  it should "be able to create a new profile with selected id" in {
    setUp
    val originalDocCount = getDocumentCount
    val randVal = new ObjectId()
    val newObject = insertSampleDocumentWithForcedId(randVal.toString)
    val newId = newObject.getAs[ObjectId]("_id").get
    assert(newId == randVal)
    tearDown
  }

	it should "be able to retrieve user profile by _id" in {
		setUp
		insertSampleDocument
    val randVal = new ObjectId()
    insertSampleDocumentWithForcedId(randVal.toString)
    val searchId = randVal
		val query = ProfileTO(_id = searchId, null, null, null)
		val results = ProfileDao.queryUserProfileById(query)
		results should not be null
    results._id.toString should equal (searchId.toString)
		tearDown
	}

  it should "fail to retrieve user profile by _id if profile doesn't exist" in {
    setUp
    val searchId = new ObjectId("544123901adee7b53870bacb")
    val query = ProfileTO(_id = searchId, null, null, null)
    val results = ProfileDao.queryUserProfileById(query)
    results should be (null)
    tearDown
  }

  it should "be able to retrieve user profile by username" in {
    setUp
    insertSampleDocument
    val randVal = new ObjectId()
    insertSampleDocumentWithForcedId(randVal.toString)
    val query = ProfileTO(null, "AAAA", null, null)
    val results = ProfileDao.queryUserProfileByUsername(query)
    results should not be null
    results.username should equal ("AAAA")
    tearDown
  }

  it should "fail to retrieve user profile by username if there isn't an entry" in {
    setUp
    val query = ProfileTO(null, "AAAA", null, null)
    val results = ProfileDao.queryUserProfileByUsername(query)
    results should be (null)
    tearDown
  }

  "ProfileConverter" should "throw Mongo Exception if a field is missing" in {
    setUp
    insertDocumentWithMissingField
    val query = ProfileTO(null, "asdf", null, null)
    intercept[MongoException] {
      ProfileDao.queryUserProfileByUsername(query)
    }
    tearDown
  }

  def setUp = {
		ProfileDao.mongodbName = "lecartontest"
		selfDestructButton
	}
	
	def tearDown = {
		selfDestructButton
	}

  def insertDocumentWithMissingField: MongoDBObject = {
    val mongoConnection = MongoConnection()
    val collection = mongoConnection(ProfileDao.mongodbName)(ProfileDao.profileCollectionName)
    val newObject = MongoDBObject(
      "username" -> "asdf",
      "email" -> "asdf"
    )
    collection += newObject
    return newObject
  }

	def insertSampleDocument = ProfileDao.createUserProfile("AAAA", "hurtzip", "sample@email.com")
  def insertSampleDocument2 = ProfileDao.createUserProfile("AAAA", "hurtzip", "sample@gmail.com")
  def insertSampleDocumentWithForcedId(id: String) = ProfileDao.createUserProfile("AAAB", "hurtzip", "sample@email.com", new ObjectId(id))
  def getDocumentCount = MongoConnection()(ProfileDao.mongodbName)(ProfileDao.profileCollectionName).count(MongoDBObject.empty)
  def selfDestructButton = MongoConnection()(ProfileDao.mongodbName)(ProfileDao.profileCollectionName).remove(MongoDBObject.empty)
}