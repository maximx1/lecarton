package business

import dao.{ProfileDao, PasteDao}
import models.{ProfileTO, PasteTO}
import org.bson.types.ObjectId

/**
 * Business object for paste management.
 * Created by justin on 11/3/14.
 */
class PasteManager {

  var pasteDao: PasteDao = new PasteDao
  var profileDao: ProfileDao = new ProfileDao
  val ownerNotSignedInError = "Owner not signed in"

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
   * Updates a paste's visibility.
   * @param userId The sessions user id.
   * @param pasteId The paste id from the request.
   * @return A tuple of a bool and a message.
   */
  def updatePasteVisibility(userId: Option[String], pasteId: String, isPrivate: Boolean): (Boolean, String) = {
    if(!userId.isEmpty) {
      val pasteQuery: PasteTO = PasteTO(null, pasteId, null, null, null, false)
      val result = pasteDao.queryPasteByPasteId(pasteQuery)

      if(userId.get == result.owner.toString) {
        result.isPrivate = isPrivate
        pasteDao.updatePaste(result)
        return (true, null)
      }
    }

    return (false, ownerNotSignedInError)
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

/**
 * Paste manager helper object with some additional utilities.
 */
object PasteManager {

  /**
   * Converts part of a string to an <a />
   *  "hello https://github.com/maximx1 world" -> "hello <a href='https://github.com/maximx1'>https://github.com/maximx1</a> world"
   * @param content The string to convert.
   * @return The converted string.
   */
  def convertLinksToHTML(content: String): String = content.replaceAll("\\[(.*)\\]\\((.*)\\)", "<a href='$2'>$1</a>")
}
