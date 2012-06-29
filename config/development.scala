import com.twitter.conversions.storage._
import com.twitter.conversions.time._
import com.twitter.logging.config._
import com.twitter.ostrich.admin.config._
import com.addynamo.juggler.config._

// development mode.
new JugglerConfig {

  port = 4242
  redirectLocation = "http://www.google.com"
  redirect = false
  
  threadPoolCoreSize = 10
  threadPoolMaxSize = 50

  admin.httpPort = 9990

  admin.statsNodes = new StatsConfig {
    reporters = new TimeSeriesCollectorConfig
  }
  
  loggers =
    new LoggerConfig {
      level = Level.DEBUG
      handlers = new ConsoleHandlerConfig
    } :: new LoggerConfig {
      level = Level.DEBUG
      node = "com.addynamo"
    }
}
