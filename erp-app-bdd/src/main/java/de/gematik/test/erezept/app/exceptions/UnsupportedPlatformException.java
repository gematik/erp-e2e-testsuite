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

package de.gematik.test.erezept.app.exceptions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.mobile.PlatformType;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.serenitybdd.model.exceptions.TestCompromisedException;

public class UnsupportedPlatformException extends TestCompromisedException {

  public UnsupportedPlatformException(String givenPlatform) {
    super(
        format(
            "The given platform {0} is not supported, choose one of {1}",
            givenPlatform,
            Stream.of(PlatformType.values())
                .map(PlatformType::toString)
                .collect(Collectors.joining(","))));
  }

  public UnsupportedPlatformException(PlatformType platformType) {
    this(platformType.name());
  }

  public UnsupportedPlatformException(PlatformType platformType, String operation) {
    super(format("Operation ''{0}'' not supported for {1}", operation, platformType));
  }
}
