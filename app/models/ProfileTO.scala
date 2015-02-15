package models

import org.bson.types.ObjectId

case class ProfileTO(
	var _id: Long,
	var username: String,
	var password: String,
	var email: String
)