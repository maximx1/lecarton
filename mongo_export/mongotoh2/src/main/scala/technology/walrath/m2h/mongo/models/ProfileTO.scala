package technology.walrath.m2h.mongo.models

import org.bson.types.ObjectId

/**
 * Created by justin on 2/22/15.
 */
case class ProfileTO(
  var _id: ObjectId,
  var username: String,
  var password: String,
  var email: String
)
