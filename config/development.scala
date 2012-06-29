import com.twitter.conversions.storage._
import com.twitter.conversions.time._
import com.twitter.logging.config._
import com.twitter.ostrich.admin.config._
import com.github.snopoke.safebrowsing2.config._

// development mode.
new Safebrowsing2Config {

  port = 8080
  apikey = "ABQIAAAABYkmAAm4XQxOniSBu7POOBSi8x_kWneXQyiBH-s1nM-mcx8RKg"

  useMac = false
  databaseUrl = "jdbc:hsqldb:mem:safebrowsing2"
  databaseUsername = "sa"
  databasePassword = ""
  databaseTablePrefix = "gsb2_"
    
  threadPoolCoreSize = 0
  threadPoolMaxSize = 1000
  threadKeepAliveTime = 30L
  
  admin.httpPort = 9990

  admin.statsNodes = new StatsConfig {
    reporters = new TimeSeriesCollectorConfig
  }
  
  loggers =
    new LoggerConfig {
      level = Level.DEBUG
      handlers = new ConsoleHandlerConfig
    }
}
