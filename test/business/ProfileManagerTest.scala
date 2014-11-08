package business

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import dao.ProfileDao
import models.ProfileTO
import org.bson.types.ObjectId
import org.mindrot.jbcrypt.BCrypt
import org.scalatest.{FlatSpec, Matchers, BeforeAndAfter}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito

/**
 * Created by justin on 11/7/14.
 */
class ProfileManagerTest extends FlatSpec with Matchers with MockitoSugar with BeforeAndAfter {

  var profileManager: ProfileManager = null

  before {
    profileManager = new ProfileManager()
    val mockedProfileDao = mock[ProfileDao]
    profileManager.profileDao = mockedProfileDao
  }

  "Checking if user exists" should "return true if username is found" in {
    val query = ProfileTO(null, "AAAA", null, null)
    val expectedResponse = ProfileTO(new ObjectId, "AAAA", "asdf", "sample@email.com")
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(expectedResponse)
    profileManager.userExists("AAAA") should be(true)
  }

  it should "return false if username is not found" in {
    val query = ProfileTO(null, "AAAA", null, null)
    val expectedResponse = null
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(expectedResponse)
    profileManager.userExists("AAAA") should be(false)
  }

  it should "return true if user id is found" in {
    val query = ProfileTO(new ObjectId, null, null, null)
    val expectedResponse = ProfileTO(query._id, "AAAA", "asdf", "sample@email.com")
    Mockito.when(profileManager.profileDao.queryUserProfileById(query)).thenReturn(expectedResponse)
    profileManager.userExists(query._id) should be(true)
  }

  it should "return false if user id is not found" in {
    val query = ProfileTO(new ObjectId, null, null, null)
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
    val query = ProfileTO(null, "tom", null, null)
    val expectedResponse = ProfileTO(new ObjectId, "tom", "1234", "sample@email.com")
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(expectedResponse)
    profileManager.createUser("tom", "1234", "sample@email.com") should be (null)
  }
  
  it should "strip the password from the returning object" in {
    val actual = successfulCreateUserBase
    actual.password should be (null)
  }

  "A login" should "be able to occur successfully if the moons are aligned" in {
    val query = ProfileTO(null, "tom", null, null)
    val expectedResponse = ProfileTO(new ObjectId, "tom", BCrypt.hashpw("1234", BCrypt.gensalt(4)), "sample@email.com")
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(expectedResponse)
    val actual = profileManager.attemptLogin("tom", "1234")
    actual should not be null
  }

  it should "fail if the user lookup result is null" in {
    val query = ProfileTO(null, "tom", null, null)
    val expectedResponse = null
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(expectedResponse)
    val actual = profileManager.attemptLogin("tom", "1234")
    actual should be (null)
  }

  it should "fail if the passwords do not match" in {
    val query = ProfileTO(null, "tom", null, null)
    val expectedResponse = ProfileTO(new ObjectId, "tom", BCrypt.hashpw("1234", BCrypt.gensalt(4)), "sample@email.com")
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(expectedResponse)
    val actual = profileManager.attemptLogin("tom", "1235")
    actual should be (null)
  }
  
  lazy val successfulCreateUserBase = {
    val query = ProfileTO(null, "tom", null, null)
    val expectedQueryUsernameResponse = null
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(expectedQueryUsernameResponse)
    Mockito.when(profileManager.profileDao.createUserProfile("tom", "1234", "sample@email.com")).thenReturn(successfullyReturnedDocument)
    profileManager.createUser("tom", "1234", "sample@email.com")
  }
  
  lazy val successfullyReturnedDocument: MongoDBObject = MongoDBObject(
    "_id" -> new ObjectId(),
    "username" -> "tom",
    "password" -> "1234",
    "email" -> "sample@email.com"
  )
}
