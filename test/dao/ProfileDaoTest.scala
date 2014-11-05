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
    val newObject = insertSampleDocumentWithForcedId
    val newId = newObject.getAs[ObjectId]("_id").get
    assert(originalDocCount < getDocumentCount)
    assert(newId == "54485f901adee7b53870bacb")
    tearDown
  }

	it should "be able to retrieve user profile by _id" in {
		setUp
		insertSampleDocument
    insertSampleDocumentWithForcedId
    val searchId = new ObjectId("54485f901adee7b53870bacb")
		val query = ProfileTO(_id = searchId, null, null, null)
		val results = ProfileDao.queryUserProfileById(query)
		results should not be null
    results._id.toString should equal (searchId.toString)
		tearDown
	}

  it should "fail to retrieve user profile by _id if profile doesn't exist" in {
    setUp
    val searchId = new ObjectId("54485f901adee7b53870bacb")
    val query = ProfileTO(_id = searchId, null, null, null)
    val results = ProfileDao.queryUserProfileById(query)
    results should be (null)
    tearDown
  }

  it should "be able to retrieve user profile by username" in {
    setUp
    insertSampleDocument
    insertSampleDocumentWithForcedId
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

	def setUp = {
		ProfileDao.mongodbName = "lecartontest"
		selfDestructButton
	}
	
	def tearDown = {
		selfDestructButton
	}
	
	def insertSampleDocument = ProfileDao.createUserProfile("AAAA", "hurtzip", "sample@email.com")
  def insertSampleDocument2 = ProfileDao.createUserProfile("AAAA", "hurtzip", "sample@gmail.com")
  def insertSampleDocumentWithForcedId = ProfileDao.createUserProfile("AAAB", "hurtzip", "sample@email.com", new ObjectId("54485f901adee7b53870bacb"))
  def getDocumentCount = MongoConnection()(ProfileDao.mongodbName)(ProfileDao.profileCollectionName).count(MongoDBObject.empty)
  def selfDestructButton = MongoConnection()(ProfileDao.mongodbName)(ProfileDao.profileCollectionName).remove(MongoDBObject.empty)
}