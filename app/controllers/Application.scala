package controllers

import business.{ProfileManager, PasteManager}
import play.api.mvc._
import models.Paste
import utils.ConversionUtils.contentToMd
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import controllers.forms._

object Application extends Controller {
  def index = Action { implicit request =>
	  Ok(views.html.index("")(request.session))
  }
  
  def createPaste = Action { implicit request =>
    val (title, content, isPublic) = newPasteForm.bindFromRequest.get
    val sessionUserId = request.session.get("loggedInUser_id")
    val owner: Long = if (sessionUserId.isEmpty) 1 else sessionUserId.get.toLong
    val storePrivate = if (owner == 1) false else isPublic.isEmpty
    val result = (new PasteManager).createPaste(owner, title, content, storePrivate)
    Ok(views.html.index(if(result != null) result.pasteId else "Unspecified error creating paste")(request.session))
  }
  
  def displayPaste(pasteId: String) = Action { implicit request =>
    val sessionUserId = request.session.get("loggedInUser_id")
	  val result = (new PasteManager).queryPasteByPasteId(pasteId)
    val verifiedResult: Paste = result match {
      case Some(x) => {
        sessionUserId match {
          case Some(y) => if (y.toLong != x.ownerId && x.isPrivate) null else contentToMd(Some(x)).get
          case None => if(x.isPrivate) Paste(Some(-1), pasteId, -1, null, null, false) else contentToMd(Some(x)).get
        }
      }
      case None => null
    }
    if(verifiedResult == null) {
      Ok(views.html.error404()(request.session))
    }
    else {
      if (verifiedResult.id.get == -1) {
        Redirect(routes.Application.login) withNewSession
      } else {
        Ok(views.html.paste(verifiedResult)(request.session))
      }
    }
  }

  def search(searchScope: String, searchString: String) = Action.async { implicit request =>
    val sessionUserId = request.session.get("loggedInUser_id")
    val result: Future[List[Paste]] = Future {(new PasteManager).handlePasteSearch(searchScope, searchString, sessionUserId)}

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
      Redirect(routes.Application.index) withSession(
        "loggedInUsername" -> result.username,
        "loggedInUser_id" -> result.id.get.toString,
        "loggedInUserIsAdmin" -> result.isAdmin.toString
      )
    }
  }

  def logout = Action { implicit request =>
    Redirect(routes.Application.login) withNewSession
  }

  def createProfile = Action { implicit request =>
    Ok(views.html.createProfile(null, parseIsAdminFromFormIfUserIsAdmin(request.session.get("loggedInUserIsAdmin")))(request.session))
  }

  def attemptCreateProfile = Action { implicit request =>
    val (username, password1, password2, email, isAdmin) = createProfileForm.bindFromRequest.get
    val isUserAdmin = request.session.get("loggedInUserIsAdmin")
    val profileManager: ProfileManager = new ProfileManager()

    profileManager.userExists(username) match {
      case Some(x) => {
        if (x) {
          Ok(views.html.createProfile("Username is already taken", parseIsAdminFromFormIfUserIsAdmin(isUserAdmin))(request.session))
        }
        else if (password1 != password2) {
          Ok(views.html.createProfile("Both passwords were not equal", parseIsAdminFromFormIfUserIsAdmin(isUserAdmin))(request.session))
        }
        else {

          val result = profileManager.createUser(username, password1, email, !isAdmin.isEmpty)
          if (result == null) {
            Ok(views.html.createProfile("There was an unspecified error, you should try to log in again", parseIsAdminFromFormIfUserIsAdmin(isUserAdmin))(request.session))
          }
          else {
            Redirect(routes.Application.login)
          }
        }
      }
      case _ => Ok(views.html.createProfile("There was an unspecified error, you should try to log in again", parseIsAdminFromFormIfUserIsAdmin(isUserAdmin))(request.session))
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

      (new ProfileManager).queryUserProfileByUsername(username) match {
      case Some(x) => {
        val pasteManager: PasteManager = new PasteManager
        Ok(views.html.profile(x, pasteManager.handlePasteSearch("profiles", username, sessionUserId))(request.session))
      }
      case None => Ok(views.html.profile(null, List.empty)(request.session))
    }
  }
  
  def loadAdmin = Action { implicit request =>
    val isUserAdmin = request.session.get("loggedInUserIsAdmin")
    if(parseIsAdminFromFormIfUserIsAdmin(isUserAdmin)) {
      Ok(views.html.admin((new PasteManager).countPastes, (new ProfileManager).countProfiles, (new ProfileManager).profiles.all.getOrElse(List.empty))(request.session))
    }
    else {
      Ok(views.html.error404()(request.session))
    }
  }
  
  def parseIsAdminFromFormIfUserIsAdmin(isUserAdmin: Option[String]): Boolean = isUserAdmin match {
    case Some(x) => x == "true"
    case None => false
  }

  def loadAccount = Action { implicit request =>
    request.session.get("loggedInUser_id") match {
      case Some(id) => Ok(views.html.account(updateAccountForm.fill(AccountFormData("", "", "")))(request.session))
      case None => Redirect(routes.Application.login)
    }
  }

  def attemptAccountUpdate = Action { implicit request =>
    updateAccountForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.account(formWithErrors)(request.session)),
      form => form match {
        case (x) if x.newPassword1 == x.oldPassword => {
          //(new ProfileManager).
          Ok(views.html.account(updateAccountForm.fill(AccountFormData("", "", "")).withGlobalError("Successfully changes password"))(request.session))
        }
        case _ => BadRequest(views.html.account(updateAccountForm.fill(form).withGlobalError("New Passwords don't match!"))(request.session))
      }
    )
  }
}