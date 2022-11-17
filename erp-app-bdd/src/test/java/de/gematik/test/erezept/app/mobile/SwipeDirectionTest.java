/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.app.mobile;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;

class SwipeDirectionTest {

  private static final List<SwipeDirection> DIRECTIONS =
      List.of(SwipeDirection.DOWN, SwipeDirection.UP, SwipeDirection.LEFT, SwipeDirection.RIGHT);

  @Test
  void shouldSwipeSlowly() {
    val dimension = new Dimension(300, 800);
    DIRECTIONS.forEach(
        direction -> {
          val s = direction.swipeSlowly(dimension);
          val actionsMap = s.encode();
          val actions = (List<HashMap<String, Object>>) actionsMap.get("actions");
          val swipeAction = actions.get(2);
          val duration = swipeAction.get("duration");
          assertEquals(500L, duration);
        });
  }

  @Test
  void shouldSwipeFast() {
    val dimension = new Dimension(300, 800);
    DIRECTIONS.forEach(
        direction -> {
          val s = direction.swipeOn(dimension);
          val actionsMap = s.encode();
          val actions = (List<HashMap<String, Object>>) actionsMap.get("actions");
          val swipeAction = actions.get(2);
          val duration = swipeAction.get("duration");
          assertEquals(100L, duration);
        });
  }

  @Test
  void shouldSwipeWithCustomSpeed() {
    val customDuration = 10L;
    val dimension = new Dimension(300, 800);
    DIRECTIONS.forEach(
        direction -> {
          val s = direction.swipeOn(dimension, customDuration);
          val actionsMap = s.encode();
          val actions = (List<HashMap<String, Object>>) actionsMap.get("actions");
          val swipeAction = actions.get(2);
          val duration = swipeAction.get("duration");
          assertEquals(customDuration, duration);
        });
  }
}
