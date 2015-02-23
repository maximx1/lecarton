package technology.walrath.m2h

import technology.walrath.m2h.data.LeCartonQueries

/**
 * Created by justin on 2/22/15.
 */
object M2h {
  def main(args: Array[String]) = {
    new LeCartonQueries().queryOwners().foreach(x => {
      println("======= " + x._id.toString + " =======")
      println(x.username)
      println(x.password)
      println(x.password)
      println()
    })
  }
}
