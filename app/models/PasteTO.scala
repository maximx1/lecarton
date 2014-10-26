package models

import org.bson.types.ObjectId

case class PasteTO(
	var _id: ObjectId,
	var pasteId: String,
	var owner: ObjectId,
	var title: String,
	var content: String,
	var isPrivate: Boolean
)