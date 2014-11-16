package business

import dao.{ProfileDao, PasteDao}
import models.{ProfileTO, PasteTO}
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
    val mockProfileDao = mock[ProfileDao]
    pasteManager.pasteDao = mockPasteDao
    pasteManager.profileDao = mockProfileDao
  }

  "Search" should "be able to look up by title" in {
    val privateExpectedQuery = PasteTO(null, null, null, "title", null, false)
    val publicExpectedQuery = PasteTO(null, null, null, "title", null, true)
    Mockito.when(pasteManager.pasteDao.queryPasteByTitle(privateExpectedQuery)).thenReturn(createPasteSearchResult)
    Mockito.when(pasteManager.pasteDao.queryPasteByTitle(publicExpectedQuery)).thenReturn(List.empty)
    val results = pasteManager.handlePasteSearch("titles", privateExpectedQuery.title, None)
    results should have size createPasteSearchResult.size
  }

  it should "return an empty list when there are no results when searching by titles" in {
    val privateExpectedQuery = PasteTO(null, null, null, "title", null, false)
    val publicExpectedQuery = PasteTO(null, null, null, "title", null, true)
    Mockito.when(pasteManager.pasteDao.queryPasteByTitle(privateExpectedQuery)).thenReturn(List.empty)
    Mockito.when(pasteManager.pasteDao.queryPasteByTitle(publicExpectedQuery)).thenReturn(List.empty)
    val results = pasteManager.handlePasteSearch("titles", privateExpectedQuery.title, None)
    results should have size 0
  }

  it should "return an empty list when scope isn't valid" in {
    pasteManager.pasteDao.mongodbName = "lecartontest" //Just to make sure
    pasteManager.handlePasteSearch("completely invalid scope", null, None) should have size 0
  }

  it should "be able to look up by owner" in {
    val testName = "mrOwnerMan"
    val ownersObjectId = new ObjectId("54485f901adee7b53870bacb")
    val expectedProfileSearchQuery = ProfileTO(null, testName, null, null)
    val expectedProfileSearchResponse = ProfileTO(ownersObjectId, testName, null, null)
    Mockito.when(pasteManager.profileDao.queryUserProfileByUsername(expectedProfileSearchQuery)).thenReturn(expectedProfileSearchResponse)

    val privateExpectedQuery = PasteTO(null, null, ownersObjectId, null, null, false)
    val publicExpectedQuery = PasteTO(null, null, ownersObjectId, null, null, true)
    Mockito.when(pasteManager.pasteDao.queryPastesOfOwner(privateExpectedQuery)).thenReturn(List.empty)
    Mockito.when(pasteManager.pasteDao.queryPastesOfOwner(publicExpectedQuery)).thenReturn(createPasteSearchResult)
    val results = pasteManager.handlePasteSearch("profiles", testName, None)
    results should have size createPasteSearchResult.size
  }

  it should "return an empty list when owning profile isn't found" in {
    val expectedProfileSearchQuery = ProfileTO(null, "mrOwnerMan", null, null)
    Mockito.when(pasteManager.profileDao.queryUserProfileByUsername(expectedProfileSearchQuery)).thenReturn(null)

    val results = pasteManager.handlePasteSearch("profiles", "mrOwnerMan", None)
    results should have size 0
  }

  it should "return an empty list when there are no results when searching by profile owner" in {
    val testName = "mrOwnerMan"
    val ownersObjectId = new ObjectId("54485f901adee7b53870bacb")
    val expectedProfileSearchQuery = ProfileTO(null, testName, null, null)
    val expectedProfileSearchResponse = ProfileTO(ownersObjectId, testName, null, null)
    Mockito.when(pasteManager.profileDao.queryUserProfileByUsername(expectedProfileSearchQuery)).thenReturn(expectedProfileSearchResponse)

    val privateExpectedQuery = PasteTO(null, null, ownersObjectId, null, null, false)
    val publicExpectedQuery = PasteTO(null, null, ownersObjectId, null, null, true)
    Mockito.when(pasteManager.pasteDao.queryPastesOfOwner(privateExpectedQuery)).thenReturn(List.empty)
    Mockito.when(pasteManager.pasteDao.queryPastesOfOwner(publicExpectedQuery)).thenReturn(List.empty)
    val results = pasteManager.handlePasteSearch("profiles", testName, None)
    results should have size 0
  }

  "Restrict and filter" should "limit the value for content down to a set length" in {
    val results = pasteManager.restrictAndFilterSearch(createPasteSearchResult, None)
    results.filter(x => x.content.length > 35) should have size 0
  }

  it should "filter out private posts if there is no user" in {
    val results = pasteManager.restrictAndFilterSearch(createPasteSearchResultWithOnePrivate, None)
    results.filter(x => x.isPrivate) should have size 0
    results should have size 5
  }

  it should "filter out private posts if user is incorrect" in {
    val results = pasteManager.restrictAndFilterSearch(createPasteSearchResultWithOnePrivate, Some("12345"))
    results.filter(x => x.isPrivate) should have size 0
    results should have size 5
  }

  it should "include private posts if search result matches passed in user" in {
    val results = pasteManager.restrictAndFilterSearch(createPasteSearchResultWithOnePrivate, Some("54485f901adee7b53870bacb"))
    results.filter(x => x.isPrivate) should have size 1
    results should have size 6
  }

  "a url" should "be converted to an <a> tag" in {
    val original = "hello [my github](https://github.com/maximx1) world"
    val result = PasteManager.convertLinksToHTML(original)
    result should be ("hello <a href='https://github.com/maximx1'>my github</a> world")
  }

  "visibility" should "be able to be updated if owner is logged in" in {
    val pasteQuery: PasteTO = PasteTO(null, "asdf", null, null, null, false)
    val expectedResult: PasteTO = PasteTO(new ObjectId, "asdf", new ObjectId("54485f901adee7b53870bacb"), "", "", true)
    Mockito.when(pasteManager.pasteDao.queryPasteByPasteId(pasteQuery)).thenReturn(expectedResult)
    val (actual, message) = pasteManager.updatePasteVisibility(Some("54485f901adee7b53870bacb"), "asdf", false)
    assert(actual)
  }

  it should "fail to update when the passed in user id is closed" in {
    val (actual, message) = pasteManager.updatePasteVisibility(Option.empty, "asdf", false)
    assert(!actual)
  }

  it should "fail to update when the passed in user id doesn't match the paste's owner" in {
    val pasteQuery: PasteTO = PasteTO(null, "asdf", null, null, null, false)
    val expectedResult: PasteTO = PasteTO(new ObjectId, "asdf", new ObjectId("54485f901adee7b53871bacb"), "", "", true)
    Mockito.when(pasteManager.pasteDao.queryPasteByPasteId(pasteQuery)).thenReturn(expectedResult)
    val (actual, message) = pasteManager.updatePasteVisibility(Some("54485f901adee7b53870bacb"), "asdf", false)
    assert(!actual)
  }

  lazy val createPasteSearchResult: List[PasteTO] = List(
    PasteTO(new ObjectId(), "aaaa", new ObjectId("54485f901adee7b53870bacb"), "title 1", PasteDao.generateRandomString(40), false),
    PasteTO(new ObjectId(), "bbbb", new ObjectId("54485f901adee7b53870bacb"), "title 2", PasteDao.generateRandomString(40), false),
    PasteTO(new ObjectId(), "cccc", new ObjectId("54485f901adee7b53870bacb"), "title 3", PasteDao.generateRandomString(40), false),
    PasteTO(new ObjectId(), "dddd", new ObjectId("54485f901adee7b53870bacb"), "title 4", PasteDao.generateRandomString(40), false),
    PasteTO(new ObjectId(), "eeee", new ObjectId("54485f901adee7b53870bacb"), "title 5", PasteDao.generateRandomString(40), false),
    PasteTO(new ObjectId(), "ffff", new ObjectId("54485f901adee7b53870bacb"), "title 6", PasteDao.generateRandomString(40), false)
  )

  lazy val createPasteSearchResultWithOnePrivate: List[PasteTO] = createPasteSearchResult.map(x=> {if(x.title == "title 4") x.isPrivate = true;x})
}
