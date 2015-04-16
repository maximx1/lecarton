import models.{Paste, Pastes}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, Matchers, FlatSpec}

import scala.util.Success

class SampleTest extends FlatSpec with Matchers with BeforeAndAfter with MockFactory {
  "test1" should "select all users" in {
    val mockedPastes = mock[Pastes]
    val expected = Paste(None, "asdflkjh", 1, "Some title", "My content", false)
    (mockedPastes.insert _) expects(*) returning(Success(expected))
    val actual = mockedPastes.insert(expected).get
    actual.pasteId should be (expected.pasteId)
  }
}
