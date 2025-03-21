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
 */

package de.gematik.test.fuzzing.string;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RegExp {

  @Getter private final String regex;

  public Pattern asPattern() {
    return Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE | Pattern.UNIX_LINES);
  }

  public Matcher matcher(String input) {
    return asPattern().matcher(input);
  }

  /**
   * Simply matches anything!
   *
   * @return RegExp wrapping the Expression (.*)
   */
  public static RegExp everything() {
    return new RegExp("(?<target>.+)");
  }
}
