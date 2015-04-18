package utils

import models.Paste
import org.pegdown.PegDownProcessor

/**
 * Common utilities for converting stuff.
 */
object ConversionUtils {

  /**
   * Converts content to markdown.
   * @param paste The paste with the original content.
   * @return The paste with the converted content.
   */
  def contentToMd(paste: Option[Paste]): Option[Paste] = {
    paste match {
      case Some(x) => Some(x.copy(content = (new PegDownProcessor).markdownToHtml(x.content)))
      case None => None
    }
  }
}
