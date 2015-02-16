package models

case class ProfileTO(
	var _id: Long,
	var username: String,
	var password: String,
	var email: String
)