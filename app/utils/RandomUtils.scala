package utils

import scala.util.Random

/**
 * Collection of functions based around random item generators.
 */
object RandomUtils {

  /**
   * Generates a sting of n length.
   */
  def generateRandomString(length: Int) = Random.alphanumeric.take(length).mkString
}
