package dao

import de.flapdoodle.embed.mongo.{MongodProcess, MongodExecutable, MongodStarter}
import org.scalatest._
import scala.collection.mutable.Set
import models.PasteTO
import org.bson.types.ObjectId
import com.mongodb.casbah.Imports._
import com.github.simplyscala.{MongodProps, MongoEmbedDatabase}
import converters.PasteMongoConverters

class PasteDaoTest extends FunSpec with Matchers with BeforeAndAfter {

  var pasteDao: PasteDao = null

  before {
    pasteDao = new PasteDao
    pasteDao.mongodbName = "lecartontest"
    selfDestructButton
  }

  after {
    selfDestructButton
    pasteDao = null
  }

  describe("Random string generator") {
    it("should create random strings") {
      var i = 0
      var randomStrings: Set[String] = Set()
      val pasteDao = new PasteDao
      for (i <- 1 to 1000) {
        randomStrings += PasteDao.generateRandomString(10)
      }

      assert(randomStrings.size == 1000)
    }
  }

  describe("PasteDao") {
    it("should be able to store a new paste") {
      val originalDocCount = getDocumentCount
      val newObject = insertSampleDocument(false)
      val newId = newObject.getAs[ObjectId]("_id").toString()
      assert(originalDocCount < getDocumentCount)
      assert(newId != null)
    }

    it("should be able to retrieve all the private pastes of a specific owner") {
      insertSampleDocument(true)
      insertSampleDocument(true)
      insertSampleDocument(true)
      insertSampleDocument(false)
      val query = PasteTO(null, null, owner = new ObjectId("54485f901adee7b53870bacb"), null, null, isPrivate = true)
      val results = pasteDao.queryPastesOfOwner(query)
      assert(results.size == 3)
      results.foreach { x =>
        assert(x.isPrivate)
      }
    }

    it("should be able to retrieve all the public pastes of a specific owner") {
      insertSampleDocument(true)
      insertSampleDocument(true)
      insertSampleDocument(true)
      insertSampleDocument(false)
      val query = PasteTO(null, null, owner = new ObjectId("54485f901adee7b53870bacb"), null, null, isPrivate = false)
      val results = pasteDao.queryPastesOfOwner(query)
      assert(results.size == 1)
      results.foreach { x =>
        assert(!x.isPrivate)
      }
    }

    it("should be able to retrieve all public pastes matching title search") {
      insertSampleDocumentWithTitle(true, "sampleWithPrivate")
      insertSampleDocumentWithTitle(false, "sample1")
      insertSampleDocumentWithTitle(false, "sample2")
      insertSampleDocumentWithTitle(false, "something without elpmas <- (reverse that) in the title")
      val query = PasteTO(null, null, null, "sample", null, isPrivate = false)
      val results = pasteDao.queryPasteByTitle(query)
      results should have size 2
      results.foreach { x =>
        assert(!x.isPrivate)
      }
      results.filter(x => x.title == "sample1") should have size 1
      results.filter(x => x.title == "sample2") should have size 1
    }

    it("should be able to retrieve one paste by pasteId") {
      val original = PasteMongoConverters.convertFromMongoObject(insertSampleDocument(false))
      insertSampleDocument(false)
      val query = PasteTO(null, original.pasteId, null, null, null, false)
      val result = pasteDao.queryPasteByPasteId(query)
      assert(result != null)
      assert(result._id == original._id)
      assert(result.pasteId == original.pasteId)
      assert(result.owner == original.owner)
      assert(result.title == original.title)
      assert(result.content == original.content)
      assert(result.isPrivate == original.isPrivate)
    }

    it("should return null if it doesn't find paste by pasteId") {
      val query = PasteTO(null, "", null, null, null, false)
      val result = pasteDao.queryPasteByPasteId(query)
      assert(result == null)
    }

    it("should be able to update the paste's visibility") {
      val insertionResult = insertSampleDocument(true)
      val originalId = insertionResult.getAs[String]("pasteId")
      val query = PasteTO(null, originalId.get, null, null, null, false)
      val originalDoc = pasteDao.queryPasteByPasteId(query)
      originalDoc.isPrivate = true
      pasteDao.updatePaste(originalDoc)
      val updatedDoc = pasteDao.queryPasteByPasteId(query)
      assert(updatedDoc.isPrivate)
    }
  }

  describe("PasteConverter")
  {
    it("should throw Mongo Exception if a field is missing") {
      insertDocumentWithMissingField
      val query = PasteTO(null, null, null, "asdf", null, isPrivate = false)
      intercept[MongoException] {
        pasteDao.queryPasteByTitle(query)
      }
    }
  }

  def insertDocumentWithMissingField: MongoDBObject = {
    val mongoConnection = MongoConnection()
    val collection = mongoConnection(pasteDao.mongodbName)(pasteDao.pasteCollectionName)
    val newObject = MongoDBObject(
      "owner" -> "1234",
      "title" -> "asdf",
      "content" -> "asdf",
      "isPrivate" -> false
    )
    collection += newObject
    return newObject
  }

	def insertSampleDocument(isPrivate: Boolean) = pasteDao.createPaste(new ObjectId("54485f901adee7b53870bacb"), "sample", "Sample Message", isPrivate)
  def insertSampleDocumentWithTitle(isPrivate: Boolean, title: String) = pasteDao.createPaste(new ObjectId("54485f901adee7b53870bacb"), title, "Sample Message", isPrivate)
  def getDocumentCount = MongoConnection()(pasteDao.mongodbName)(pasteDao.pasteCollectionName).count(MongoDBObject.empty)
  def selfDestructButton = MongoConnection()(pasteDao.mongodbName)(pasteDao.pasteCollectionName).remove(MongoDBObject.empty)
}