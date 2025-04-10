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

package io.gatling.core.check.xpath

import io.gatling.core.check.{ CheckBuilder, Extractor }
import io.gatling.core.session._

import net.sf.saxon.s9api.XdmNode

sealed trait XPathCheckType

final class XPathCheckBuilder(
    path: Expression[String],
    namespaces: Map[String, String],
    xmlParsers: XmlParsers
) extends CheckBuilder.MultipleFind.Default[XPathCheckType, XdmNode, String](displayActualValue = true) {

  override protected def findExtractor(occurrence: Int): Expression[Extractor[XdmNode, String]] =
    path.map(XPathExtractors.find(_, namespaces, occurrence, xmlParsers))
  override protected def findAllExtractor: Expression[Extractor[XdmNode, Seq[String]]] = path.map(XPathExtractors.findAll(_, namespaces, xmlParsers))
  override protected def countExtractor: Expression[Extractor[XdmNode, Int]] = path.map(XPathExtractors.count(_, namespaces, xmlParsers))
}
