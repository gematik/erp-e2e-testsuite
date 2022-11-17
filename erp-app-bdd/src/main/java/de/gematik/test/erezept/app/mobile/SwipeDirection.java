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

import java.time.Duration;
import lombok.val;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

public enum SwipeDirection {
  UP,
  DOWN,
  LEFT,
  RIGHT;

  public Sequence swipeSlowly(Dimension dimension) {
    return swipeOn(dimension, 500);
  }

  public Sequence swipeOn(Dimension dimension) {
    return swipeOn(dimension, 100);
  }

  public Sequence swipeOn(Dimension dimension, long millis) {
    val edgeBorderX = (int) (dimension.width * 0.1); // avoid swiping to or from edges
    val edgeBorderY = (int) (dimension.height * 0.1);
    val swipeDistX = dimension.width - 2 * edgeBorderX;
    val swipeDistY = dimension.height - 2 * edgeBorderY;
    Point startPoint;
    Point endPoint;

    switch (this) {
      case DOWN: // swipe down from the top
        startPoint = new Point(dimension.width / 2, edgeBorderY);
        endPoint = startPoint.moveBy(0, swipeDistY);
        break;
      case UP: // swipe up from the bottom
        startPoint = new Point(dimension.width / 2, dimension.height - edgeBorderY);
        endPoint = startPoint.moveBy(0, -swipeDistY);
        break;
      case LEFT:
        startPoint = new Point(dimension.width - edgeBorderX, dimension.height / 2);
        endPoint = startPoint.moveBy(-swipeDistX, 0);
        break;
      case RIGHT: // center of right side
        startPoint = new Point(edgeBorderX, dimension.height / 2);
        endPoint = startPoint.moveBy(swipeDistX, 0);
        break;
      default:
        throw new IllegalArgumentException("swipeScreen(): dir: '" + this + "' NOT supported");
    }

    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    val swipe = new Sequence(finger, 0);
    swipe.addAction(
        finger.createPointerMove(
            Duration.ofMillis(0), PointerInput.Origin.viewport(), startPoint.x, startPoint.y));
    swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.MIDDLE.asArg()));
    swipe.addAction(
        finger.createPointerMove(
            Duration.ofMillis(millis), PointerInput.Origin.viewport(), endPoint.x, endPoint.y));
    swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.MIDDLE.asArg()));

    return swipe;
  }
}
