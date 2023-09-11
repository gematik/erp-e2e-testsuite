/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.app.task;

import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import java.util.function.Supplier;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.Task;

public class PlatformScreenplayUtil {

  private PlatformScreenplayUtil() {
    throw new AssertionError("Do not instantiate this utility-class");
  }

  public static Task chooseTaskForPlatform(
      PlatformType platformType, Supplier<Task> android, Supplier<Task> ios) {
    Task task;
    if (platformType == PlatformType.ANDROID) {
      task = android.get();
    } else if (platformType == PlatformType.IOS) {
      task = ios.get();
    } else {
      throw new UnsupportedPlatformException(platformType);
    }
    return task;
  }

  public static <T> Question<T> chooseQuestionForPlatform(
      PlatformType platformType, Supplier<Question<T>> android, Supplier<Question<T>> ios) {
    Question<T> question;
    if (platformType == PlatformType.ANDROID) {
      question = android.get();
    } else if (platformType == PlatformType.IOS) {
      question = ios.get();
    } else {
      throw new UnsupportedPlatformException(platformType);
    }
    return question;
  }
}
