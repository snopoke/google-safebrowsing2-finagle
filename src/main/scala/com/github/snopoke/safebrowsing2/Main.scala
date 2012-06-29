package com.github.snopoke.safebrowsing2

import com.twitter.logging.Logger
import com.twitter.ostrich.admin.RuntimeEnvironment
import com.twitter.ostrich.admin.ServiceTracker

object Main {
  val log = Logger.get(getClass)

  def main(args: Array[String]) {
    val runtime = RuntimeEnvironment(this, args)
    val server = runtime.loadRuntimeConfig[Safebrowsing2ServiceServer]
    try {
      log.info("Starting service")
      server.start()
    } catch {
      case e: Exception =>
        log.error(e, "Failed starting service, exiting")
        ServiceTracker.shutdown()
        sys.exit(1)
    }
  }
}
