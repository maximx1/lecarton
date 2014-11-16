package controllers

import business.{ProfileManager, PasteManager}
import org.pegdown.PegDownProcessor
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import dao.{ProfileDao, AnonUserManager, PasteDao}
import org.bson.types.ObjectId
import models.{ProfileTO, PasteTO}

object Application extends Controller {

  val newPasteForm = Form(
    tuple(
        "title" -> text,
        "content" -> text,
        "isPublic" -> optional(text)
    )
  )

  val loginForm = Form(
    tuple(
      "username" -> text,
      "password" -> text
    )
  )

  val createProfileForm = Form(
    tuple(
      "username" -> text,
      "password1" -> text,
      "password2" -> text,
      "email" -> text
    )
  )
  
  def index = Action { implicit request =>
    val anonUserId: ObjectId = AnonUserManager.anonId.value //Hack to initialze anon user on new installs
	  Ok(views.html.index("Create A New Paste")(request.session))
  }
  
  def createPaste = Action { implicit request =>
    val (title, content, isPublic) = newPasteForm.bindFromRequest.get
    val sessionUserId = request.session.get("loggedInUser_id")
    val owner: ObjectId = if (sessionUserId.isEmpty) AnonUserManager.anonId.value else new ObjectId(sessionUserId.get)
    val result = (new PasteDao).createPaste(owner, title, content, isPublic.isEmpty)
    Ok(views.html.index(result.getAs[String]("pasteId").get)(request.session))
  }
  
  def displayPaste(pasteId: String) = Action { implicit request =>
    val sessionUserId = request.session.get("loggedInUser_id")
	  val pasteQuery = PasteTO(null, pasteId, null, null, null, false)
	  val result = (new PasteDao).queryPasteByPasteId(pasteQuery)
    var verifiedResult: PasteTO = result
    if(result != null && result.isPrivate) {
      if(sessionUserId.isEmpty) {
        verifiedResult = null
      }
      else if(sessionUserId.get != result.owner.toString){
        verifiedResult = null
      }
    }
    verifiedResult.content = (new PegDownProcessor).markdownToHtml(verifiedResult.content)
	  Ok(views.html.paste(verifiedResult)(request.session))
  }

  def search(searchScope: String, searchString: String) = Action { implicit request =>
    val sessionUserId = request.session.get("loggedInUser_id")
    val result = (new PasteManager).handlePasteSearch(searchScope, searchString, sessionUserId)
    Ok(views.html.searchResults(result, searchScope, searchString)(request.session))
  }

  def login = Action { implicit request =>
    Ok(views.html.login(null)(request.session))
  }

  def attemptLogin = Action { implicit request =>
    val (username, password) = loginForm.bindFromRequest.get
    val result = (new ProfileManager).attemptLogin(username, password)
    if(result == null) {
      Ok(views.html.login("Username/password not found")(request.session))
    }
    else {
      Redirect(routes.Application.index) withSession("loggedInUsername" -> result.username, "loggedInUser_id" -> result._id.toString)
    }
  }

  def logout = Action { implicit request =>
    Redirect(routes.Application.login) withNewSession
  }

  def createProfile = Action { implicit request =>
    Ok(views.html.createProfile(null)(request.session))
  }

  def attemptCreateProfile = Action { implicit request =>
    val (username, password1, password2, email) = createProfileForm.bindFromRequest.get
    val profileManager: ProfileManager = new ProfileManager()

    if(profileManager.userExists(username)) {
      Ok(views.html.createProfile("Username is already taken")(request.session))
    }
    else if(password1 != password2) {
      Ok(views.html.createProfile("Both passwords were not equal")(request.session))
    }
    else {
      val result = profileManager.createUser(username, password1, email)
      if(result == null) {
        Ok(views.html.createProfile("There was an unspecified error, you should try to log in again")(request.session))
      }
      else {
        Redirect(routes.Application.index) withSession("loggedInUsername" -> result.username, "loggedInUser_id" -> result._id.toString)
      }
    }
  }

  def loadPersonalProfile = Action { implicit request =>
    val sessionUsername = request.session.get("loggedInUsername")
    if(sessionUsername.isEmpty) {
      Redirect(routes.Application.login)
    }
    else {
      Redirect(routes.Application.loadProfile(sessionUsername.get))
    }
  }

  def loadProfile(username: String) = Action { implicit request =>
    val sessionUserId = request.session.get("loggedInUser_id")

    val profileQuery = ProfileTO(null, username, null, null)
    val profileResults = (new ProfileDao).queryUserProfileByUsername(profileQuery)

    if(profileResults != null) {
      val pasteManager: PasteManager = new PasteManager
      Ok(views.html.profile(profileResults, pasteManager.handlePasteSearch("profiles", username, sessionUserId))(request.session))
    }
    else {
      Ok(views.html.profile(profileResults, List.empty)(request.session))
    }
  }
}