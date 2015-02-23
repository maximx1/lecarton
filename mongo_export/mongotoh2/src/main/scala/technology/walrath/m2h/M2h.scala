package technology.walrath.m2h

import technology.walrath.m2h.data.{H2Push, LeCartonQueries}
import technology.walrath.m2h.h2.models.{ProfileTO, PasteTO}

/**
 * Created by justin on 2/22/15.
 */
object M2h {
  def main(args: Array[String]) = {
    val dao = new LeCartonQueries()
    val h2 = new H2Push()
    val oldProfiles = dao.queryOwners()
    oldProfiles.foreach(x => {
      val newProfile = ProfileTO(-1, x.username, x.password, x.email)
      if(x.username != "anon") {
        val newId = h2.createUserProfile(x.username, x.password, x.email)(H2Push.con).get
        val oldPastes = dao.queryPastesOfOwner(x)
        oldPastes.foreach(y => h2.createPaste(newId, y.pasteId, y.title, y.content, y.isPrivate)(H2Push.con))
      }
      else {
        val oldPastes = dao.queryPastesOfOwner(x)
        oldPastes.foreach(y => h2.createPaste(1, y.pasteId, y.title, y.content, y.isPrivate)(H2Push.con))
      }
    })
  }
}
