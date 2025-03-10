/*
 * Copyright 2011-2021 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class ScenarioSampleScala {

//#bootstrapping
val scn = scenario("Scenario")
//#bootstrapping

//#exec
// attached to a scenario
scenario("Scenario")
  .exec(http("Home").get("https://gatling.io"))

// directly created and stored in a reference
val chain = exec(http("Home").get("https://gatling.io"))

// attached to another
exec(http("Home").get("https://gatling.io"))
  .exec(http("Enterprise").get("https://gatling.io/enterprise"))
//#exec

//#session-lambda
exec { session =>
  // displays the content of the session in the console (debugging only)
  println(session)
  // return the original session
  session
}

exec { session =>
  // return a new session instance
  // with a new "foo" attribute whose value is "bar"
  session.set("foo", "bar")
}
//#session-lambda

//#pause-fixed
// with a number of seconds
pause(10)
// with a scala.concurrent.duration.FiniteDuration
pause(100.millis)
// with a Gatling EL string resolving to a number of seconds or a scala.concurrent.duration.FiniteDuration
pause("#{pause}")
// with a function that returns a scala.concurrent.duration.FiniteDuration
pause(session => 100.millis)
//#pause-fixed

//#pause-uniform
// with a number of seconds
pause(10, 20)
// with a scala.concurrent.duration.FiniteDuration
pause(100.millis, 200.millis);
// with a Gatling EL strings
pause("#{min}", "#{max}")
// with a function that returns a scala.concurrent.duration.FiniteDuration
pause(session => 100.millis, session => 200.millis)
//#pause-uniform

//#pace
forever(
  pace(5)
    .exec(
      // will be run every 5 seconds, irrespective of what pause time is used
      pause(1, 4)
    )
)
//#pace

//#pace-fixed
// with a number of seconds
pace(10)
// with a scala.concurrent.duration.FiniteDuration
pace(100.millis)
// with a Gatling EL string resolving to a number of seconds or a scala.concurrent.duration.FiniteDuration
pace("#{pace}")
// with a function that returns a scala.concurrent.duration.FiniteDuration
pace(session => 100.millis)
//#pace-fixed

//#pace-uniform
// with a number of seconds
pace(10, 20)
// with a java.time.Duration
pace(100.millis, 200.millis)
// with a Gatling EL strings
pace("#{min}", "#{max}")
// with a function that returns a scala.concurrent.duration.FiniteDuration
pace(session => 100.millis, session => 200.millis)
//#pace-uniform

//#rendezVous
rendezVous(100)
//#rendezVous

//#repeat
// with an Int times
repeat(5) {
  exec(http("name").get("/"))
}
// with a Gatling EL string resolving an Int
repeat("#{times}") {
  exec(http("name").get("/"))
}
// with a function times
repeat(session => 5) {
  exec(http("name").get("/"))
}
// with a counter name
repeat(5, "counter") {
  exec { session =>
    System.out.println(session("counter").as[Int])
    session
  }
}
//#repeat

//#foreach
// with a static Seq
foreach(Seq("elt1", "elt2"), "elt") {
  exec(http("name").get("/"))
}
// with a Gatling EL string
foreach("#{elts}", "elt") {
  exec(http("name").get("/"))
}
// with a function
foreach(session => Seq("elt1", "elt2"), "elt") {
  exec(http("name").get("/"))
}
// with a counter name
foreach(Seq("elt1", "elt2"), "elt", "counter") {
  exec { session =>
    System.out.println(session("elt2").as[String])
    session
  }
}
//#foreach

//#during
// with a duration in seconds
during(5) {
  exec(http("name").get("/"))
}
// with a java.time.Duration
during(10.minutes) {
  exec(http("name").get("/"))
}
// with a Gatling EL string resolving a duration
during("#{times}") {
  exec(http("name").get("/"))
}
// with a function times
during(session => 10.minutes) {
  exec(http("name").get("/"))
}
// with a counter name
during(5, "counter") {
  exec(http("name").get("/"))
}
// with exitASAP
during(5, "counter", false) {
  exec(http("name").get("/"))
}
//#during

//#asLongAs
// with a Gatling EL string resolving a boolean
asLongAs("#{condition}") {
  exec(http("name").get("/"))
}
// with a function
asLongAs(session => session("condition").as[Boolean]) {
  exec(http("name").get("/"))
}
// with a counter name and exitASAP
asLongAs("#{condition}", "counter", false) {
  exec(http("name").get("/"))
}
//#asLongAs

//#doWhile
// with a Gatling EL string resolving to a boolean
doWhile("#{condition}") {
  exec(http("name").get("/"))
}
// with a function
doWhile(session => session("condition").as[Boolean]) {
  exec(http("name").get("/"))
}
// with a counter name
doWhile("#{condition}", "counter") {
  exec(http("name").get("/"))
}
//#doWhile

//#asLongAsDuring
// with a Gatling EL string resolving to a boolean and an int duration
asLongAsDuring("#{condition}", 5) {
  exec(http("name").get("/"))
}
// with a counter name and exitASAP
asLongAsDuring("#{condition}", session => 10.minutes, "counter", false) {
  exec(http("name").get("/"))
}
//#asLongAsDuring

//#doWhileDuring
// with a Gatling EL string resolving to a boolean and an int duration
doWhileDuring("#{condition}", 5) {
  exec(http("name").get("/"))
}
// with a counter name and exitASAP
doWhileDuring("#{condition}", session => 10.minutes, "counter", false) {
  exec(http("name").get("/"))
}
//#doWhileDuring

//#forever
forever {
  exec(http("name").get("/"))
}
// with a counter name
forever("counter") {
  exec(http("name").get("/"))
}
//#forever

//#doIf
// with a Gatling EL string resolving to a boolean
doIf("#{condition}") {
  exec(http("name").get("/"))
}

// with a function
doIf(session => session("condition").as[Boolean]) {
  exec(http("name").get("/"))
}
//#doIf

//#doIfEquals
doIfEquals("#{actual}", "expectedValue") {
  // executed if the session value stored in "actual" is equal to "expectedValue"
  exec(http("name").get("/"))
}
//#doIfEquals

//#doIfOrElse
doIfOrElse("#{condition}") {
  exec(http("name").get("/"))
} {
  exec(http("else").get("/"))
}
//#doIfOrElse

//#doIfEqualsOrElse
doIfEqualsOrElse("#{actual}", "expectedValue") {
  // executed if the session value stored in "actual" equals to "expectedValue"
  exec(http("name").get("/"))
} {
  // executed if the session value stored in "actual" is not equal to "expectedValue"
  exec(http("else").get("/"))
}
//#doIfEqualsOrElse

//#doSwitch
doSwitch("#{myKey}")( // beware: use parentheses, not curly braces!
  "foo" -> exec(http("name1").get("/foo")),
  "bar" -> exec(http("name2").get("/bar"))
)
//#doSwitch

//#doSwitchOrElse
doSwitchOrElse("#{myKey}")( // beware: use parentheses, not curly braces!
  "foo" -> exec(http("name1").get("/foo")),
  "bar" -> exec(http("name2").get("/bar"))
)(
  exec(http("name3").get("/baz"))
)
//#doSwitchOrElse

//#randomSwitch
randomSwitch( // beware: use parentheses, not curly braces!
  60.0 -> exec(http("name1").get("/foo")),
  40.0 -> exec(http("name2").get("/bar"))
)
//#randomSwitch

//#randomSwitchOrElse
randomSwitchOrElse( // beware: use parentheses, not curly braces!
  60.0 -> exec(http("name1").get("/foo")),
  20.0 -> exec(http("name2").get("/bar"))
)(
  exec(http("name3").get("/baz"))
)
//#randomSwitchOrElse

//#uniformRandomSwitch
uniformRandomSwitch( // beware: use parentheses, not curly braces!
  exec(http("name1").get("/foo")),
  exec(http("name2").get("/bar"))
)
//#uniformRandomSwitch

//#roundRobinSwitch
roundRobinSwitch( // beware: use parentheses, not curly braces!
  exec(http("name1").get("/foo")),
  exec(http("name2").get("/bar"))
)
//#roundRobinSwitch

//#tryMax
tryMax(5) {
  exec(http("name").get("/"))
}

// with a counter name
tryMax(5, "counter") {
  exec(http("name").get("/"))
}
//#tryMax

//#exitBlockOnFail
exitBlockOnFail {
  exec(http("name").get("/"))
}
//#exitBlockOnFail

//#exitHere
exitHere
//#exitHere

//#exitHereIf
exitHereIf("#{condition}")
exitHereIf(session => true)
//#exitHereIf

//#exitHereIfFailed
exitHereIfFailed
//#exitHereIfFailed

//#stopInjector
stopInjector("#{someErrorMessage}")
stopInjector(session => "someErrorMessage")
//#stopInjector

//#group
group("foo") {
  exec(http("name").get("/"))
}
//#group
}
