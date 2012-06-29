package com.github.snopoke.safebrowsing2.finagle

import com.twitter.ostrich.admin.PeriodicBackgroundProcess
import com.twitter.util.Duration
import net.google.safebrowsing2.db.Storage
import net.google.safebrowsing2.SafeBrowsing2
import com.twitter.logging.Logger
import com.twitter.logging.Level

class Safebrowsing2UpdateService(apikey: String,
  storage: Storage,
  useMac: Boolean,
  lists: Option[Array[String]],
  period: Duration) extends PeriodicBackgroundProcess("SafeBrowsing2 Update Process", period, false) {

  private val log = Logger.get(getClass.getName)
  private val statsLog = Logger.get("dbstats")

  private val sb2 = new SafeBrowsing2(apikey, storage)

  override def periodic = {
    try {
      val secondsToWaitBeforeNextUpdate = sb2.update(lists.orNull, false, useMac)
      log.info("Update success. Next update in %d seconds", secondsToWaitBeforeNextUpdate)
    } catch {
      case e => log.error(e, "Error running update process")
    }

    if (statsLog.isLoggable(Level.INFO)){
      val stats = storage.getDatabaseStats
      statsLog.info("=====> Database Statistics <=====")
      stats.foreach{
        case (k,v) => statsLog.info(k + " -> " + v)
      }
      statsLog.info("=================================")
    }
  }
}