import com.twitter.conversions.storage._
import com.twitter.conversions.time._
import com.twitter.logging.config._
import com.twitter.ostrich.admin.config._
import com.addynamo.juggler.config._

// production mode.
new JugglerConfig {

  port = @port@
  redirectLocation = @redirectLocation@
  redirect = @redirect@

  threadPoolCoreSize = 10
  threadPoolMaxSize = 1000
  
  maxConnectionsPerRoute = 100
  maxConnectionsTotal = 500

  // Ostrich http admin port.  Curl this for stats, etc
  admin.httpPort = 9990

  admin.statsNodes = new StatsConfig {
    reporters = new TimeSeriesCollectorConfig
  }

  loggers =
    new LoggerConfig {
      level = Level.INFO
      handlers = new FileHandlerConfig {
        filename = "/var/log/juggler/juggler.log"
        roll = Policy.Daily
      }
    } :: new LoggerConfig {
      level = Level.INFO
      node = "com.addynamo"
    }
}
