package com.github.snopoke.safebrowsing2

import com.twitter.conversions.time.intToTimeableNumber
import com.twitter.ostrich.admin.{Service => OstrichService}
import com.github.snopoke.safebrowsing2.config.Safebrowsing2Config
import net.google.safebrowsing2.SafeBrowsing2

class Safebrowsing2ServiceServer(config: Safebrowsing2Config) extends OstrichService with Safebrowsing2Service {
  require(config != null, "Config must be specified")
  require(config.port > 0, "Need a port to listen on")
  require(config.name != null && config.name.length > 0, "Need a service name")
  require(config.futurePool != null, "Need an Future Pool")

  override val port = config.port
  override val name = config.name
  override val futurePool = config.futurePool
  override val useMac = config.useMac
  override val sb2 = new SafeBrowsing2(config.apikey, config.storage)

  override def start() {
    log.info("Starting server %s on port %d", name, port)
    server = Some(serverSpec.build(service))
  }

  override def shutdown() {
    log.debug("Shutdown requested")
    server match {
      case None =>
        log.warning("Server not started, refusing to shutdown")
      case Some(server) =>
        try {
          server.close(0.seconds)
          log.info("Shutdown complete")
        } catch {
          case e: Exception =>
            log.error(e, "Error shutting down server %s listening on port %d", name, port)
        }
    } // server match
  }

  override def reload() {
    log.info("Reload requested but not supported")
  }
}
