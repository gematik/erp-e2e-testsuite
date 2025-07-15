/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.app.mobile;

import de.gematik.test.erezept.app.exceptions.NoSuchEnvironmentException;
import de.gematik.test.erezept.app.mobile.elements.DebugSettings;
import de.gematik.test.erezept.app.mobile.elements.PageElement;
import lombok.Getter;

@Getter
public enum Environment {
  RU(DebugSettings.RU_ENVIRONMENT),
  RU_DEV(DebugSettings.RU_DEV_ENVIRONMENT),
  TU(DebugSettings.TU_ENVIRONMENT);

  private final PageElement pageElement;

  Environment(PageElement pageElement) {
    this.pageElement = pageElement;
  }

  public static Environment fromString(String value) {
    return switch (value.toUpperCase()) {
      case "RU", "TITUS" -> RU;
      case "RU-DEV" -> RU_DEV;
      case "TU" -> TU;
      default -> throw new NoSuchEnvironmentException(value);
    };
  }
}
