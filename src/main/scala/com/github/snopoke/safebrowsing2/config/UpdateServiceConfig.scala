package com.github.snopoke.safebrowsing2.config

import com.github.snopoke.safebrowsing2.Safebrowsing2UpdateService
import com.twitter.conversions.time.intToTimeableNumber
import com.twitter.ostrich.admin.config.ServerConfig
import com.twitter.ostrich.admin.RuntimeEnvironment
import com.twitter.util.Duration
import net.google.safebrowsing2.db.Storage
import net.google.safebrowsing2.db.HSQLDB
import util.LiteDataSource
import net.google.safebrowsing2.db.MySQL
import net.google.safebrowsing2.db.MSSQL
import net.google.safebrowsing2.db.DBI
import com.twitter.util.Config
import com.twitter.ostrich.admin.ServiceTracker

class UpdateServiceConfig extends Config[RuntimeEnvironment => Option[Safebrowsing2UpdateService]] {

  var apikey = required[String]
  
  /**
   * Optional list of blacklist names to update
   */
  var lists: Array[String] = optional[Array[String]]

  var useMac = required[Boolean]
  
  /**
   * The update period
   */
  var udpatePeriod: Duration = 1.minute
  
  var storage: Storage = required[Storage]

  def apply = { (runtime: RuntimeEnvironment) =>
    val updateService = new Safebrowsing2UpdateService(apikey, storage, useMac, lists, udpatePeriod)
    updateService.start()

    ServiceTracker.register(updateService)
    
    updateService
  }
}
