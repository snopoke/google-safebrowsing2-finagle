package com.github.snopoke.safebrowsing2.config

import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.impl.conn.SchemeRegistryFactory
import org.apache.http.params.SyncBasicHttpParams
import com.github.snopoke.safebrowsing2.Helpers.isPortAvailable
import com.twitter.ostrich.admin.config.ServerConfig
import com.twitter.ostrich.admin.RuntimeEnvironment
import com.twitter.ostrich.stats.Stats
import com.twitter.util.FuturePool
import com.github.snopoke.safebrowsing2.Safebrowsing2ServiceServer
import net.google.safebrowsing2.db.Storage
import net.google.safebrowsing2.db.MSSQL
import net.google.safebrowsing2.db.HSQLDB
import net.google.safebrowsing2.db.MySQL
import net.google.safebrowsing2.db.DBI
import util.LiteDataSource

class Safebrowsing2Config extends ServerConfig[Safebrowsing2ServiceServer] {
  
  val updater = new UpdateServiceConfig()
  
  /**
   * The HTTP Port to run the service on 
   */
  var port: Int = 8080
  
  /**
   * The Google Safe Browsing API Key to use
   */
  var apikey = required[String]
  updater.apikey = apikey
  
  /**
   * Set useMac to true to request MAC signatures on all data
   */
  var useMac = false
  updater.useMac = useMac
  
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
    
  var storage: Storage = getStorage
  updater.storage = storage
    
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
        new HSQLDB(LiteDataSource.driverManager(databaseUrl, databaseUrl, databasePassword), databaseTablePrefix)
      }
      case "mysql" => new MySQL(LiteDataSource.driverManager(databaseUrl, databaseUrl, databasePassword), databaseTablePrefix)
      case "sqlserver" => new MSSQL(LiteDataSource.driverManager(databaseUrl, databaseUrl, databasePassword), databaseTablePrefix)
      case _ => new DBI(LiteDataSource.driverManager(databaseUrl, databaseUrl, databasePassword), databaseTablePrefix)
    }
  }

  def apply(_runtime: RuntimeEnvironment) = {
    require(port > 0, "Port must be specified and greater than 0")
    require(isPortAvailable(port) == true, "Already listening on port " + port)
    require(_runtime != null, "Need a runtime")
    require(name != null && name.length > 0, "Need a service name")

    runtime = _runtime

    updater()(runtime)
    
    new Safebrowsing2ServiceServer(this)
  }
}
