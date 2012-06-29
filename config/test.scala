import com.twitter.conversions.time._
import com.twitter.logging.config._
import com.twitter.ostrich.admin.config._
import com.addynamo.juggler.config._
import org.mockito.Mockito
import com.twitter.util.FuturePool
import com.github.snopoke.safebrowsing2.finagle._

// test mode.
new Safebrowsing2Config {

  port = 4243
  apikey = "testkey"
  databaseUrl = "jdbc:hsqldb:mem:safebrowsing2"
  databaseUsername = "sa"
  databasePassword = ""

  loggers =
    new LoggerConfig {
      level = Level.ERROR
      handlers = new ConsoleHandlerConfig
    }
  
  override def getFuturePool = {
    FuturePool.immediatePool
  }
}
