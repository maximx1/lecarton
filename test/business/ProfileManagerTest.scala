package business

import java.sql.SQLException

import models.Profile
import org.mindrot.jbcrypt.BCrypt
import test.core.BaseTestSpec

import scala.util.{Failure, Success}

/**
 * Created by justin on 3/2/15.
 */
class ProfileManagerTest extends BaseTestSpec {
  val profileManager: ProfileManager = new ProfileManager with TestDaoTrait
  val testProfile = Profile(Some(1), "AAAA", "asdf", "sample@email.com", false)
  val dbConnectionError = "Error connecting to the db"

  "Checking if user exists" should "return true if username is found" in {
    (profileManager.profiles.byUsername _) expects("AAAA") returning (Success(Some(testProfile)))
    assert(profileManager.userExists("AAAA").get)
  }

  it should "return false if username is not found" in {
    (profileManager.profiles.byUsername _) expects("AAAA") returning(Success(None))
    assert(!profileManager.userExists("AAAA").get)
  }

  it should "log and return None should there be a failure calling for profile by username" in {
    (profileManager.profiles.byUsername _) expects("AAAA") returning(Failure(new SQLException(dbConnectionError)))
    profileManager.userExists("AAAA") should be (None)
  }

  it should "return true if user id is found" in {
    (profileManager.profiles.byId _) expects(1l) returning(Success(Some(testProfile)))
    assert(profileManager.userExists(1).get)
  }

  it should "return false if user id is not found" in {
    (profileManager.profiles.byId _) expects(1l) returning(Success(None))
    assert(!profileManager.userExists(1).get)
  }

  it should "log and return None should there be a failure calling for profile by id" in {
    (profileManager.profiles.byId _) expects(1l) returning(Failure(new SQLException(dbConnectionError)))
    profileManager.userExists(1) should be (None)
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

  it should "return null if None was returned from the user existance check" in {
    (profileManager.profiles.byUsername _) expects("AAAA") returning(Failure(new SQLException(dbConnectionError)))
    profileManager.createUser("AAAA", "", "", false) should be (null)
  }

  it should "return null if there was a db failure while inserting profile" in {
    (profileManager.profiles.byUsername _) expects("AAAA") returning(Success(None))
    (profileManager.profiles.+= _) expects(*) returning(Failure(new SQLException(dbConnectionError)))
    profileManager.createUser("AAAA", "", "", false) should be (null)
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

  it should "fail, log error, and return null if there is a db error calling profiles by username" in {
    (profileManager.profiles.byUsername _) expects(*) returning(Failure(new SQLException(dbConnectionError)))
    profileManager.attemptLogin("tom", "1234") should be (null)
  }

  "Count profiles" should "be able to return a count of the profiles" in {
    (profileManager.profiles.size _) expects() returning(Success(100))
    profileManager.countProfiles should be (100)
  }

  it should "return -1 and log to fail gracefully" in {
    (profileManager.profiles.size _) expects() returning(Failure(new SQLException(dbConnectionError)))
    profileManager.countProfiles should be (-1)
  }

  "Query Profile" should "return a profile in Option if a profile is found" in {
    (profileManager.profiles.byUsername _) expects("AAAA") returning(Success(Some(testProfile)))
    profileManager.queryUserProfileByUsername("AAAA") should be (Some(testProfile))
  }

  it should "return None if a profile is found" in {
    (profileManager.profiles.byUsername _) expects(*) returning(Success(None))
    profileManager.queryUserProfileByUsername("AAAA") should be (None)
  }

  it should "log and return None should there be a failure calling the db" in {
    (profileManager.profiles.byUsername _) expects(*) returning(Failure(new SQLException(dbConnectionError)))
    profileManager.queryUserProfileByUsername("AAAA") should be (None)
  }

  def successfulCreateUserBase(isAdmin: Boolean=false) = {
    (profileManager.profiles.byUsername _) expects("tom") returning(Success(None))
    (profileManager.profiles += _) expects(Profile(None, "tom", "1234", "sample@email.com", isAdmin)) returning(Success(1))
    profileManager.createUser("tom", "1234", "sample@email.com", isAdmin)
  }
}
