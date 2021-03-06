package singletonactor

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import common.RandomSupport

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn.readLine

object SingletonPersistentApp extends RandomSupport with SingletonSupport {

  implicit val timeout = Timeout(10 seconds)
  implicit val system = ActorSystem("singleton-persistent", ConfigFactory.load("singleton-persistent"))
  implicit val materializer = ActorMaterializer()

  val map = createSingleton

  val Attempts = 2500000
  val KeySize  = 5000000

  def populate(map: ActorRef) = {
    val result = Source(1 to Attempts)
      .mapAsync(1)(_ => map ? Insert(randomKey(KeySize), randomValue()))
      .runWith(Sink.ignore)

    val start = System.currentTimeMillis()
    Await.ready(result, Duration.Inf)
    val end = System.currentTimeMillis()
    println(s" Populating done in ${end - start} ms.")
  }

  def query(map: ActorRef) = {
    val result = Source(1 to KeySize)
      .mapAsync(1)(_ => map ? Get(randomKey(KeySize)))
      .runWith(Sink.ignore)

    val start = System.currentTimeMillis()
    Await.ready(result, Duration.Inf)
    val end = System.currentTimeMillis()
    println(s" Querying done in ${end - start} ms.")
  }

  readLine("Press ENTER to start the benchmark")
  populate(map)
  query(map)
  readLine("Press ENTER to exit")
  system.terminate()
}