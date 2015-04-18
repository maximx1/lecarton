package business

import models.{Pastes, Profiles}

/**
 * Created by justin on 4/15/15.
 */
trait BaseDaoTrait {
  def pastes: Pastes
  def profiles: Profiles
}
