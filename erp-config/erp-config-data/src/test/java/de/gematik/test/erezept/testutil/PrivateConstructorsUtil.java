/*
 * Copyright 2023 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.testutil;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.*;
import lombok.*;

public class PrivateConstructorsUtil {

  public static boolean throwsInvocationTargetException(Class<?> cls) {
    return throwsOnCall(InvocationTargetException.class, cls);
  }

  @SneakyThrows
  public static boolean throwsOnCall(Class<? extends Throwable> expectation, Class<?> cls) {
    val constructor = cls.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
    } catch (Throwable e) {
      return isExpectedError(expectation, e);
    }
    return false;
  }

  private static boolean isExpectedError(Class<? extends Throwable> expectation, Throwable error) {
    return error.getClass().equals(expectation)
        || (error.getCause() != null && error.getCause().getClass().equals(expectation));
  }
}
