package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import dao.PasteDao
import org.bson.types.ObjectId
import models.PasteTO

object Application extends Controller {

  val newPasteForm = Form(
	tuple(
	    "title" -> text,
	    "content" -> text
	)
  )
  
  def index = Action {
	  Ok(views.html.index("Create A New Paste"))
  }
  
  def createPaste = Action { implicit request =>
	val (title, content) = newPasteForm.bindFromRequest.get
	val result = PasteDao.createPaste(new ObjectId("54485f901adee7b53870bacb"), title, content, false)
	Ok(views.html.index(result.getAs[String]("pasteId").get))
  }
  
  def displayPaste(pasteId: String) = Action {
	  val pasteQuery = PasteTO(null, pasteId, null, null, null, false)
	  val result = PasteDao.queryPasteByPasteId(pasteQuery)
	  Ok(views.html.paste(result))
  }

}