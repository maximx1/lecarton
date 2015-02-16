package models

case class PasteTO(
	var _id: Long,
	var pasteId: String,
	var owner: Long,
	var title: String,
	var content: String,
	var isPrivate: Boolean
)