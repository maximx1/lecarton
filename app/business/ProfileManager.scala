package business

import dao.ProfileDao
import models.ProfileTO
import org.bson.types.ObjectId

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
  def userExists(userId: ObjectId): Boolean = profileDao.queryUserProfileById(ProfileTO(userId, null, null, null)) != null


}
