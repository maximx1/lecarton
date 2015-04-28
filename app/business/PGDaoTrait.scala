package business

import models.{Pastes, Profiles}

/**
 * Created by justin on 4/15/15.
 */
trait PGDaoTrait extends BaseDaoTrait {
  override val pastes: Pastes = new Pastes
  override val profiles: Profiles = new Profiles
  val dbError = "Database Error"
}
