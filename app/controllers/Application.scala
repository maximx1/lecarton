package controllers

import business.{ProfileManager, PasteManager}
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import dao.{ProfileDao, PasteDao}
import models.{ProfileTO, PasteTO}
import business.PasteManager.contentToMd
import scala.concurrent.Future

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
	  Ok(views.html.index("")(request.session))
  }
  
  def createPaste = Action { implicit request =>
    val (title, content, isPublic) = newPasteForm.bindFromRequest.get
    val sessionUserId = request.session.get("loggedInUser_id")
    val owner: Long = if (sessionUserId.isEmpty) 1 else sessionUserId.get.toLong
    val storePrivate = if (owner == 1) false else isPublic.isEmpty
    val result = (new PasteDao).createPaste(owner, title, content, storePrivate)
    Ok(views.html.index(result.pasteId)(request.session))
  }
  
  def displayPaste(pasteId: String) = Action { implicit request =>
    val sessionUserId = request.session.get("loggedInUser_id")
	  val pasteQuery = PasteTO(-1, pasteId, -1, null, null, false)
	  val result = (new PasteDao).queryPasteByPasteId(pasteQuery)
    var verifiedResult: PasteTO = result match {
      case Some(x) => {
        sessionUserId match {
          case Some(y) => if (y.toLong != x.owner && x.isPrivate) null else contentToMd(Some(x)).get
          case None => if(x.isPrivate) pasteQuery else contentToMd(Some(x)).get
        }
      }
      case None => null
    }
    if (verifiedResult != null && verifiedResult._id == -1) {
      Redirect(routes.Application.login) withNewSession
    } else {
      Ok(views.html.paste(verifiedResult)(request.session))
    }
  }

  def search(searchScope: String, searchString: String) = Action.async { implicit request =>
    val sessionUserId = request.session.get("loggedInUser_id")
    val result: Future[List[PasteTO]] = Future {(new PasteManager).handlePasteSearch(searchScope, searchString, sessionUserId)}

    result.map {x =>
      Ok(views.html.searchResults(x, searchScope, searchString)(request.session))
    }
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

    val profileQuery = ProfileTO(-1, username, null, null, false)
    val profileResults = (new ProfileDao).queryUserProfileByUsername(profileQuery)

    profileResults match {
      case Some(x) => {
        val pasteManager: PasteManager = new PasteManager
        Ok(views.html.profile(x, pasteManager.handlePasteSearch("profiles", username, sessionUserId))(request.session))
      }
      case None => Ok(views.html.profile(null, List.empty)(request.session))
    }
  }
  
  def loadAdmin = Action { implicit request =>

    Ok(views.html.admin((new PasteDao).pasteCount, (new ProfileDao).profileCount)(request.session))
  }
}