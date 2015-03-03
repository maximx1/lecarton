package business

import dao.ProfileDao
import models.ProfileTO
import org.mindrot.jbcrypt.BCrypt
import org.mockito.Mockito
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, Matchers, FlatSpec}

/**
 * Created by justin on 3/2/15.
 */
class ProfileManagerTest extends FlatSpec with Matchers with BeforeAndAfter with MockitoSugar {
  var profileManager: ProfileManager = null

  before {
    profileManager = new ProfileManager()
    val mockedProfileDao = mock[ProfileDao]
    profileManager.profileDao = mockedProfileDao
  }

  "Checking if user exists" should "return true if username is found" in {
    val query = ProfileTO(-1, "AAAA", null, null, false)
    val expectedResponse = ProfileTO(1, "AAAA", "asdf", "sample@email.com", false)
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(Some(expectedResponse))
    profileManager.userExists("AAAA") should be(true)
  }

  it should "return false if username is not found" in {
    val query = ProfileTO(-1, "AAAA", null, null, false)
    val expectedResponse = None
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(expectedResponse)
    profileManager.userExists("AAAA") should be(false)
  }

  it should "return true if user id is found" in {
    val query = ProfileTO(1, null, null, null, false)
    val expectedResponse = ProfileTO(query._id, "AAAA", "asdf", "sample@email.com", false)
    Mockito.when(profileManager.profileDao.queryUserProfileById(query)).thenReturn(Some(expectedResponse))
    profileManager.userExists(query._id) should be(true)
  }

  it should "return false if user id is not found" in {
    val query = ProfileTO(-1, null, null, null, false)
    val expectedResponse = null
    Mockito.when(profileManager.profileDao.queryUserProfileById(query)).thenReturn(expectedResponse)
    profileManager.userExists(query._id) should be(false)
  }

  "Create user profile" should "be able to create a new user profile" in {
    val actual = successfulCreateUserBase
    actual.username should be ("tom")
    actual.email should be ("sample@email.com")
  }

  it should "block creation if user already exists" in {
    val query = ProfileTO(-1, "tom", null, null, false)
    val expectedResponse = ProfileTO(1, "tom", "1234", "sample@email.com", false)
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(Some(expectedResponse))
    profileManager.createUser("tom", "1234", "sample@email.com") should be (null)
  }

  it should "strip the password from the returning object" in {
    val actual = successfulCreateUserBase
    actual.password should be (null)
  }

  "A login" should "be able to occur successfully if the moons are aligned" in {
    val query = ProfileTO(-1, "tom", null, null, false)
    val expectedResponse = ProfileTO(1, "tom", BCrypt.hashpw("1234", BCrypt.gensalt(4)), "sample@email.com", false)
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(Some(expectedResponse))
    val actual = profileManager.attemptLogin("tom", "1234")
    actual should not be null
  }

  it should "fail if the user lookup result is null" in {
    val query = ProfileTO(-1, "tom", null, null, false)
    val expectedResponse = None
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(expectedResponse)
    val actual = profileManager.attemptLogin("tom", "1234")
    actual should be (null)
  }

  it should "fail if the passwords do not match" in {
    val query = ProfileTO(-1, "tom", null, null, false)
    val expectedResponse = ProfileTO(1, "tom", BCrypt.hashpw("1234", BCrypt.gensalt(4)), "sample@email.com", false)
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(Some(expectedResponse))
    val actual = profileManager.attemptLogin("tom", "1235")
    actual should be (null)
  }

  lazy val successfulCreateUserBase = {
    val query = ProfileTO(-1, "tom", null, null, false)
    val expectedQueryUsernameResponse = None
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(expectedQueryUsernameResponse)
    Mockito.when(profileManager.profileDao.createUserProfile("tom", "1234", "sample@email.com")).thenReturn(successfullyReturnedDocument)
    profileManager.createUser("tom", "1234", "sample@email.com")
  }

  lazy val successfullyReturnedDocument = ProfileTO(1, "tom", "1234", "sample@email.com", false)
}
