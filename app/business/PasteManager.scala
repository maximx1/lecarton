package business

import dao.PasteDao
import models.PasteTO

/**
 * Business object for paste management.
 * Created by justin on 11/3/14.
 */
object PasteManager {
  /**
   * Handles search situations using the scope and a search parameter.
   * @param searchScope The search scope to limit to.
   * @param searchString The search string to look for.
   * @return List of pastes matching everything.
   */
  def handlePasteSearch(searchScope: String, searchString: String): List[PasteTO] = {
      if(searchScope == "titles") {
          val query = PasteTO(null, null, null, searchString, null, isPrivate = false)
          return PasteDao.queryPasteByTitle(query).map(
            x => { x.content = x.content.slice(0, 35);x }
          )
      }
      else {
          return List.empty
      }
  }
}
