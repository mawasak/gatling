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

package io.gatling.recorder.render.template

import io.gatling.BaseSpec
import io.gatling.recorder.render.RequestElement

import io.netty.handler.codec.http.EmptyHttpHeaders

class ExtractedUrisSpec extends BaseSpec {

  private def mockRequestElement(uri: String) = new RequestElement(uri, "get", EmptyHttpHeaders.INSTANCE, None, EmptyHttpHeaders.INSTANCE, None, 200, Nil, Nil)

  private def extractUris(uris: Seq[String]): ExtractedUris = ExtractedUris(uris.map(mockRequestElement), Format.Scala)

  "extracting uris" should "extract common root" in {
    val commonRoot = "http://gatling.io/path1"
    val url1 = commonRoot + "/file1"
    val url2 = commonRoot + "/file2"
    val extractedUris = extractUris(Seq(url1, url2))

    extractedUris.renderUri(url1) shouldBe """uri1 + "/file1""""
    extractedUris.renderUri(url2) shouldBe """uri1 + "/file2""""
  }

  it should "extract common roots from different authorities" in {
    val gatlingRoot = "http://gatling.io/path1"
    val gatlingUrl1 = gatlingRoot + "/file1"
    val gatlingUrl2 = gatlingRoot + "/file2"
    val nettyRoot = "http://netty.io"
    val nettyUrl1 = nettyRoot + "/file1"
    val nettyUrl2 = nettyRoot + "/file2"

    val extractedUris = extractUris(Seq(gatlingUrl1, gatlingUrl2, nettyUrl1, nettyUrl2))

    extractedUris.renderUri(gatlingUrl1) shouldBe """uri1 + "/file1""""
    extractedUris.renderUri(gatlingUrl2) shouldBe """uri1 + "/file2""""
    extractedUris.renderUri(nettyUrl1) shouldBe """uri2 + "/file1""""
    extractedUris.renderUri(nettyUrl2) shouldBe """uri2 + "/file2""""
  }

  it should "preserve port and auth" in {
    val gatlingUri = "https://user:pwd@gatling.io:8080/?q=v"
    val extractedUris = extractUris(Seq(gatlingUri))

    extractedUris.renderUri(gatlingUri) shouldBe """uri1 + "/?q=v""""
  }

  it should "extract only authorities when they are used with different schemes" in {
    val extractedUris = extractUris(Seq("http://gatling.io/path1/file1", "https://gatling.io/path1/file2"))

    extractedUris.renderUri("http://gatling.io/path1/file1") shouldBe """"http://" + uri1 + "/path1/file1""""
    extractedUris.renderUri("https://gatling.io/path1/file2") shouldBe """"https://" + uri1 + "/path1/file2""""
  }

  it should "extract only authorities when they are used with different ports" in {
    val uri1 = "http://gatling.io/path1/file"
    val uri2 = "http://gatling.io:8080/path1/file"
    val extractedUris = extractUris(Seq(uri1, uri2))

    extractedUris.renderUri(uri1) shouldBe """"http://" + uri1 + "/path1/file""""
    extractedUris.renderUri(uri2) shouldBe """"http://" + uri1 + ":8080/path1/file""""
  }
}
