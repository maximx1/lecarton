package business

import models.Profile
import org.mindrot.jbcrypt.BCrypt
import test.core.BaseTestSpec

import scala.util.Success

/**
 * Created by justin on 3/2/15.
 */
class ProfileManagerTest extends BaseTestSpec {
  val profileManager: ProfileManager = new ProfileManager with TestDaoTrait
  val testProfile = Profile(Some(1), "AAAA", "asdf", "sample@email.com", false)

  "Checking if user exists" should "return true if username is found" in {
    (profileManager.profiles.byUsername _) expects("AAAA") returning (Success(Some(testProfile)))
    assert(profileManager.userExists("AAAA").get)
  }

  it should "return false if username is not found" in {
    (profileManager.profiles.byUsername _) expects("AAAA") returning(Success(None))
    assert(!profileManager.userExists("AAAA").get)
  }

  it should "return true if user id is found" in {
    (profileManager.profiles.byId _) expects(1l) returning(Success(Some(testProfile)))
    assert(profileManager.userExists(1).get)
  }

  it should "return false if user id is not found" in {
    (profileManager.profiles.byId _) expects(1l) returning(Success(None))
    assert(!profileManager.userExists(1).get)
  }

  "Create user profile" should "be able to create a new user profile" in {
    val actual = successfulCreateUserBase()
    actual.username should be ("tom")
    actual.email should be ("sample@email.com")
  }

  it should "be able to create a new user profile as an admin" in {
    val actual = successfulCreateUserBase(true)
    actual.isAdmin should be (true)
  }

  it should "block creation if user already exists" in {
    (profileManager.profiles.byUsername _) expects("tom") returning(Success(Some(testProfile)))
    profileManager.createUser("tom", "1234", "sample@email.com", false) should be (null)
  }

  it should "strip the password from the returning object" in {
    val actual = successfulCreateUserBase()
    actual.password should be (null)
  }

  "A login" should "be able to occur successfully if the moons are aligned" in {
    val expectedResponse = Profile(Some(1), "tom", BCrypt.hashpw("1234", BCrypt.gensalt(4)), "sample@email.com", false)
    (profileManager.profiles.byUsername _) expects("tom") returning(Success(Some(expectedResponse)))
    profileManager.attemptLogin("tom", "1234") should not be null
  }

  it should "fail if the user lookup result is null" in {
    (profileManager.profiles.byUsername _) expects("tom") returning(Success(None))
    profileManager.attemptLogin("tom", "1234") should be (null)
  }

  it should "fail if the passwords do not match" in {
    val expectedResponse = Profile(Some(1), "tom", BCrypt.hashpw("1234", BCrypt.gensalt(4)), "sample@email.com", false)
    (profileManager.profiles.byUsername _) expects("tom") returning(Success(Some(expectedResponse)))
    profileManager.attemptLogin("tom", "1235") should be (null)
  }

  def successfulCreateUserBase(isAdmin: Boolean=false) = {
    (profileManager.profiles.byUsername _) expects("tom") returning(Success(None))
    (profileManager.profiles += _) expects(Profile(None, "tom", "1234", "sample@email.com", isAdmin)) returning(Success(1))
    profileManager.createUser("tom", "1234", "sample@email.com", isAdmin)
  }
}
