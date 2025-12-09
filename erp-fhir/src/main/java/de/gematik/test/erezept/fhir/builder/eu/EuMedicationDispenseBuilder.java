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

package de.gematik.test.erezept.fhir.builder.eu;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBaseBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.r4.eu.EuMedicationDispense;
import de.gematik.test.erezept.fhir.util.IdentifierUtil;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.*;

public class EuMedicationDispenseBuilder
    extends ErxMedicationDispenseBaseBuilder<
        EuMedicationDispense, EuVersion, EuMedicationDispenseBuilder> {

  private Date whenPrepared;
  private Boolean wasSubstituted;
  private final List<String> dosageInstructions = new LinkedList<>();
  private final List<String> notes = new LinkedList<>();

  private GemErpMedication baseMedication;
  private Medication.MedicationBatchComponent batch;

  protected EuMedicationDispenseBuilder(KVNR kvnr) {
    super(kvnr);
    this.version(EuVersion.getDefaultVersion());
  }

  public static EuMedicationDispenseBuilder forKvnr(KVNR kvnr) {
    return new EuMedicationDispenseBuilder(kvnr);
  }

  public EuMedicationDispenseBuilder whenPrepared(Date whenPrepared) {
    this.whenPrepared = whenPrepared;
    return this;
  }

  @Override
  public EuMedicationDispenseBuilder medication(GemErpMedication medication) {
    this.baseMedication = medication;
    return this;
  }

  public EuMedicationDispenseBuilder batch(String lotNumber, Date expirationDate) {
    val newBatch = new Medication.MedicationBatchComponent();
    newBatch.setLotNumber(lotNumber);
    newBatch.setExpirationDate(expirationDate);
    return batch(newBatch);
  }

  public EuMedicationDispenseBuilder batch(Medication.MedicationBatchComponent batch) {
    this.batch = batch;
    return this;
  }

  public EuMedicationDispenseBuilder performer(PractitionerRole practitionerRoleRef) {
    this.performerContains(practitionerRoleRef);
    return this;
  }

  public EuMedicationDispenseBuilder wasSubstituted(Boolean wasSubstituted) {
    this.wasSubstituted = wasSubstituted;
    return this;
  }

  public EuMedicationDispenseBuilder dosageInstruction(String instruction) {
    this.dosageInstructions.add(instruction);
    return this;
  }

  public EuMedicationDispenseBuilder note(String note) {
    this.notes.add(note);
    return this;
  }

  @Override
  public EuMedicationDispense build() {
    checkRequired();
    val medDisp =
        this.createResource(
            EuMedicationDispense::new, GemErpEuStructDef.EU_DISPENSATION, erpWorkflowVersion);
    buildBase(medDisp);

    medDisp.setMedication(
        new Reference(IdentifierUtil.getUnqualifiedId(this.baseMedication.getIdPart())));

    Optional.ofNullable(whenPrepared)
        .ifPresent(
            date ->
                medDisp.setWhenPreparedElement(new DateTimeType(date, TemporalPrecisionEnum.DAY)));
    Optional.ofNullable(batch).ifPresent(baseMedication::setBatch);
    Optional.ofNullable(wasSubstituted)
        .ifPresent(s -> medDisp.getSubstitution().setWasSubstituted(s));

    this.dosageInstructions.stream()
        .map(instruction -> new Dosage().setText(instruction))
        .forEach(medDisp::addDosageInstruction);

    this.notes.stream().map(note -> new Annotation().setText(note)).forEach(medDisp::addNote);

    return medDisp;
  }

  private void checkRequired() {
    this.checkRequired(baseMedication, "MedicationDispense requires a Medication");
  }
}
