package dao

import org.scalatest.{FlatSpec, Matchers}
import scala.collection.mutable.MutableList
import scala.collection.mutable.Set
import models.PasteTO
import org.bson.types.ObjectId
import com.mongodb.casbah.Imports._
import org.apache.xalan.xsltc.compiler.ForEach
import converters.PasteMongoConverters

class PasteDaoTest extends FlatSpec with Matchers {
	"Random string generator" should "create random strings" in {
		var i = 0
		var randomStrings: Set[String] = Set()
		//MutableList[String] = MutableList()
		for(i <- 1 to 1000) {
			randomStrings += PasteDao.generateRandomString(10)
		}
		
		assert(randomStrings.size == 1000)
	}
	
	"PasteDao" should "be able to store a new paste" in {
		setUp
		val originalDocCount = getDocumentCount
		val newObject = insertSampleDocument(false)
		val newId = newObject.getAs[ObjectId]("_id").toString()
		assert(originalDocCount < getDocumentCount)
		assert(newId != null)
		tearDown
	}
	
	it should "be able to retrieve all the private pastes of a specific owner" in {
		setUp
		insertSampleDocument(true)
		insertSampleDocument(true)
		insertSampleDocument(true)
		insertSampleDocument(false)
		val query = PasteTO(null, null, owner = new ObjectId("54485f901adee7b53870bacb"), null, null, isPrivate = true)
		val results = PasteDao.queryPastesOfOwner(query)
		assert(results.size == 3)
		results.foreach { x =>
			assert(x.isPrivate) 
		}
		tearDown
	}
	
	it should "be able to retrieve all the public pastes of a specific owner" in {
		setUp
		insertSampleDocument(true)
		insertSampleDocument(true)
		insertSampleDocument(true)
		insertSampleDocument(false)
		val query = PasteTO(null, null, owner = new ObjectId("54485f901adee7b53870bacb"), null, null, isPrivate = false)
		val results = PasteDao.queryPastesOfOwner(query)
		assert(results.size == 1)
		results.foreach { x =>
			assert(!x.isPrivate) 
		}
		tearDown
	}

  it should "be able to retrieve all public pastes matching title search" in {
    setUp
    insertSampleDocumentWithTitle(true, "sampleWithPrivate")
    insertSampleDocumentWithTitle(false, "sample1")
    insertSampleDocumentWithTitle(false, "sample2")
    insertSampleDocumentWithTitle(false, "something without elpmas <- (reverse that) in the title")
    val query = PasteTO(null, null, null, "sample", null, isPrivate = false)
    val results = PasteDao.queryPasteByTitle(query)
    results should have size 2
    results.foreach { x =>
      assert(!x.isPrivate)
    }
    results.filter(x => x.title == "sample1") should have size 1
    results.filter(x => x.title == "sample2") should have size 1
    tearDown
  }

	it should "be able to retrieve one paste by pasteId" in {
		setUp
		val original = PasteMongoConverters.convertFromMongoObject(insertSampleDocument(false))
		insertSampleDocument(false)
		val query = PasteTO(null, original.pasteId, null, null, null, false)
		val result = PasteDao.queryPasteByPasteId(query)
		assert(result != null)
		assert(result._id == original._id)
		assert(result.pasteId == original.pasteId)
		assert(result.owner == original.owner)
		assert(result.title == original.title)
		assert(result.content == original.content)
		assert(result.isPrivate == original.isPrivate)
		tearDown
	}
	
	it should "return null if it doesn't find paste by pasteId" in {
		setUp
		val query = PasteTO(null, "", null, null, null, false)
		val result = PasteDao.queryPasteByPasteId(query)
		assert(result == null)
		tearDown
	}

  "PasteConverter" should "throw Mongo Exception if a field is missing" in {
    setUp
    insertDocumentWithMissingField
    val query = PasteTO(null, null, null, "asdf", null, isPrivate = false)
    intercept[MongoException] {
      PasteDao.queryPasteByTitle(query)
    }
    tearDown
  }
	
	def setUp = {
		PasteDao.mongodbName = "lecartontest"
		selfDestructButton
	}

  def setUp(dbName: String) = {
    PasteDao.mongodbName = dbName
    selfDestructButton
  }

	def tearDown = {
		selfDestructButton
	}

  def insertDocumentWithMissingField: MongoDBObject = {
    val mongoConnection = MongoConnection()
    val collection = mongoConnection(PasteDao.mongodbName)(PasteDao.pasteCollectionName)
    val newObject = MongoDBObject(
      "owner" -> "1234",
      "title" -> "asdf",
      "content" -> "asdf",
      "isPrivate" -> false
    )
    collection += newObject
    return newObject
  }

	def insertSampleDocument(isPrivate: Boolean) = PasteDao.createPaste(new ObjectId("54485f901adee7b53870bacb"), "sample", "Sample Message", isPrivate)
  def insertSampleDocumentWithTitle(isPrivate: Boolean, title: String) = PasteDao.createPaste(new ObjectId("54485f901adee7b53870bacb"), title, "Sample Message", isPrivate)
  def getDocumentCount = MongoConnection()(PasteDao.mongodbName)(PasteDao.pasteCollectionName).count(MongoDBObject.empty)
  def selfDestructButton = MongoConnection()(PasteDao.mongodbName)(PasteDao.pasteCollectionName).remove(MongoDBObject.empty)
}