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
          var queryResults = pasteDao.queryPasteByTitle(publicQuery)
          val privateQuery = PasteTO(null, null, null, searchString, null, true)
          queryResults = queryResults ::: pasteDao.queryPasteByTitle(privateQuery)

          return queryResults.filter({x =>
            var isNotRestricted = true
            if(sessionUserId.isEmpty) {
              if(x.isPrivate) {
                isNotRestricted = false
              }
            }
            else if(sessionUserId.get != x.owner.toString) {
              isNotRestricted = false
            }
            isNotRestricted
          }).map(x => {
              x.content = x.content.slice(0, 35);x }
          )
      }
      else if(searchScope == "profiles") {
        val profileQuery = ProfileTO(null, searchString, null, null)
        val profileData = profileDao.queryUserProfileByUsername(profileQuery)
        if (profileData == null) {
          return List.empty
        }
        val pasteQuery = PasteTO(null, null, profileData._id, null, null, isPrivate = false)
        return pasteDao.queryPastesOfOwner(pasteQuery)
      }
      else {
          return List.empty
      }
  }
}
