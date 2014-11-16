package controllers

import play.api.libs.json._
import play.api.mvc._
import play.api._
import play.api.libs.functional.syntax._

case class UpdateVisibilityRequest(pasteId: String, newVisibility: Boolean)

object ApplicationApi extends Controller {

  implicit val updateVisibilityRequestRead: Reads[UpdateVisibilityRequest] = (
    (JsPath \ "pasteId").read[String] and
    (JsPath \ "newVisibility").read[Boolean]
  )(UpdateVisibilityRequest.apply _)


  def updatePasteVisibility = Action(parse.json) { implicit request =>
    val sessionUserId = request.session.get("loggedInUser_id")
    request.body.validate[UpdateVisibilityRequest].map {
      case s: UpdateVisibilityRequest =>
        val (pasteId, newVisibility) = UpdateVisibilityRequest.unapply(s).get

        val response = Json.obj()
        Ok(response)
    }.recoverTotal{ e =>
      BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(e)))
    }
  }
}