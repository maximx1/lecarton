package business

import dao.{ProfileDao, PasteDao}
import models.{ProfileTO, PasteTO}

/**
 * Business object for paste management.
 * Created by justin on 11/3/14.
 */
class PasteManager {

  var pasteDao: PasteDao = new PasteDao
  var profileDao: ProfileDao = new ProfileDao

  /**
   * Handles search situations using the scope and a search parameter.
   * @param searchScope The search scope to limit to.
   * @param searchString The search string to look for.
   * @return List of pastes matching everything.
   */
  def handlePasteSearch(searchScope: String, searchString: String, sessionUserId: Option[String]): List[PasteTO] = {
      if(searchScope == "titles") {
          val publicQuery = PasteTO(null, null, null, searchString, null, false)
          val privateQuery = PasteTO(null, null, null, searchString, null, true)
          val queryResults = pasteDao.queryPasteByTitle(publicQuery) ::: pasteDao.queryPasteByTitle(privateQuery)
          return restrictAndFilterSearch(queryResults, sessionUserId)
      }
      else if(searchScope == "profiles") {
        val profileQuery = ProfileTO(null, searchString, null, null)
        val profileData = profileDao.queryUserProfileByUsername(profileQuery)
        if (profileData == null) {
          return List.empty
        }
        val publicQuery = PasteTO(null, null, profileData._id, null, null, false)
        val privateQuery = PasteTO(null, null, profileData._id, null, null, true)
        val queryResults = pasteDao.queryPastesOfOwner(publicQuery) ::: pasteDao.queryPastesOfOwner(privateQuery)
        return restrictAndFilterSearch(queryResults, sessionUserId)
      }
      else {
          return List.empty
      }
  }

  /**
   * Restricts a list of PasteTOs for a search behind the visibility filter and content box length
   * @param queryResults The results from the dao.
   * @param sessionUserId The optional session id
   * @return The restricted results.
   */
  def restrictAndFilterSearch(queryResults: List[PasteTO], sessionUserId: Option[String]): List[PasteTO] = {
    return queryResults.filter({ x =>
      var isNotRestricted = true
      if(sessionUserId.isEmpty) {
        if(x.isPrivate) {
          isNotRestricted = false
        }
      }
      else if(x.isPrivate && sessionUserId.get != x.owner.toString) {
        isNotRestricted = false
      }
      isNotRestricted
    }).map(
      x => { x.content = x.content.slice(0, 35);x }
    )
  }
}
