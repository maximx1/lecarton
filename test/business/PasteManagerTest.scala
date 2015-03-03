package business

import dao.{ProfileDao, PasteDao}
import models.{ProfileTO, PasteTO}
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
    val privateExpectedQuery = PasteTO(-1, null, -1, "title", null, false)
    val publicExpectedQuery = PasteTO(-1, null, -1, "title", null, true)
    Mockito.when(pasteManager.pasteDao.queryPasteByTitle(privateExpectedQuery)).thenReturn(createPasteSearchResult)
    Mockito.when(pasteManager.pasteDao.queryPasteByTitle(publicExpectedQuery)).thenReturn(List.empty)
    val results = pasteManager.handlePasteSearch("titles", privateExpectedQuery.title, None)
    results should have size createPasteSearchResult.size
  }

  it should "return an empty list when there are no results when searching by titles" in {
    val privateExpectedQuery = PasteTO(-1, null, -1, "title", null, false)
    val publicExpectedQuery = PasteTO(-1, null, -1, "title", null, true)
    Mockito.when(pasteManager.pasteDao.queryPasteByTitle(privateExpectedQuery)).thenReturn(List.empty)
    Mockito.when(pasteManager.pasteDao.queryPasteByTitle(publicExpectedQuery)).thenReturn(List.empty)
    val results = pasteManager.handlePasteSearch("titles", privateExpectedQuery.title, None)
    results should have size 0
  }

  it should "return an empty list when scope isn't valid" in {
    val privateExpectedQuery = PasteTO(-1, null, -1, "title", null, false)
    val publicExpectedQuery = PasteTO(-1, null, -1, "title", null, true)
    Mockito.when(pasteManager.pasteDao.queryPasteByTitle(privateExpectedQuery)).thenReturn(List.empty)
    Mockito.when(pasteManager.pasteDao.queryPasteByTitle(publicExpectedQuery)).thenReturn(List.empty)
    pasteManager.handlePasteSearch("completely invalid scope", null, None) should have size 0
  }

  it should "be able to look up by owner" in {
    val testName = "mrOwnerMan"
    val expectedProfileSearchQuery = ProfileTO(-1, testName, null, null, false)
    val expectedProfileSearchResponse = ProfileTO(1, testName, null, null, false)
    Mockito.when(pasteManager.profileDao.queryUserProfileByUsername(expectedProfileSearchQuery)).thenReturn(Some(expectedProfileSearchResponse))

    val privateExpectedQuery = PasteTO(-1, null, 1, null, null, false)
    val publicExpectedQuery = PasteTO(-1, null, 1, null, null, true)
    Mockito.when(pasteManager.pasteDao.queryPastesOfOwner(privateExpectedQuery)).thenReturn(List.empty)
    Mockito.when(pasteManager.pasteDao.queryPastesOfOwner(publicExpectedQuery)).thenReturn(createPasteSearchResult)
    val results = pasteManager.handlePasteSearch("profiles", testName, None)
    results should have size createPasteSearchResult.size
  }

  it should "return an empty list when owning profile isn't found" in {
    val expectedProfileSearchQuery = ProfileTO(1, "mrOwnerMan", null, null, false)
    Mockito.when(pasteManager.profileDao.queryUserProfileByUsername(expectedProfileSearchQuery)).thenReturn(null)

    val results = pasteManager.handlePasteSearch("profiles", "mrOwnerMan", None)
    results should have size 0
  }

  it should "return an empty list when there are no results when searching by profile owner" in {
    val testName = "mrOwnerMan"
    val ownersObjectId = 1
    val expectedProfileSearchQuery = ProfileTO(-1, testName, null, null, false)
    val expectedProfileSearchResponse = ProfileTO(1, testName, null, null, false)
    Mockito.when(pasteManager.profileDao.queryUserProfileByUsername(expectedProfileSearchQuery)).thenReturn(Some(expectedProfileSearchResponse))

    val privateExpectedQuery = PasteTO(-1, null, ownersObjectId, null, null, false)
    val publicExpectedQuery = PasteTO(-1, null, ownersObjectId, null, null, true)
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
    val results = pasteManager.restrictAndFilterSearch(createPasteSearchResultWithOnePrivate, Some("1"))
    results.filter(x => x.isPrivate) should have size 1
    results should have size 6
  }

  "a url" should "be converted to an <a> tag" in {
    val original = "hello [my github](https://github.com/maximx1) world"
    val result = PasteManager.convertLinksToHTML(original)
    result should be ("hello <a href='https://github.com/maximx1'>my github</a> world")
  }

  "visibility" should "be able to be updated if owner is logged in" in {
    val pasteQuery: PasteTO = PasteTO(-1, "asdf", -1, null, null, false)
    val expectedResult: PasteTO = PasteTO(1, "asdf", 1, "", "", true)
    Mockito.when(pasteManager.pasteDao.queryPasteByPasteId(pasteQuery)).thenReturn(Some(expectedResult))
    val (actual, message) = pasteManager.updatePasteVisibility(Some("1"), "asdf", false)
    assert(actual)
  }

  it should "fail to update when the passed in user id is closed" in {
    val (actual, message) = pasteManager.updatePasteVisibility(Option.empty, "asdf", false)
    assert(!actual)
  }

  it should "fail to update when the passed in user id doesn't match the paste's owner" in {
    val pasteQuery: PasteTO = PasteTO(-1, "asdf", -1, null, null, false)
    val expectedResult: PasteTO = PasteTO(1, "asdf", 1, "", "", true)
    Mockito.when(pasteManager.pasteDao.queryPasteByPasteId(pasteQuery)).thenReturn(Some(expectedResult))
    val (actual, message) = pasteManager.updatePasteVisibility(Some("2"), "asdf", false)
    assert(!actual)
  }

  "Markdown content conversion" should "convert a markdown link to markdown" in {
    val actual = PasteManager.contentToMd(Some(markdownConvertedExample)).get
    actual.content should be ("<p><a href=\"https://google.com\">google</a></p>")
  }
  
  it should "not change any of the other values in the TO" in {
    val actual = PasteManager.contentToMd(Some(markdownConvertedExample)).get
    actual._id should be (1)
    actual.pasteId should be ("asdf")
    actual.title should be ("title 1")
    actual.isPrivate should be (false)
  }
  
  it should "return none if the input TO is none" in {
    val actual = PasteManager.contentToMd(None)
    actual should be (None)
  }
  
  val markdownConvertedExample = PasteTO(1, "asdf", 1, "title 1", "[google](https://google.com)", false)

  lazy val createPasteSearchResult: List[PasteTO] = List(
    PasteTO(1, "aaaa", 1, "title 1", PasteDao.generateRandomString(40), false),
    PasteTO(2, "bbbb", 1, "title 2", PasteDao.generateRandomString(40), false),
    PasteTO(3, "cccc", 1, "title 3", PasteDao.generateRandomString(40), false),
    PasteTO(4, "dddd", 1, "title 4", PasteDao.generateRandomString(40), false),
    PasteTO(5, "eeee", 1, "title 5", PasteDao.generateRandomString(40), false),
    PasteTO(6, "ffff", 1, "title 6", PasteDao.generateRandomString(40), false)
  )

  lazy val createPasteSearchResultWithOnePrivate: List[PasteTO] = createPasteSearchResult.map(x=> {if(x.title == "title 4") x.isPrivate = true;x})
}
