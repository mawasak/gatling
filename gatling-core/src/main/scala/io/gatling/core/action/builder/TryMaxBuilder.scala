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

package io.gatling.core.action.builder

import io.gatling.core.action.{ Action, TryMax }
import io.gatling.core.session.Expression
import io.gatling.core.structure.{ ChainBuilder, ScenarioContext }

private[core] final class TryMaxBuilder(times: Expression[Int], counterName: String, loopNext: ChainBuilder) extends ActionBuilder {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    import ctx._
    val tryMaxAction = new TryMax(times, counterName, coreComponents.statsEngine, coreComponents.clock, next)
    val loopNextAction = loopNext.build(ctx, tryMaxAction)
    tryMaxAction.initialize(loopNextAction)
    tryMaxAction
  }
}
