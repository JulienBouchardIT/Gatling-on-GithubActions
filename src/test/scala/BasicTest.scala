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
  def userCount: Int = 1
  def rampDuration: Int = 10
  def testDuration: Int = 60

  /*** Before ***/
  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
    def TOKEN: string = exec(
      http("Main page")
      .get("")
      .check(jsonPath("$..[*].token").findAll.saveAs("TOKEN"))
    )
    println(TOKEN)
  }

  /*** HTTP Calls ***/
 //todo
  def aHttpCall() = {
    exec(
      http("Main page")
        .get("")
        .check(status.is(200))
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
