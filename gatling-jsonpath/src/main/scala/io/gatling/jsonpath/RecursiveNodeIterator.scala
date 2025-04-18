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

package io.gatling.jsonpath

import java.{ util => ju }

import com.fasterxml.jackson.databind.JsonNode

/**
 * Collect all nodes
 * @param root the tree root
 *
 *  Originally contributed by Nicolas Rémond.
 */
class RecursiveNodeIterator(root: JsonNode) extends RecursiveIterator[ju.Iterator[JsonNode]](root) {

  override protected def visit(it: ju.Iterator[JsonNode]): Unit = {
    while (it.hasNext && !pause) {
      visitNode(it.next())
    }
    if (!pause) {
      stack = stack.tail
    }
  }

  override protected def visitNode(node: JsonNode): Unit = {
    if (node.size > 0) {
      stack = node.elements :: stack
    }
    nextNode = node
    pause = true
  }
}
