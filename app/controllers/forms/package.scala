package controllers

import play.api.data._
import play.api.data.Forms._

/**
 * Created by justin on 3/13/15.
 */
package object forms {
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
      "email" -> text,
      "isAdmin" -> optional(text)
    )
  )
}
