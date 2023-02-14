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

package de.gematik.test.erezept.app.cfg;

import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import lombok.NonNull;

public enum PlatformType {
  IOS,
  ANDROID,
  DESKTOP;

  public static PlatformType fromString(@NonNull final String platform) {
    return switch (platform.toLowerCase()) {
      case "ios" -> PlatformType.IOS;
      case "android" -> PlatformType.ANDROID;
      case "desktop", "adv" -> PlatformType.DESKTOP;
      default -> throw new UnsupportedPlatformException(platform);
    };
  }
}
