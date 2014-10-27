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
	
	"PasteDao" should "be able to retrieve all the private pastes of a specific owner" in {
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
	
	"PasteDao" should "be able to retrieve all the public pastes of a specific owner" in {
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
	
	"PasteDao" should "be able to retrieve one paste by pasteId" in {
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
	
	"PasteDao" should "return null if it doesn't find paste by pasteId" in {
		setUp
		val query = PasteTO(null, "", null, null, null, false)
		val result = PasteDao.queryPasteByPasteId(query)
		assert(result == null)
		tearDown
	}
	
	def setUp = {
		PasteDao.mongodbName = "lecartontest"
		selfDestructButton
	}
	
	def tearDown = {
		selfDestructButton
	}
	
	def insertSampleDocument(isPrivate: Boolean) = PasteDao.createPaste(new ObjectId("54485f901adee7b53870bacb"), "sample", "Sample Message", isPrivate)
	
    def getDocumentCount = MongoConnection()(PasteDao.mongodbName)(PasteDao.pasteCollectionName).count(MongoDBObject.empty)
	
    def selfDestructButton = MongoConnection()(PasteDao.mongodbName)(PasteDao.pasteCollectionName).remove(MongoDBObject.empty)
}