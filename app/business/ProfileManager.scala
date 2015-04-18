package business

import dao.PGDaoTrait
import models.Profile
import org.mindrot.jbcrypt.BCrypt

import scala.util.{Failure, Success}

/**
 * Business logic for handling profiles.
 * Created by justin on 11/7/14.
 */
class ProfileManager extends PGDaoTrait {

  def queryUserProfileByUsername(username: String): Option[Profile] = {
    profiles.byUsername(username) match {
      case Success(x) => x
      case Failure(x) => { println(x); None }
    }
  }

  /**
   * Does a quick check to verify that the user does indeed exist.
   * @param username The username to search for.
   * @return true if exists.
   */
  def userExists(username: String): Option[Boolean] = profiles.byUsername(username) match {
    case Success(x) => Some(!x.isEmpty)
    case Failure(x) => { println(x); None }
  }

  /**
   * Does a quick check to verify that the user does indeed exist.
   * @param userId The user's id to search for.
   * @return true if exists.
   */
  def userExists(userId: Long): Option[Boolean] = profiles.byId(userId) match {
    case Success(x) => Some(!x.isEmpty)
    case Failure(x) => { println(x); None }
  }

  /**
   * Creates a new userafter checking that one doesn't already exist.
   * @param username The username to create with.
   * @param password The password to create with.
   * @param email The email to create with.
   * @return New ProfileTO if successful or null ortherwise.
   */
  def createUser(username: String, password: String, email: String, isAdmin: Boolean): Profile = userExists(username) match {
    case Some(x) if x => null
    case Some(x) if !x => {
      val newProfile = Profile(None, username, password, email, isAdmin)
      profiles += newProfile match {
        case Success(x) => newProfile.copy(password = null)
        case Failure(x) => null
      }
    }
    case _ => null
  }

  /**
   * Attempts to log in a profile.
   * @param username The username to log in with.
   * @param password The password to log in with.
   * @return The ProfileTO of the logged in user profile.
   */
  def attemptLogin(username: String, password: String): Profile = {
    profiles.byUsername(username) match {
      case Success(Some(x)) => return if (BCrypt.checkpw(password, x.password)) Profile(Some(x.id.get), x.username, null, x.email, x.isAdmin) else null
      case Failure(x) => { println(x); null }
      case _ => null
    }
  }

  /**
   * Calls the model to get a count.
   * @return Count or -1 if there was a read issue.
   */
  def countProfiles: Int = {
    pastes.size match {
      case Success(x) => x
      case Failure(x) => { println(x); -1 }
    }
  }
}
