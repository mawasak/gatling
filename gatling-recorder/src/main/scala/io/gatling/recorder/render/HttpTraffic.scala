/*
 * Copyright 2011-2022 GatlingCorp (https://gatling.io)
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

package io.gatling.recorder.render

import scala.concurrent.duration.{ Duration, DurationLong }

import io.gatling.http.util.HttpHelper
import io.gatling.recorder.config.RecorderConfiguration
import io.gatling.recorder.util.collection.RichSeq

import com.softwaremill.quicklens._
import com.typesafe.scalalogging.StrictLogging
import io.netty.handler.codec.http.HttpResponseStatus

private[recorder] final case class HttpTraffic(elements: List[HttpTrafficElement]) {
  def isEmpty: Boolean = elements.isEmpty
}

private[recorder] object HttpTraffic extends StrictLogging {

  private val ConsecutiveResourcesMaxIntervalInMillis = 1000

  private def isRedirection(t: TimedScenarioElement[RequestElement]) = HttpHelper.isRedirect(HttpResponseStatus.valueOf(t.element.statusCode))

  private def filterRedirection(requests: Seq[TimedScenarioElement[RequestElement]]): List[TimedScenarioElement[RequestElement]] = {
    val groupedRequests = requests.groupAsLongAs(isRedirection)

    // Remove the redirection and keep the last status code
    groupedRequests.flatMap {
      case TimedScenarioElement(firstSendTime, _, firstReq) :: redirectedReqs if redirectedReqs.nonEmpty =>
        val TimedScenarioElement(_, lastArrivalTime, lastReq) = redirectedReqs.last
        List(
          TimedScenarioElement(firstSendTime, lastArrivalTime, firstReq.copy(statusCode = lastReq.statusCode, embeddedResources = lastReq.embeddedResources))
        )

      case reqs => reqs
    }
  }

  private def filterInferredResources(requests: List[TimedScenarioElement[RequestElement]]): List[TimedScenarioElement[RequestElement]] = {

    val groupChainedRequests: List[List[TimedScenarioElement[RequestElement]]] = {
      var globalAcc = List.empty[List[TimedScenarioElement[RequestElement]]]
      var currentAcc = List.empty[TimedScenarioElement[RequestElement]]
      var previousArrivalTime = requests.head.arrivalTime
      for (request <- requests) {
        if (request.sendTime - previousArrivalTime < ConsecutiveResourcesMaxIntervalInMillis) {
          currentAcc = currentAcc ::: List(request)
        } else {
          if (currentAcc.nonEmpty)
            globalAcc = globalAcc ::: List(currentAcc)
          currentAcc = List(request)
        }
        previousArrivalTime = request.arrivalTime
      }
      globalAcc ::: List(currentAcc)
    }

    groupChainedRequests.map {
      case request :: Nil           => request
      case mainRequest :: resources =>
        // TODO NRE : are we sure they are both absolute URLs?
        val nonEmbeddedResources = resources.filterNot(request => mainRequest.element.embeddedResources.exists(_.url == request.element.uri)).map(_.element)
        mainRequest
          .modify(_.arrivalTime)
          .setTo(resources.map(_.arrivalTime).max)
          .modify(_.element.nonEmbeddedResources)
          .setTo(nonEmbeddedResources)
      case _ => throw new IllegalArgumentException(s"groupChainedRequests shouldn't be empty")
    }
  }

  // FIXME no need for sortedRequests
  private def mergeWithPauses(
      sortedRequests: List[TimedScenarioElement[RequestElement]],
      tags: List[TimedScenarioElement[TagElement]],
      thresholdForPauseCreation: Duration
  ): List[HttpTrafficElement] = {

    if (sortedRequests.size <= 1)
      sortedRequests.map(_.element)
    else {
      val allElements: List[TimedScenarioElement[HttpTrafficElement]] = (sortedRequests ++ tags).sortBy(_.arrivalTime)
      var lastSendDateTime = allElements.last.sendTime
      val allElementsWithTagsStickingToNextRequest: List[TimedScenarioElement[HttpTrafficElement]] =
        allElements.reverse.foldLeft(List.empty[TimedScenarioElement[HttpTrafficElement]]) { (acc, current) =>
          current match {
            case TimedScenarioElement(_, _, TagElement(text)) =>
              TimedScenarioElement(lastSendDateTime, lastSendDateTime, TagElement(text)) :: acc

            case TimedScenarioElement(sendTime, _, _) =>
              lastSendDateTime = sendTime
              current :: acc
          }
        }

      val pauses = allElementsWithTagsStickingToNextRequest.tail
        .zip(allElementsWithTagsStickingToNextRequest)
        .map { case (element, previousElement) =>
          val pauseDurationInMillis = math.max(element.sendTime - previousElement.arrivalTime, 0L)
          TimedScenarioElement(element.arrivalTime, element.arrivalTime, PauseElement(pauseDurationInMillis.milliseconds))
        }

      val combined = allElementsWithTagsStickingToNextRequest.zip(pauses).flatMap { case (elem, pause) => List(elem, pause) } ++ Seq(
        allElementsWithTagsStickingToNextRequest.last
      )

      combined
        .filter {
          case TimedScenarioElement(_, _, PauseElement(duration)) => duration >= thresholdForPauseCreation
          case _                                                  => true
        }
        .map(_.element)
    }
  }

  def apply(
      requests: List[TimedScenarioElement[RequestElement]],
      tags: List[TimedScenarioElement[TagElement]],
      config: RecorderConfiguration
  ): HttpTraffic = {
    val sortedRequests = requests.sortBy(_.arrivalTime)

    val requests1 = if (config.http.followRedirect) filterRedirection(sortedRequests) else sortedRequests
    val requests2 = if (config.http.inferHtmlResources) filterInferredResources(requests1) else requests1

    if (config.http.followRedirect) logger.debug(s"Cleaning redirections: ${requests.size}->${requests1.size} requests")
    if (config.http.inferHtmlResources) logger.debug(s"Cleaning automatically fetched HTML resources: ${requests1.size}->${requests2.size} requests")

    val allElements = mergeWithPauses(requests2, tags, config.core.thresholdForPauseCreation)
    apply(allElements)
  }
}
