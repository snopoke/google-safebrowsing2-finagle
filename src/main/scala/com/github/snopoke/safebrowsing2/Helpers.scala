package com.github.snopoke.safebrowsing2
import java.net.ConnectException
import java.net.Socket
import java.net.URLDecoder

import scala.collection.mutable

object Helpers {
  def isPortAvailable(port: Int): Boolean = {
    require(port > 0, "Port must be greater than 0")
    import java.net.{ConnectException, Socket}

    try {
      val socket = new Socket("localhost", port)
      socket.close()
      false
    } catch {
      case e:ConnectException => true
      case e => false
    }

  }
  
  def convertToMap(query: String): Map[String, String] = {
    val map = mutable.Map[String, String]()
    val regex = """([a-zA-Z0-9\-]*=[^&]*)+""".r
    val param = regex findAllIn query
    param foreach { p =>
      val pair = p.split("=")
      if (pair.length == 2)
        map(pair(0)) = URLDecoder.decode(pair(1), "UTF-8")
    }
    map.toMap
  }
}
