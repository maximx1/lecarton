package business

import java.sql.SQLException

import models._
import test.core.BaseTestSpec
import utils.{ConversionUtils, RandomUtils}

import scala.util.{Failure, Success}

class PasteManagerTest extends BaseTestSpec {
  val pasteManager: PasteManager = new PasteManager with TestDaoTrait
  val contentFieldPreviewMaxLength = 35
  val dbConnectionError = "Error connecting to the db"

  "Search" should "be able to look up by title" in {
    (pasteManager.pastes.byTitle _) expects ("title") returning(Success(createPasteSearchResult))
    pasteManager.handlePasteSearch("titles", "title", None) should have size createPasteSearchResult.size
  }

  it should "return an empty list when there are no results when searching by titles" in {
    (pasteManager.pastes.byTitle _) expects ("title") returning(Success(List.empty))
    pasteManager.handlePasteSearch("titles", "title", None) should have size 0
  }

  it should "return an empty list when scope isn't valid" in {
    pasteManager.handlePasteSearch("completely invalid scope", null, None) should have size 0
  }

  it should "log error and return an empty list when db fails looking for title" in {
    (pasteManager.pastes.byTitle _) expects ("title") returning(Failure(new SQLException(dbConnectionError)))
    pasteManager.handlePasteSearch("titles", "title", None) should have size 0
  }

  it should "be able to look up by owner" in {
    val testName = "mrOwnerMan"
    (pasteManager.profiles.byUsername _) expects(testName) returning(Success(Some(Profile(Some(1), testName, null, null, false))))
    (pasteManager.pastes.byOwner _) expects (1l) returning(Success(createPasteSearchResult))
    pasteManager.handlePasteSearch("profiles", testName, None) should have size createPasteSearchResult.size
  }

  it should "return an empty list when owning profile isn't found" in {
    (pasteManager.profiles.byUsername _) expects("mrOwnerMan") returning (null)
    pasteManager.handlePasteSearch("profiles", "mrOwnerMan", None) should have size 0
  }

  it should "log error and return an empty list when db fails looking for profile before pastes" in {
    (pasteManager.profiles.byUsername _) expects("mrOwnerMan") returning(Failure(new SQLException(dbConnectionError)))
    pasteManager.handlePasteSearch("profiles", "mrOwnerMan", None) should have size 0
  }

  it should "log error and return an empty list when db fails looking for pastes after finding profile" in {
    val testName = "mrOwnerMan"
    (pasteManager.profiles.byUsername _) expects(testName) returning(Success(Some(Profile(Some(1), testName, null, null, false))))
    (pasteManager.pastes.byOwner _) expects (1l) returning(Failure(new SQLException(dbConnectionError)))
    pasteManager.handlePasteSearch("profiles", testName, None) should have size 0
  }

  it should "return an empty list when there are no results when searching by profile owner" in {
    val testName = "mrOwnerMan"
    (pasteManager.profiles.byUsername _) expects(testName) returning(Success(Some(Profile(Some(1), testName, null, null, false))))
    (pasteManager.pastes.byOwner _) expects (1l) returning(Success(List.empty))
    pasteManager.handlePasteSearch("profiles", testName, None) should have size 0
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

  "visibility" should "be able to be updated if owner is logged in" in {
    val pasteId = "asdf"
    (pasteManager.pastes.byPasteId _) expects (pasteId) returning (Success(Some(Paste(Some(1), pasteId, 1, "", "", true))))
    (pasteManager.pastes.updateVisibility _) expects(*) returning (Success(1))
    val (actual, message) = pasteManager.updatePasteVisibility(Some("1"), pasteId, false)
    assert(actual)
  }

  it should "fail to update when the passed in user id is closed" in {
    val (actual, message) = pasteManager.updatePasteVisibility(Option.empty, "asdf", false)
    assert(!actual)
  }

  it should "fail to update when the passed in user id doesn't match the paste's owner" in {
    val pasteId = "asdf"
    (pasteManager.pastes.byPasteId _) expects (pasteId) returning (Success(Some(Paste(Some(1), pasteId, 1, "", "", true))))
    val (actual, message) = pasteManager.updatePasteVisibility(Some("2"), pasteId, false)
    assert(!actual)
  }

  it should "fail to update when there is a database error updating" in {
    val pasteId = "asdf"
    (pasteManager.pastes.byPasteId _) expects (pasteId) returning (Success(Some(Paste(Some(1), pasteId, 1, "", "", true))))
    (pasteManager.pastes.updateVisibility _) expects(*) returning (Failure(new SQLException(dbConnectionError)))
    val (actual, message) = pasteManager.updatePasteVisibility(Some("1"), pasteId, false)
    assert(!actual)
    message should be (pasteManager.dbError)
  }

  "Markdown content conversion" should "convert a markdown link to markdown" in {
    val actual = ConversionUtils.contentToMd(Some(markdownConvertedExample)).get
    actual.content should be ("<p><a href=\"https://google.com\">google</a></p>")
  }

  it should "not change any of the other values in the TO" in {
    val actual = ConversionUtils.contentToMd(Some(markdownConvertedExample)).get
    actual.id.get should be (1)
    actual.pasteId should be ("asdf")
    actual.title should be ("title 1")
    actual.isPrivate should be (false)
  }

  it should "return none if the input TO is none" in {
    val actual = ConversionUtils.contentToMd(None)
    actual should be (None)
  }

  "Count pastes" should "be able to return a count of the pastes" in {
    (pasteManager.pastes.size _) expects() returning(Success(100))
    pasteManager.countPastes should be (100)
  }

  it should "return -1 and log to fail gracefully" in {
    (pasteManager.pastes.size _) expects() returning(Failure(new SQLException(dbConnectionError)))
    pasteManager.countPastes should be (-1)
  }

  "Create paste" should "return new paste when successfully inserted" in {
    val paste = markdownConvertedExample
    (pasteManager.pastes.insert _) expects(*) returning(Success(paste))
    pasteManager.createPaste(paste.ownerId, paste.title, paste.content, paste.isPrivate) should be (paste)
  }

  it should "log error and return null if there was anothing wrong in the model layer" in {
    val paste = markdownConvertedExample
    (pasteManager.pastes.insert _) expects(*) returning(Failure(new SQLException(dbConnectionError)))
    pasteManager.createPaste(paste.ownerId, paste.title, paste.content, paste.isPrivate) should be (null)
  }

  "Query paste" should "return a paste in an option when valid pasteId passed in" in {
    (pasteManager.pastes.byPasteId _) expects(markdownConvertedExample.pasteId) returning(Success(Some(markdownConvertedExample)))
    pasteManager.queryPasteByPasteId(markdownConvertedExample.pasteId) should be (Some(markdownConvertedExample))
  }

  it should "return none if the paste isn't found" in {
    (pasteManager.pastes.byPasteId _) expects(markdownConvertedExample.pasteId) returning(Success(None))
    pasteManager.queryPasteByPasteId(markdownConvertedExample.pasteId) should be (None)
  }

  it should "log and return None if there is a db failure" in {
    (pasteManager.pastes.byPasteId _) expects(markdownConvertedExample.pasteId) returning(Failure(new SQLException(dbConnectionError)))
    pasteManager.queryPasteByPasteId(markdownConvertedExample.pasteId) should be (None)
  }

  val markdownConvertedExample = Paste(Some(1), "asdf", 1, "title 1", "[google](https://google.com)", false)

  val createPasteSearchResult: List[Paste] = List(
    Paste(Some(1), "aaaa", 1, "title 1", RandomUtils.generateRandomString(40), false),
    Paste(Some(2), "bbbb", 1, "title 2", RandomUtils.generateRandomString(40), false),
    Paste(Some(3), "cccc", 1, "title 3", RandomUtils.generateRandomString(40), false),
    Paste(Some(4), "dddd", 1, "title 4", RandomUtils.generateRandomString(40), false),
    Paste(Some(5), "eeee", 1, "title 5", RandomUtils.generateRandomString(40), false),
    Paste(Some(6), "ffff", 1, "title 6", RandomUtils.generateRandomString(40), false)
  )

  val createPasteSearchResultWithOnePrivate: List[Paste] = createPasteSearchResult.map(x => x match {
    case paste if paste.title == "title 4" => paste.copy(isPrivate = true)
    case _ => x
  })
}
