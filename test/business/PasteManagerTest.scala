package business

import dao.PasteDao
import models.PasteTO
import org.bson.types.ObjectId
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, BeforeAndAfter, Matchers}
import org.mockito.Mockito

class PasteManagerTest extends FlatSpec with Matchers with BeforeAndAfter with MockitoSugar {

  var pasteManager: PasteManager = null
  val contentFieldPreviewMaxLength = 35

  before {
    pasteManager = new PasteManager
    val mockPasteDao = mock[PasteDao]
    pasteManager.pasteDao = mockPasteDao
  }

  "Search" should "be able to look up by title" in {
    val expectedQuery = PasteTO(null, null, null, "title", null, false)
    Mockito.when(pasteManager.pasteDao.queryPasteByTitle(expectedQuery)).thenReturn(createPasteSearchResult)
    val results = pasteManager.handlePasteSearch("titles", expectedQuery.title)
    results should have size createPasteSearchResult.size
  }

  it should "return an empty list when scope isn't valid" in {
    pasteManager.pasteDao.mongodbName = "lecartontest" //Just to make sure
    pasteManager.handlePasteSearch("completely invalid scope", null) should have size 0
  }

  it should "limit the value for content down to a set length" in {
    val expectedQuery = PasteTO(null, null, null, "title", null, false)
    Mockito.when(pasteManager.pasteDao.queryPasteByTitle(expectedQuery)).thenReturn(createPasteSearchResult)
    val results = pasteManager.handlePasteSearch("titles", expectedQuery.title)
    results.filter(x => x.content.length > 35) should have size 0
  }

  lazy val createPasteSearchResult: List[PasteTO] = List(
    PasteTO(new ObjectId(), "aaaa", new ObjectId("54485f901adee7b53870bacb"), "title 1", PasteDao.generateRandomString(40), false),
    PasteTO(new ObjectId(), "bbbb", new ObjectId("54485f901adee7b53870bacb"), "title 2", PasteDao.generateRandomString(40), false),
    PasteTO(new ObjectId(), "cccc", new ObjectId("54485f901adee7b53870bacb"), "title 3", PasteDao.generateRandomString(40), false),
    PasteTO(new ObjectId(), "dddd", new ObjectId("54485f901adee7b53870bacb"), "title 4", PasteDao.generateRandomString(40), false),
    PasteTO(new ObjectId(), "eeee", new ObjectId("54485f901adee7b53870bacb"), "title 5", PasteDao.generateRandomString(40), false),
    PasteTO(new ObjectId(), "ffff", new ObjectId("54485f901adee7b53870bacb"), "title 6", PasteDao.generateRandomString(40), false)
  )
}
