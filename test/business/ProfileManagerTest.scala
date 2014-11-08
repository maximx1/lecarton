package business

import dao.ProfileDao
import models.ProfileTO
import org.bson.types.ObjectId
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
    profileManager.userExists("AAAA") should be (true)
  }

  it should "return false if username is not found" in {
    val query = ProfileTO(null, "AAAA", null, null)
    val expectedResponse = null
    Mockito.when(profileManager.profileDao.queryUserProfileByUsername(query)).thenReturn(expectedResponse)
    profileManager.userExists("AAAA") should be (false)
  }

  it should "return true if user id is found" in {
    val query = ProfileTO(new ObjectId, null, null, null)
    val expectedResponse = ProfileTO(query._id, "AAAA", "asdf", "sample@email.com")
    Mockito.when(profileManager.profileDao.queryUserProfileById(query)).thenReturn(expectedResponse)
    profileManager.userExists(query._id) should be (true)
  }

  it should "return false if user id is not found" in {
    val query = ProfileTO(new ObjectId, null, null, null)
    val expectedResponse = null
    Mockito.when(profileManager.profileDao.queryUserProfileById(query)).thenReturn(expectedResponse)
    profileManager.userExists(query._id) should be (false)
  }
}
