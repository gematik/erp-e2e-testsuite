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

package de.gematik.test.core.exceptions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.actors.ErpActor;
import java.lang.reflect.Field;

public class NotAnActorException extends RuntimeException {

  public NotAnActorException(Field field) {
    super(
        format(
            "Field {0} of type {1} does not extend the {2}",
            field.getName(), field.getType(), ErpActor.class.getSimpleName()));
  }
}
