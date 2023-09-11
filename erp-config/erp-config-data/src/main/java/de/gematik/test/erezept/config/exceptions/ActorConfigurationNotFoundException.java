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

package de.gematik.test.erezept.config.exceptions;

import static java.text.MessageFormat.format;

import java.util.List;

public class ActorConfigurationNotFoundException extends ConfigurationException {

  public ActorConfigurationNotFoundException(String actorName, List<String> availableActors) {
    super(
        format(
            "Actor named {0} is not configured within the list of valid Actors {1}",
            actorName, String.join(", ", availableActors)));
  }
}
