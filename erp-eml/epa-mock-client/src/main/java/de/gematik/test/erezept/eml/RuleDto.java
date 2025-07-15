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

package de.gematik.test.erezept.eml;

public record RuleDto(String kvnr, int statusCode, String template, int delay) {

  public static RuleDto reply(String kvnr, RuleTemplate template, int delay) {
    return new RuleDto(kvnr, template.getStatusCode(), template.getFileName(), delay);
  }

  public static RuleDto reply(String kvnr, RuleTemplate template) {
    return reply(kvnr, template, 0);
  }
}
