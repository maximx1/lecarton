package controllers

import business.{ProfileManager, PasteManager}
import com.sun.xml.internal.bind.v2.TODO
import play.api._
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
        "content" -> text
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
    val (title, content) = newPasteForm.bindFromRequest.get
    val sessionUserId = request.session.get("loggedInUser_id")
    val owner: ObjectId = if (sessionUserId.isEmpty) AnonUserManager.anonId.value else new ObjectId(sessionUserId.get)
    val result = (new PasteDao).createPaste(owner, title, content, false)
    Ok(views.html.index(result.getAs[String]("pasteId").get)(request.session))
  }
  
  def displayPaste(pasteId: String) = Action { implicit request =>
	  val pasteQuery = PasteTO(null, pasteId, null, null, null, false)
	  val result = (new PasteDao).queryPasteByPasteId(pasteQuery)
	  Ok(views.html.paste(result)(request.session))
  }

  def search(searchScope: String, searchString: String) = Action { implicit request =>
    val result = (new PasteManager).handlePasteSearch(searchScope, searchString)
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

      val publicPasteQuery = PasteTO(null, null, profileResults._id, null, null, false)
      val pasteDao: PasteDao = new PasteDao
      var pasteResults = pasteDao.queryPastesOfOwner(publicPasteQuery)

      if (!sessionUserId.isEmpty && (profileResults._id eq sessionUserId.get)) {
        val privatePasteQuery = PasteTO(null, null, profileResults._id, null, null, true)
        pasteResults = pasteResults ::: pasteDao.queryPastesOfOwner(privatePasteQuery)
      }
      Ok(views.html.profile(profileResults, pasteResults)(request.session))
    }
    else {
      Ok(views.html.profile(profileResults, List.empty)(request.session))
    }
  }
}