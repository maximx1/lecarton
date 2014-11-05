package controllers

import play.api.mvc.{Action, Controller}

/**
 * Created by justin on 11/4/14.
 */
object ApplicationFail extends Controller {
  /**
   * Simple fail routine for missing search term.
   * @param searchScope The scope to search through.
   * @return Search results page view.
   */
  def searchFail(searchScope: String) = Action { implicit request =>
     Ok(views.html.searchResults(List.empty, searchScope, ""))
  }
}
