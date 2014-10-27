package controllers

import play.api._
import play.api.mvc._
import dao.PasteDao
import org.bson.types.ObjectId
import models.PasteTO

object Application extends Controller {

  def index = Action {
	  val result = PasteDao.createPaste(new ObjectId("54485f901adee7b53870bacb"), "sample", "Sample Message", false)
	  Ok(views.html.index(result.getAs[String]("pasteId").get))
  }
  
  def displayPaste(pasteId: String) = Action {
	  val pasteQuery = PasteTO(null, pasteId, null, null, null, false)
	  val result = PasteDao.queryPasteByPasteId(pasteQuery)
	  Ok(views.html.paste(result))
  }

}