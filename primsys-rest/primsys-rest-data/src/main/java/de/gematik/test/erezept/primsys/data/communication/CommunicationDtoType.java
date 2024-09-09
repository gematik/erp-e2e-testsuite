/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.primsys.data.communication;

import de.gematik.test.erezept.primsys.exceptions.InvalidCodeValueException;
import java.util.Arrays;
import java.util.Objects;
import lombok.val;

public enum CommunicationDtoType {
  CHANGE_REQ,
  CHANGE_REPLY,
  INFO_REQ,
  DISP_REQ,
  REPLY,
  REPRESENTATIVE;

  public static CommunicationDtoType fromString(String input) {
    val comName = Objects.requireNonNullElse(input, "NULL");
    return Arrays.stream(CommunicationDtoType.values())
        .filter(e -> comName.toUpperCase().contains(e.name()))
        .findFirst()
        .orElseThrow(() -> new InvalidCodeValueException(CommunicationDtoType.class, input));
  }
}
