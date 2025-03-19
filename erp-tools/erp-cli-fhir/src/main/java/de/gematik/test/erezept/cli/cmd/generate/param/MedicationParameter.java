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

package de.gematik.test.erezept.cli.cmd.generate.param;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNBuilder;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.List;
import lombok.Getter;
import picocli.CommandLine;

public class MedicationParameter implements BaseResourceParameter {

  @CommandLine.Option(
      names = {"--category"},
      paramLabel = "<CATEGORY>",
      type = MedicationCategory.class,
      description =
          "Category of the medication from ${COMPLETION-CANDIDATES} (default=${DEFAULT-VALUE})")
  @Getter
  private MedicationCategory category = MedicationCategory.C_00;

  @CommandLine.Option(
      names = {"--vaccine"},
      type = Boolean.class,
      description = "Marks medication as vaccine (default=${DEFAULT-VALUE})")
  @Getter
  private Boolean isVaccine = false;

  @CommandLine.Option(
      names = {"--size", "--normgröße"},
      paramLabel = "<SIZE>",
      type = StandardSize.class,
      description = "Standardsize (Normgröße) from ${COMPLETION-CANDIDATES} (default=random)")
  private StandardSize size;

  @CommandLine.Option(
      names = {"--supply", "--darreichungsform"},
      paramLabel = "<FORM>",
      type = StandardSize.class,
      description = "Supplyform (Darreichungsform) from ${COMPLETION-CANDIDATES} (default=random)")
  private Darreichungsform supplyForm;

  @CommandLine.Option(
      names = {"--amount"},
      paramLabel = "<QUANTITY>",
      type = Integer.class,
      description = "The amount of units to be supplied (default=random)")
  private Integer amount;

  @CommandLine.Option(
      names = {"--pzn"},
      paramLabel = "<PZN>",
      type = String.class,
      description = "The PZN (Pharmazentralnummer) of the medication (default=random)")
  private String pzn;

  @CommandLine.Option(
      names = {"--drugname"},
      paramLabel = "<NAME>",
      type = String.class,
      description = "The name of the medication (default=random)")
  private String drugName;

  public StandardSize getStandardSize() {
    return this.getOrDefault(size, () -> GemFaker.fakerValueSet(StandardSize.class));
  }

  public Darreichungsform getSupplyForm() {
    return this.getOrDefault(
        supplyForm,
        () ->
            GemFaker.fakerValueSet(
                Darreichungsform.class, List.of(Darreichungsform.PUE, Darreichungsform.LYE)));
  }

  public Integer getQuantity() {
    return this.getOrDefault(amount, GemFaker::fakerAmount);
  }

  public String getPzn() {
    return this.getOrDefault(pzn, () -> PZN.random().getValue());
  }

  public String getDrugName() {
    return this.getOrDefault(drugName, GemFaker::fakerDrugName);
  }

  public KbvErpMedication createMedication() {
    return KbvErpMedicationPZNBuilder.builder()
        .category(getCategory())
        .isVaccine(getIsVaccine())
        .normgroesse(getStandardSize())
        .darreichungsform(getSupplyForm())
        .amount(getQuantity(), "Stk")
        .pzn(getPzn(), getDrugName())
        .build();
  }
}
