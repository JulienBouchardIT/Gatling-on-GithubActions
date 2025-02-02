package simulation

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.duration._
import scala.util._

import scala.util.parsing.json.JSON
import scala.collection.immutable.Map

import scalaj.http._

class BasicTest extends Simulation {

  val httpConf = http
    .baseUrl("https://raw.githubusercontent.com/JulienBouchardIT/Gatling-on-GithubActions/master/src/test/scala/responseExample.json")
    .header("Accept", "application/json")

  /*** Variables ***/
  // runtime variables
  def userCount: Int = Integer.getInteger("userCount", 1)
  def rampDuration: Int = Integer.getInteger("rampDuration", 10)
  def testDuration: Int = Integer.getInteger("testDuration", 60)

  var aToken: String = ""
  var sessionTimeout: Long = 300000 // Timeout of session in ms
  var lastSessionTime: Long = 0

  def authURL: String = "https://raw.githubusercontent.com/JulienBouchardIT/Gatling-on-GithubActions/master/src/test/scala/responseExample.json"

  /*** Before ***/
  before {
    println(s"Running test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
    println(getSession())
  }

  /*** Like example on how to manage sessions and minimize the load on a your 
  authentication service. Use this fonction if you dont want to load test auth. ***/

  def updateSession() = {
    val jsonString = Http(authURL).asString.body
    val jsonMap = JSON.parseFull(jsonString).getOrElse(0).asInstanceOf[Map[String,String]]
    val innerMap = jsonMap("session").asInstanceOf[Map[String,String]]
    innerMap("token")
  }


  def getSession() = {
    if(aToken == "" || lastSessionTime+sessionTimeout>System.currentTimeMillis()){
      aToken = updateSession()
      lastSessionTime = System.currentTimeMillis()
    }
    aToken
  }



  /*** HTTP Calls ***/
 // todo
  def aHttpCall() = {
    exec(
      http("Get element from json")
      .get("")
      .check(jsonPath("$.session.token").is("1234567890"))
      .check(bodyString.saveAs("responseBody"))
      ).exec { session => println(session("responseBody").as[String]); session}
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
