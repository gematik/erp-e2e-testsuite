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

package de.gematik.test.erezept.screenplay.abilities;

import de.gematik.test.erezept.screenplay.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;

@Slf4j
@Getter
public class ManageCommunications implements Ability {

  private final ManagedList<ExchangedCommunication> sentCommunications;
  private final ManagedList<ExchangedCommunication> expectedCommunications;

  private ManageCommunications() {
    this.sentCommunications = new ManagedList<>(() -> "No Communications were sent so far");
    this.expectedCommunications = new ManagedList<>(() -> "No Communications were expected so far");
  }

  public static ManageCommunications sheExchanges() {
    return heExchanges();
  }

  public static ManageCommunications heExchanges() {
    return new ManageCommunications();
  }
}
