/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.fuzzing.kbv;

import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.LinkedList;
import java.util.List;
import lombok.val;

public class MvoExtensionManipulatorFactory {

  private MvoExtensionManipulatorFactory() {
    throw new AssertionError("Do not instantiate");
  }

  public static List<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>
      getMvoExtensionKennzeichenFalsifier() {
    val manipulators = new LinkedList<NamedEnvelope<FuzzingMutator<KbvErpBundle>>>();

    manipulators.add(
        NamedEnvelope.of(
            "MVO Kennzeichen = false mit Nummerierung und Zeitraum",
            b -> {
              val ext =
                  b.getMedicationRequest()
                      .getExtensionByUrl(
                          ErpStructureDefinition.KBV_MULTIPLE_PRESCRIPTION.getUnversionedUrl());
              val kennzeichen = ext.getExtensionByUrl("Kennzeichen");
              kennzeichen.getValue().castToBoolean(kennzeichen.getValue()).setValue(false);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MVO ohne Kennzeichen und mit Nummerierung und Zeitraum",
            b -> {
              val mr = b.getMedicationRequest();
              val ext =
                  mr.getExtensionByUrl(
                      ErpStructureDefinition.KBV_MULTIPLE_PRESCRIPTION.getUnversionedUrl());
              val kennzeichen = ext.getExtensionByUrl("Kennzeichen");
              ext.getExtension().remove(kennzeichen);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MVO Kennzeichen = true ohne Nummerierung und mit Zeitraum",
            b -> {
              val mr = b.getMedicationRequest();
              val ext =
                  mr.getExtensionByUrl(
                      ErpStructureDefinition.KBV_MULTIPLE_PRESCRIPTION.getUnversionedUrl());
              val nummerierung = ext.getExtensionByUrl("Nummerierung");
              ext.getExtension().remove(nummerierung);
            }));

    manipulators.add(
        NamedEnvelope.of(
            "MVO Kennzeichen = true mit Nummerierung und ohne Zeitraum",
            b -> {
              val mr = b.getMedicationRequest();
              val ext =
                  mr.getExtensionByUrl(
                      ErpStructureDefinition.KBV_MULTIPLE_PRESCRIPTION.getUnversionedUrl());
              val zeitraum = ext.getExtensionByUrl("Zeitraum");
              ext.getExtension().remove(zeitraum);
            }));

    return manipulators;
  }
}
