package business

import dao.ProfileDao
import models.ProfileTO
import org.mindrot.jbcrypt.BCrypt

/**
 * Business logic for handling profiles.
 * Created by justin on 11/7/14.
 */
class ProfileManager {
  var profileDao: ProfileDao = new ProfileDao

  /**
   * Does a quick check to verify that the user does indeed exist.
   * @param username The username to search for.
   * @return true if exists.
   */
  def userExists(username: String): Boolean = profileDao.queryUserProfileByUsername(ProfileTO(null, username, null, null)) != null

  /**
   * Does a quick check to verify that the user does indeed exist.
   * @param userId The user's mongo id to search for.
   * @return true if exists.
   */
  def userExists(userId: Long): Boolean = profileDao.queryUserProfileById(ProfileTO(userId, null, null, null)) != null

  /**
   * Creates a new userafter checking that one doesn't already exist.
   * @param username The username to create with.
   * @param password The password to create with.
   * @param email The email to create with.
   * @return New ProfileTO if successful or null ortherwise.
   */
  def createUser(username: String, password: String, email: String): ProfileTO = {
    if(userExists(username)) {
      return null
    }
    else {
      return profileDao.createUserProfile(username, password, email)
    }
  }

  /**
   * Attempts to log in a profile.
   * @param username The username to log in with.
   * @param password The password to log in with.
   * @return The ProfileTO of the logged in user profile.
   */
  def attemptLogin(username: String, password: String): ProfileTO = {
    val result = profileDao.queryUserProfileByUsername(ProfileTO(null, username, null, null))
    if(result != null && BCrypt.checkpw(password, result.get.password)) {
      return result.get
    }
    return null
  }
}
