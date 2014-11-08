package dao

import business.ProfileManager
import models.ProfileTO
import org.bson.types.ObjectId
import org.scalatest.{FunSpec, Matchers, BeforeAndAfter}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito
import org.mockito.Matchers.{ eq => mockitoEq, any }

/**
 * Created by justin on 11/8/14.
 */
class AnonUserManagerTest extends FunSpec with Matchers with BeforeAndAfter with MockitoSugar {

  val profileManager = null

  before {
    AnonUserManager.anonId.reset()
    AnonUserManager.profileDao = mock[ProfileDao]
    AnonUserManager.profileManager = mock[ProfileManager]
  }

  describe("AnonUserManager") {
    it ("can get anon user's objectId number") {
      val expected = new ObjectId()
      Mockito.when(AnonUserManager.profileManager.userExists("anon")).thenReturn(true)
      val query = ProfileTO(null, "anon", null, null)
      val expectedResponse = ProfileTO(expected, "anon", "", "")
      Mockito.when(AnonUserManager.profileDao.queryUserProfileByUsername(query)).thenReturn(expectedResponse)
      AnonUserManager.anonId.value should be (expected)
      Mockito.verify(AnonUserManager.profileManager).userExists("anon")
      Mockito.verify(AnonUserManager.profileDao).queryUserProfileByUsername(query)
    }

    it ("can create a new anon user if it doesn't exist") {
      val expectedId = new ObjectId()
      Mockito.when(AnonUserManager.profileManager.userExists("anon")).thenReturn(false)

      val expectedResponseForQueryByUsername = ProfileTO(expectedId, "anon", "", "")
      Mockito.when(AnonUserManager.profileManager.createUser(mockitoEq("anon"), any[String], mockitoEq("anon@sample.com"))).thenReturn(expectedResponseForQueryByUsername)

      AnonUserManager.anonId.value should be (expectedId)
      Mockito.verify(AnonUserManager.profileManager).userExists("anon")
      Mockito.verify(AnonUserManager.profileManager).createUser(mockitoEq("anon"), any[String], mockitoEq("anon@sample.com"))
    }
  }
}