package controllers

import test.core.BaseTestSpec
import controllers.Application._

/**
 * Created by justin on 3/13/15.
 */
class ApplicationTest extends BaseTestSpec {
  "Attempting to parse session user's admin status" should "yield true if a user option is populated with true" in {
    assert(parseIsAdminFromFormIfUserIsAdmin(Some("true")))
  }

  "Attempting to parse session user's admin status" should "yield false if a user option is populated with false" in {
    assert(!parseIsAdminFromFormIfUserIsAdmin(Some("false")))
  }

  "Attempting to parse session user's admin status" should "yield false if a user option is not populated" in {
    assert(!parseIsAdminFromFormIfUserIsAdmin(Some("false")))
  }
}
