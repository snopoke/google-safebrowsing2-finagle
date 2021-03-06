import com.twitter.conversions.storage._
import com.twitter.conversions.time._
import com.twitter.logging.config._
import com.twitter.ostrich.admin.config._
import com.github.snopoke.safebrowsing2.finagle._

// production mode.
new Safebrowsing2Config {

  port = @port@
  apikey = "@apikey@"

  useMac = false
  databaseUrl = "@jdbcurl@"
  databaseUsername = "@jdbcuser@"
  databasePassword = "@jdbcpasswd@"
  databaseTablePrefix = "@tableprefix@"
    
  threadPoolCoreSize = 0
  threadPoolMaxSize = 1000
  threadKeepAliveTime = 10L

  // Ostrich http admin port.  Curl this for stats, etc
  admin.httpPort = 9990

  admin.statsNodes = new StatsConfig {
    reporters = new TimeSeriesCollectorConfig
  }

  loggers =
    new LoggerConfig {
      level = Level.WARNING
      handlers = new FileHandlerConfig {
        filename = "/var/log/safebrowsing/safebrowsing.log"
        roll = Policy.Daily
      }
    } :: new LoggerConfig {
      level = Level.INFO
      node = "com.github.snopoke"
    } :: new LoggerConfig {
      level = Level.OFF
	  node = "dbstats"
    }
}
