import com.twitter.conversions.time._
import com.twitter.logging.config._
import com.twitter.ostrich.admin.config._
import com.addynamo.juggler.config._
import org.mockito.Mockito
import com.twitter.util.FuturePool

// test mode.
new Safebrowsing2Config {

  port = 4243
  apikey = "testkey"

  loggers =
    new LoggerConfig {
      level = Level.ERROR
      handlers = new ConsoleHandlerConfig
    }
  
  override def getFuturePool = {
    FuturePool.immediatePool
  }
}
