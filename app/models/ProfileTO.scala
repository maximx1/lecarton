package models

import org.bson.types.ObjectId

case class ProfileTO(
	var _id: ObjectId,
	var username: String,
	var password: String,
	var email: String
)