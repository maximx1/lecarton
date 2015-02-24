package technology.walrath.m2h.h2.models

/**
 * Created by justin on 2/22/15.
 */
case class PasteTO(
  var _id: Long,
  var pasteId: String,
  var owner: Long,
  var title: String,
  var content: String,
  var isPrivate: Boolean
)
