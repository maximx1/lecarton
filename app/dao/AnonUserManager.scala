package dao

import business.ProfileManager
import models.{LazyCache, ProfileTO}
import org.bson.types.ObjectId

/**
 * The master anon user account owner.
 * Created by justin on 11/4/14.
 */
object AnonUserManager {
  var profileManager = new ProfileManager()
  var profileDao = new ProfileDao()

  /**
   * Gets the anon user's objectId
   */
  val anonId = LazyCache[ObjectId] {
    if (profileManager.userExists("anon")) {
      val query = ProfileTO(null, "anon", null, null)
      profileDao.queryUserProfileByUsername(query)._id
    }
    else {
      profileManager.createUser("anon", PasteDao.generateRandomString(15), "anon@sample.com")._id
    }
  }
}
