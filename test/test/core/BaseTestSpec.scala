package test.core

import org.scalatest.mock.MockitoSugar
import org.mockito.mock
import org.scalatest.{BeforeAndAfter, Matchers, FlatSpec}

/**
 * Standard base trait for main tests.
 * Created by justin on 3/13/15.
 */
trait BaseTestSpec extends FlatSpec with Matchers with BeforeAndAfter with MockitoSugar