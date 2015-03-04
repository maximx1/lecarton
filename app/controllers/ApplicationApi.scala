package controllers

import business.PasteManager
import play.api.libs.json._
import play.api.mvc._
import play.api._
import play.api.libs.functional.syntax._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import com.github.tototoshi.play2.json4s.jackson._

case class UpdateVisibilityRequest(pasteId: String, newVisibility: Boolean)
case class UpdateVisibilityResponse(status: Boolean, message: String)

object ApplicationApi extends Controller with Json4s {

  implicit val format = DefaultFormats
  
  def updatePasteVisibility = Action(json).async { implicit request =>
    val sessionUserId = request.session.get("loggedInUser_id")
    val requestJson = request.body.extract[UpdateVisibilityRequest]
    val (status, message) = (new PasteManager).updatePasteVisibility(sessionUserId, requestJson.pasteId, requestJson.newVisibility)
    Ok(Extraction.decompose(UpdateVisibilityResponse(status, message)))
  }
}