package com.github.snopoke.safebrowsing2

import java.net.InetSocketAddress
import org.apache.http.client.HttpClient
import org.jboss.netty.handler.codec.http.HttpVersion
import com.twitter.finagle.builder.Server
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.Http
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.Response
import com.twitter.finagle.http.RichHttp
import com.twitter.finagle.stats.OstrichStatsReceiver
import com.twitter.finagle.Service
import com.twitter.finagle.SimpleFilter
import com.twitter.logging.Logger
import com.twitter.util.FuturePool
import com.twitter.util.Future
import net.google.safebrowsing2.SafeBrowsing2
import com.twitter.finagle.http.service.RoutingService
import org.jboss.netty.buffer.ChannelBuffers
import com.twitter.finagle.http.MediaType
import org.jboss.netty.util.CharsetUtil.UTF_8
import com.twitter.ostrich.stats.Stats
import com.twitter.finagle.http.Status
import org.jboss.netty.handler.codec.http.HttpMethod
import java.net.URLDecoder

trait Safebrowsing2Service {
  val port: Int
  val name: String
  val futurePool: FuturePool
  val sb2: SafeBrowsing2
  val useMac: Boolean
  var server: Option[Server] = None

  val log = Logger.get(getClass)

  // Don't initialize until after mixed in by another class
  lazy val serverSpec = ServerBuilder()
    .codec(RichHttp[Request](Http()))
    .bindTo(new InetSocketAddress(port))
    .name(name)
    .reportTo(new OstrichStatsReceiver)

  val exceptionHanlder = new SimpleFilter[Request, Response] {
    def apply(request: Request, service: Service[Request, Response]) = {
      try {
        service(request)
      } catch {
        case error =>
          log.error(error, "Server error")
          Future.value(Response(HttpVersion.HTTP_1_1, Status.InternalServerError))
      }
    }
  }

  lazy val serve = RoutingService.byPath {
    case "/lookup" => new Service[Request, Response] {
      def apply(request: Request): Future[Response] = {
        val client = request.getParam("client")
        val requestApiKey = request.getParam("apikey")
        val requestAppver = request.getParam("appver")
        val requestPver = request.getParam("pver")
        val url = request.getParam("url")
        if (!sb2.pver.equals(requestPver)) {
          Future.value(Response(HttpVersion.HTTP_1_1, Status.BadRequest))
        }
        // TODO: check apikey

        val badResponse = Response(request)
        badResponse.status = Status.BadRequest
        val toCheck = request.method match {
          case HttpMethod.GET => {
            if (url == null) {
              return Future(badResponse)
            }
            try {
              Array(URLDecoder.decode(url, "UTF-8"))
            } catch {
              case e => return Future(badResponse)
            }
          }
          case HttpMethod.POST => {
            val lines = request.getContentString().split("\n")
            try {
              val count = lines.head.toInt
              val urls = lines.drop(1)
              if (urls.size != count)
                return Future(badResponse)
              urls
            } catch {
              case e: NumberFormatException => return Future(badResponse)
            }
          }
        }
        
        if (toCheck.isEmpty)
          return Future(badResponse)
          
        futurePool(doLookup(toCheck, request))
      }
    }
  }

  def doLookup(urls: Array[String], request: Request) = {
    var hitCount = 0

    val response = request.response
    val results = urls.map(url => {
      sb2.lookup(url, null, useMac)
    }).map {
      case Some(list) => {
        hitCount += 1
        if (list.contains("malw")) "malware"
        else if (list.contains("phis")) "phishing"
        else list
      }
      case None => "ok"
    }
    Stats.incr("safebrowsing2/list_matches", hitCount)
    if (hitCount == 0) {
      response.status = Status.NoContent
      response
    } else {
      response.setContent(ChannelBuffers.copiedBuffer(results.mkString("\n"), UTF_8))
      response.setContentType(MediaType.Html, UTF_8.name)
      response
    }
  }

  lazy val service: Service[Request, Response] = exceptionHanlder andThen serve

}
