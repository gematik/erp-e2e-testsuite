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

package de.gematik.test.erezept.primsys.data.actors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import de.gematik.test.erezept.primsys.exceptions.InvalidActorRoleException;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum ActorType {
  DOCTOR("Arzt"),
  PHARMACY("Apotheke"),
  HEALTH_INSURANCE("Krankenkasse");

  @JsonValue private final String readable;

  ActorType(final String readable) {
    this.readable = readable;
  }

  @Override
  public String toString() {
    return readable;
  }

  @JsonCreator
  public static ActorType fromString(String value) {
    return optionalFromString(value).orElseThrow(() -> new InvalidActorRoleException(value));
  }

  public static Optional<ActorType> optionalFromString(String value) {
    return switch (value.toLowerCase()) {
      case "arzt", "doctor" -> Optional.of(DOCTOR);
      case "apotheke", "pharmacy" -> Optional.of(PHARMACY);
      case "kostenträger", "ktr", "krankenkasse" -> Optional.of(HEALTH_INSURANCE);
      default -> Optional.empty();
    };
  }
}
