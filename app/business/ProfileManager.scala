package business

import models.Profile
import org.mindrot.jbcrypt.BCrypt

import scala.util.{Failure, Success}

/**
 * Business logic for handling profiles.
 * Created by justin on 11/7/14.
 */
class ProfileManager extends PGDaoTrait {
  
  val userNotSignedInError = "User not logged in"
  val loggedInUserNotAdminError = "Logged in user isn't admin"
  val currentUserIsUserBeingUpdatedError = "The current user is the user that is being updated"
  val userNotFoundError = "The user is not found"

  /**
   * Queries a user by id
   * @param id The id to look for.
   * @return The profile
   */
  def queryUserProfileById(id: Long): Option[Profile] = {
    profiles.byId(id) match {
      case Success(x) => x
      case Failure(x) => { println(x); None }
    }
  }

  /**
   * Queries a user by username
   * @param username The username to look for.
   * @return The profile
   */
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
    profiles.size match {
      case Success(x) => x
      case Failure(x) => { println(x); -1 }
    }
  }

  /**
   * Updates user admin status.
   * @param userId The user Id of the person to update.
   * @param newStatus The status to update to.
   * @param currentUser The current logged in user.
   * @return
   */
  def updateUserAdminStatus(userId: Long, newStatus: Boolean, currentUser: Option[Profile]): (Boolean, String) = {
    currentUser match {
      case Some(profile) => {
        profile.id match {
          case Some(id) if id == userId => (false, currentUserIsUserBeingUpdatedError)
          case Some(id) if id != userId => {
            profile.isAdmin match {
              case true => {
                profiles.updateAdminStatus(userId, newStatus) match {
                  case Success(updated) if updated > 0 => (true, null)
                  case Success(updated) if updated == 0 => (false, userNotFoundError)
                  case Failure(e) => { println(e); (false, dbError) }
                }
              }
              case false => (false, loggedInUserNotAdminError)
            }
          }
          case None => (false, userNotSignedInError)
        }
      }
      case None => (false, userNotSignedInError)
    }
  }

}
