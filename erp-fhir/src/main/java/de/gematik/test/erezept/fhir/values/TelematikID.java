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

package de.gematik.test.erezept.fhir.values;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import java.util.InputMismatchException;
import java.util.List;
import org.hl7.fhir.r4.model.Identifier;

public class TelematikID extends Value<String> {

  private static final List<ErpWorkflowNamingSystem> TELEMATIK_ID_SYSTEMS =
      List.of(ErpWorkflowNamingSystem.TELEMATIK_ID_SID, ErpWorkflowNamingSystem.TELEMATIK_ID);

  private TelematikID(String telematikId) {
    this(ErpWorkflowNamingSystem.TELEMATIK_ID, telematikId);
  }

  private TelematikID(ErpWorkflowNamingSystem system, String telematikId) {
    super(system, telematikId);
  }

  public static TelematikID from(String value) {
    return new TelematikID(value);
  }

  public static TelematikID from(Identifier identifier) {
    return TELEMATIK_ID_SYSTEMS.stream()
        .filter(ewns -> ewns.match(identifier))
        .map(ewns -> new TelematikID(ewns, identifier.getValue()))
        .findFirst()
        .orElseThrow(
            () ->
                new InputMismatchException(
                    format(
                        "given Identifier with System: {0} is not a Telematik ID",
                        identifier.getSystem())));
  }
}
