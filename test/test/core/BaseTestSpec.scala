package test.core

import dao.BaseDaoTrait
import models.{Profiles, Pastes}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, Matchers, FlatSpec}

/**
 * Standard base trait for main tests.
 * Created by justin on 3/13/15.
 */
trait BaseTestSpec extends FlatSpec with Matchers with BeforeAndAfter with MockFactory {
  trait TestDaoTrait extends BaseDaoTrait {
    override val pastes: Pastes = mock[Pastes]
    override val profiles: Profiles = mock[Profiles]
  }
}