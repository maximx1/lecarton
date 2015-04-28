package business

import scala.util.{Success, Failure}
import models.{Paste}

/**
 * Business object for paste management.
 * Created by justin on 11/3/14.
 */
class PasteManager extends PGDaoTrait {
  val ownerNotSignedInError = "Owner not signed in"
  val notFound = "Entry not found"
  val failUnspecified = "Unspecified error doing action"

  /**
   * Handles search situations using the scope and a search parameter.
   * @param searchScope The search scope to limit to.
   * @param searchString The search string to look for.
   * @return List of pastes matching everything.
   */
  def handlePasteSearch(searchScope: String, searchString: String, sessionUserId: Option[String]): List[Paste] = {
      if(searchScope == "titles") {
        return pastes.byTitle(searchString) match {
          case Success(x) => restrictAndFilterSearch(x, sessionUserId)
          case Failure(x) => { println(x); return List.empty }
        }
      }
      else if(searchScope == "profiles") {
        return profiles.byUsername(searchString) match {
          case Success(Some(x)) => {
            pastes.byOwner(x.id.get) match {
              case Success(x) => restrictAndFilterSearch(x, sessionUserId)
              case Failure(x) => { println(x); return List.empty }
            }
          }
          case Failure(x) => { println(x); return List.empty }
          case _ => List.empty
        }
      }
      else {
          return List.empty
      }
  }

  /**
   * Updates a paste's visibility.
   * @param userId The sessions user id.
   * @param pasteId The paste id from the request.
   * @param isPrivate The new visibility.
   * @return A tuple of a bool and a message.
   */
  def updatePasteVisibility(userId: Option[String], pasteId: String, isPrivate: Boolean): (Boolean, String) = {
    userId.map { x: String =>
      val result = pastes.byPasteId(pasteId)
      result match {
        case Success(Some(y)) if y.ownerId == x.toLong => {
          pastes.updateVisibility(y.copy(isPrivate = isPrivate)) match {
            case Success(z) => return (true, null)
            case Failure(e) => { println(e); (false, dbError) }
          }
        }
        case _ => (false, ownerNotSignedInError)
      }
    }.getOrElse((false, failUnspecified))
  }

  /**
   * Restricts a list of PasteTOs for a search behind the visibility filter and content box length
   * @param queryResults The results from the dao.
   * @param sessionUserId The optional session id
   * @return The restricted results.
   */
  def restrictAndFilterSearch(queryResults: List[Paste], sessionUserId: Option[String]): List[Paste] = {
    return queryResults.filter({ x =>
      var isNotRestricted = true
      if(sessionUserId.isEmpty) {
        if(x.isPrivate) {
          isNotRestricted = false
        }
      }
      else if(x.isPrivate && sessionUserId.get.toLong != x.ownerId) {
        isNotRestricted = false
      }
      isNotRestricted
    }).map(
      x => { x.copy(content = x.content.slice(0, 35)) }
    )
  }

  /**
   * Calls the model to get a count.
   * @return Count or -1 if there was a read issue.
   */
  def countPastes: Int = {
    pastes.size match {
      case Success(x) => x
      case Failure(x) => { println(x); -1 }
    }
  }

  /**
   * Handles creating a paste.
   * @param ownerId The id of the profile that owns this paste.
   * @param title The title of the paste.
   * @param content The content of the paste.
   * @param storePrivate The mark which determines if the paste is to be public or private.
   */
  def createPaste(ownerId: Long, title: String, content: String, storePrivate: Boolean): Paste = {
    pastes.insert(Paste(None, null, ownerId, title, content, storePrivate)) match {
      case Success(x) => x
      case Failure(x) => { println(x); null }
    }
  }

  /**
   * Looks up a paste by it's id.
   * @param pasteId The pasteId to look for.
   */
  def queryPasteByPasteId(pasteId: String): Option[Paste] = {
    pastes.byPasteId(pasteId) match {
      case Success(x) => x
      case Failure(x) => { println(x); None }
    }
  }
}