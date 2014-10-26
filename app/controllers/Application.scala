package controllers

import play.api._
import play.api.mvc._
import dao.PasteDao
import org.bson.types.ObjectId

object Application extends Controller {

  def index = Action {
      PasteDao.createPaste(new ObjectId("54485f901adee7b53870bacb"), "sample", "Sample Message", false)
    Ok(views.html.index("Your new application is ready."))
  }

}