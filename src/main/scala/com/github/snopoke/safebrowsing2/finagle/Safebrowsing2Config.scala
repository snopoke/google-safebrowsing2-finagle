package com.github.snopoke.safebrowsing2.finagle

import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

import com.github.snopoke.safebrowsing2.finagle.Helpers.isPortAvailable
import com.twitter.conversions.time.intToTimeableNumber
import com.twitter.ostrich.admin.config.ServerConfig
import com.twitter.ostrich.admin.RuntimeEnvironment
import com.twitter.ostrich.admin.ServiceTracker
import com.twitter.ostrich.stats.Stats
import com.twitter.util.Duration
import com.twitter.util.FuturePool

import net.google.safebrowsing2.db.DBI
import net.google.safebrowsing2.db.HSQLDB
import net.google.safebrowsing2.db.MSSQL
import net.google.safebrowsing2.db.MySQL
import net.google.safebrowsing2.db.Storage
import util.LiteDataSource

class Safebrowsing2Config extends ServerConfig[Safebrowsing2ServiceServer] {
  
  /**
   * The HTTP Port to run the service on 
   */
  var port: Int = 8080
  
  /**
   * The Google Safe Browsing API Key to use
   */
  var apikey = required[String]

  /**
   * Set useMac to true to request MAC signatures on all data
   */
  var useMac = false
  
  /**
   * Optional list of blacklist names to update
   */
  var lists = optional[Array[String]]
  
  /**
   * The update period
   */
  var udpatePeriod: Duration = 1.minute
  
  var name: String = "Safebrowsing2Service"
    
  /**
   * Supported databases are HSQLDB, MySQL and MS SQL
   */
  var databaseUrl = "jdbc:hsqldb:mem:safebrowsing2"
  var databaseUsername = "sa"
  var databasePassword = ""
    
  /**
   * The table prefix to use for the database tables
   */
  var databaseTablePrefix = "gsb2_"
    
  lazy val storage: Storage = getStorage
    
  var threadPoolCoreSize = 0
  var threadPoolMaxSize = 1000
  var threadKeepAliveTime = 30L
  
  lazy val futurePool: FuturePool = getFuturePool

  var runtime: RuntimeEnvironment = null

  def getFuturePool: FuturePool = {
    val executor = new ThreadPoolExecutor(threadPoolCoreSize, threadPoolMaxSize,
      threadKeepAliveTime, TimeUnit.SECONDS,
      new SynchronousQueue[Runnable]());

    Stats.addGauge("threadpool_pool_size") { executor.getPoolSize() }

    FuturePool(executor)
  }
  
  def getStorage = {
    val protocol = databaseUrl.split(":").drop(1).head
    protocol match {
      case "hsqldb" => {
        if (databaseUrl.contains(":mem:")) {
          Console.withOut(Console.err) { println("WARNING: in memory database being used. Data will be lost on shutdown") }
        }
        new HSQLDB(LiteDataSource.driverManager(databaseUrl, databaseUsername, databasePassword), databaseTablePrefix)
      }
      case "mysql" => new MySQL(LiteDataSource.driverManager(databaseUrl, databaseUsername, databasePassword), databaseTablePrefix)
      case "sqlserver" => new MSSQL(LiteDataSource.driverManager(databaseUrl, databaseUsername, databasePassword), databaseTablePrefix)
      case _ => new DBI(LiteDataSource.driverManager(databaseUrl, databaseUsername, databasePassword), databaseTablePrefix)
    }
  }

  def apply(_runtime: RuntimeEnvironment) = {
    require(port > 0, "Port must be specified and greater than 0")
    require(isPortAvailable(port) == true, "Already listening on port " + port)
    require(_runtime != null, "Need a runtime")
    require(name != null && name.length > 0, "Need a service name")

    runtime = _runtime

    val updateService = new Safebrowsing2UpdateService(apikey, storage, useMac, lists, udpatePeriod)
    updateService.start()

    ServiceTracker.register(updateService)
    
    new Safebrowsing2ServiceServer(this)
  }
}
