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

package de.gematik.test.erezept.screenplay.strategy.prescription;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNBuilder;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.List;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

public class PrescriptionDataMapperPZN extends PrescriptionDataMapper {

  public PrescriptionDataMapperPZN(
      Actor patient,
      PrescriptionAssignmentKind prescriptionAssignmentKind,
      List<Map<String, String>> medications) {
    super(medications, patient, prescriptionAssignmentKind);
  }

  @Override
  protected KbvErpMedication getKbvErpMedication(Map<String, String> medMap) {
    val pzn = getOrDefault("PZN", PZN.random().getValue(), medMap);
    val name = getOrDefault("Name", GemFaker.fakerDrugName(), medMap);
    val category = getOrDefault("Verordnungskategorie", "00", medMap);
    val isVaccine = Boolean.parseBoolean(getOrDefault("Impfung", "false", medMap));
    val size = getOrDefault("Normgröße", "NB", medMap);
    val darreichungsForm = getOrDefault("Darreichungsform", "TAB", medMap);
    val darreichungsMenge = getOrDefault("Darreichungsmenge", "1", medMap);

    return KbvErpMedicationPZNBuilder.builder()
        .category(MedicationCategory.fromCode(category))
        .isVaccine(isVaccine)
        .normgroesse(StandardSize.fromCode(size))
        .darreichungsform(Darreichungsform.fromCode(darreichungsForm))
        .amount(Integer.decode(darreichungsMenge))
        .pzn(pzn, name)
        .build();
  }
}
