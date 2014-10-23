package controllers

import play.api._
import play.api.mvc._
import dao.PasteDao

object Application extends Controller {

  def index = Action {
      PasteDao.createPaste()
    Ok(views.html.index("Your new application is ready."))
  }

}