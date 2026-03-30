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
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    val pznBuilder =
        KbvErpMedicationPZNBuilder.builder()
            .category(MedicationCategory.fromCode(category))
            .isVaccine(isVaccine)
            .normgroesse(StandardSize.fromCode(size))
            .darreichungsform(Darreichungsform.fromCode(darreichungsForm))
            .amount(Integer.decode(darreichungsMenge))
            .pzn(pzn, name);
    // temporary fix caused by deleting this behavior in PznBuilder and moved to PznFaker, but
    // forgot
    // to enforce that behavior in E2E scope
    if (KbvItaErpVersion.getDefaultVersion().isSmallerThanOrEqualTo(KbvItaErpVersion.V1_3_0)) {
      pznBuilder.ingredientText(
          getOrDefault("Wirkstoffname", "Phrmakologische Fachbegriffe", medMap));
    }

    Optional.ofNullable(medMap.get("Wirkstoffname")).ifPresent(pznBuilder::ingredientText);

    Optional.ofNullable(medMap.get("WirkstoffMenge"))
        .map(Double::valueOf)
        .ifPresent(
            it ->
                pznBuilder.ingredientStrengthNum(
                    it, getOrDefault("WirkstoffMengeEinheit", "mg", medMap)));

    Optional.ofNullable(medMap.get("Bezugsmenge"))
        .map(Double::valueOf)
        .ifPresent(
            it ->
                pznBuilder.ingredientStrengthDenom(
                    it, getOrDefault("BezugsmengeEinheit", "Stück", medMap)));

    return pznBuilder.build();
  }
}
