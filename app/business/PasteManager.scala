package business

import scala.util.{Success, Failure}
import dao.{PGDaoTrait, ProfileDao, PasteDao}
import models.{Pastes, Profiles, PasteTO}
import org.pegdown.PegDownProcessor

/**
 * Business object for paste management.
 * Created by justin on 11/3/14.
 */
class PasteManager extends PGDaoTrait {
  val ownerNotSignedInError = "Owner not signed in"
  val dbError = "Database Error"
  val notFound = "Entry not found"
  val failUnspecified = "Unspecified error doing action"

  /**
   * Handles search situations using the scope and a search parameter.
   * @param searchScope The search scope to limit to.
   * @param searchString The search string to look for.
   * @return List of pastes matching everything.
   */
  def handlePasteSearch(searchScope: String, searchString: String, sessionUserId: Option[String]): List[PasteTO] = {
      if(searchScope == "titles") {
        return pastes.byTitle(searchString) match {
          case Success(x) => restrictAndFilterSearch(x.map(y => PasteTO(y.id.get, y.pasteId, y.ownerId, y.title, y.content, y.isPrivate)), sessionUserId)
          case Failure(x) => { println(x); return List.empty }
        }
      }
      else if(searchScope == "profiles") {
        return profiles.byUsername(searchString) match {
          case Success(Some(x)) => {
            pastes.byOwner(x.id.get) match {
              case Success(x) => restrictAndFilterSearch(x.map(y => PasteTO(y.id.get, y.pasteId, y.ownerId, y.title, y.content, y.isPrivate)), sessionUserId)
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
            case _ => (false, notFound)
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
  def restrictAndFilterSearch(queryResults: List[PasteTO], sessionUserId: Option[String]): List[PasteTO] = {
    return queryResults.filter({ x =>
      var isNotRestricted = true
      if(sessionUserId.isEmpty) {
        if(x.isPrivate) {
          isNotRestricted = false
        }
      }
      else if(x.isPrivate && sessionUserId.get.toLong != x.owner) {
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
   * "hello https://github.com/maximx1 world" -> "hello <a href='https://github.com/maximx1'>https://github.com/maximx1</a> world"
   * @param content The string to convert.
   * @return The converted string.
   */
  def convertLinksToHTML(content: String): String = content.replaceAll("\\[(.*)\\]\\((.*)\\)", "<a href='$2'>$1</a>")

  /**
   * Converts content to markdown.
   * @param paste The paste with the original content.
   * @return The paste with the converted content.
   */
  def contentToMd(paste: Option[PasteTO]): Option[PasteTO] = {
      paste match {
        case Some(x) => Some(x.copy(content = (new PegDownProcessor).markdownToHtml(x.content)))
        case None => None
      }
  }
}
