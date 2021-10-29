package simulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.duration._
import scala.util.Random

class BasicTest extends Simulation {

  val httpConf = http
    .baseUrl("https://raw.githubusercontent.com/JulienBouchardIT/Gatling-on-GithubActions/master/src/test/scala/responseExample.json")
    .header("Accept", "application/json")

  /*** Variables ***/
  // runtime variables
  def userCount: Int = getProperty("USERS", "1").toInt
  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt
  def testDuration: Int = getProperty("DURATION", "60").toInt

  /*** Before ***/
  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
    println(http("Main page")
      .get("")
      .check(status.is(200)).toString)
  }

  /*** HTTP Calls ***/
 //todo
  def aHttpCall() = {
    exec(
      http("Main page")
        .get("")
        .check(status.is(200)))
    )
  }

  /*** Scenario Design ***/
  val scn = scenario("Basic").exec(aHttpCall())

  /*** Setup Load Simulation ***/
  setUp(
    scn.inject(
      nothingFor(5.seconds),
      rampUsers(userCount) during (rampDuration.seconds))
  )
    .protocols(httpConf)
    .maxDuration(testDuration.seconds)

  /*** After ***/
  after {
    println("Stress test completed")
  }

}
