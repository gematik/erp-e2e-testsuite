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

package de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer;

import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirResourceFuzz;
import de.gematik.test.fuzzing.fhirfuzz.impl.ListFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.IdentifierFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Ratio;

public class MedicationFuzzImpl implements FhirResourceFuzz<Medication> {

  private static final String ONLY_PROFILE = "OnlyProfile";
  private static final String TRUE = "TRUE";
  private final FuzzerContext fuzzerContext;

  public MedicationFuzzImpl(FuzzerContext fuzzerContext) {
    this.fuzzerContext = fuzzerContext;
  }

  /**
   * this Medication Fuzzer fuzzes entries anv Values. the intensity could be changed by setting
   * usedPercentOfMutators in fuzzConfig
   *
   * @param med you want to get fuzzed
   * @return the fuzzed med
   */
  @Override
  public Medication fuzz(Medication med) {
    val m = fuzzerContext.getRandomPart(getMutators());
    for (FuzzingMutator<Medication> f : m) {
      f.accept(med);
    }
    return med;
  }

  private List<FuzzingMutator<Medication>> getMutators() {
    val manipulators = new LinkedList<FuzzingMutator<Medication>>();
    manipulators.add(this::identFuzz);
    manipulators.add(this::metaFuzz);
    manipulators.add(this::idFuzz);
    manipulators.add(this::formFuzz);
    manipulators.add(this::extensionFuzz);
    manipulators.add(this::codeFuzz);
    manipulators.add(this::amountFuzz);
    manipulators.add(this::langFuzz);
    manipulators.add(this::statusFuzzer);
    if (getMapContent("BreakRanges").toLowerCase().matches("true")) {
      manipulators.add(this::rangeTexFuzz);
    }
    return manipulators;
  }

  private void langFuzz(Medication m) {
    var org = m.hasLanguage() ? m.getLanguage() : null;
    fuzzerContext.getLanguageCodeFuzzer().fuzz(m::hasLanguage, m::getLanguage, m::setLanguage);
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "set Language in Medication", org, m.hasLanguage() ? m.getLanguage() : null));
  }

  private void identFuzz(Medication m) {
    var fhirIdentifierFuzzer =
        fuzzerContext.getTypeFuzzerFor(
            Identifier.class, () -> new IdentifierFuzzerImpl(fuzzerContext));
    if (!m.hasIdentifier()) {
      val ident = fhirIdentifierFuzzer.generateRandom();
      m.setIdentifier(List.of(ident));
      fuzzerContext.addLog(new FuzzOperationResult<>("set Identifier in Medication:", null, ident));
    } else {
      val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, fhirIdentifierFuzzer);
      val ident = m.getIdentifier();
      listFuzzer.fuzz(m::getIdentifier, m::setIdentifier);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "set Identifier in Medication:",
              ident,
              m.hasIdentifier() ? m.getIdentifier() : null));
    }
  }

  private void metaFuzz(Medication m) {
    fuzzerContext.getFuzzConfig().getDetailSetup().put(ONLY_PROFILE, TRUE);
    val meta = m.hasMeta() ? m.getMeta() : null;
    fuzzerContext
        .getTypeFuzzerFor(Meta.class)
        .ifPresent(tf -> tf.fuzz(m::hasMeta, m::getMeta, m::setMeta));
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "set Meta in Medication:", meta, m.hasMeta() ? m.getMeta() : null));
    fuzzerContext.getFuzzConfig().getDetailSetup().remove(ONLY_PROFILE);
  }

  private void idFuzz(Medication m) {
    val orgId = m.hasId() ? m.getId() : null;
    fuzzerContext.getIdFuzzer().fuzz(m::hasId, m::getId, m::setId);
    fuzzerContext.addLog(
        new FuzzOperationResult<>("set ID in Medication:", orgId, m.hasId() ? m.getId() : null));
  }

  private void formFuzz(Medication m) {
    val orgCoding = m.hasForm() ? m.getForm().copy() : null;
    fuzzerContext
        .getTypeFuzzerFor(CodeableConcept.class)
        .ifPresent(tf -> tf.fuzz(m::hasForm, m::getForm, m::setForm));
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "fuzz Form in Medication:", orgCoding, m.hasForm() ? m.getForm() : null));
  }

  private void extensionFuzz(Medication m) {
    fuzzerContext.getFuzzConfig().getDetailSetup().put(ONLY_PROFILE, TRUE);
    val extensionFuzzer =
        fuzzerContext.getTypeFuzzerFor(
            Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext));
    if (!m.hasExtension()) {
      val ext = extensionFuzzer.generateRandom();
      m.setExtension(List.of(ext));
      fuzzerContext.addLog(new FuzzOperationResult<>("Extension in Medication", null, ext));
    } else {
      val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzzer);
      val org = m.getExtension();
      listFuzzer.fuzz(m::getExtension, m::setExtension);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "Extension in Medication", org, m.hasExtension() ? m.getExtension() : null));
      fuzzerContext.getFuzzConfig().getDetailSetup().remove(ONLY_PROFILE);
    }
  }

  private void codeFuzz(Medication m) {
    val codConceptFuzz = fuzzerContext.getTypeFuzzerFor(CodeableConcept.class);
    val orgCoding = m.hasCode() ? m.getCode().copy() : null;
    codConceptFuzz.ifPresent(tf -> tf.fuzz(m::hasCode, m::getCode, m::setCode));
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "fuzz Code in Medication:", orgCoding, m.hasCode() ? m.getCode() : null));
  }

  private void rangeTexFuzz(Medication m) {
    if (m.hasCode()) {
      m.getCode()
          .setText(
              fuzzerContext
                  .getStringFuzz()
                  .generateRandom(52)); // KBV define a maximum length of 50
    }
  }

  private void amountFuzz(Medication m) {
    val org = m.hasAmount() ? m.getAmount() : null;
    fuzzerContext
        .getTypeFuzzerFor(Ratio.class)
        .ifPresent(tf -> tf.fuzz(m::hasAmount, m::getAmount, m::setAmount));
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "set Amount in Medication:", org, m.hasAmount() ? m.getAmount() : null));
  }

  private void statusFuzzer(Medication m) {
    val status = m.hasStatus() ? m.getStatus() : null;
    if (status == null) {
      val newStatus =
          fuzzerContext.getRandomOneOfClass(
              Medication.MedicationStatus.class, Medication.MedicationStatus.NULL);
      m.setStatus(newStatus);
      fuzzerContext.addLog(new FuzzOperationResult<>("set Status in Medication:", null, newStatus));
    } else {
      val newStatus =
          fuzzerContext.getRandomOneOfClass(
              Medication.MedicationStatus.class, List.of(Medication.MedicationStatus.NULL, status));
      m.setStatus(newStatus);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("fuzz Status in Medication:", status, newStatus));
    }
  }

  @Override
  public Medication generateRandom() {
    val med = new Medication();
    fuzzerContext
        .getTypeFuzzerFor(Identifier.class)
        .ifPresent(tf -> med.setIdentifier(List.of(tf.generateRandom())));
    fuzzerContext.getTypeFuzzerFor(Meta.class).ifPresent(tf -> med.setMeta(tf.generateRandom()));
    med.setId(fuzzerContext.getIdFuzzer().generateRandom());
    fuzzerContext
        .getTypeFuzzerFor(CodeableConcept.class)
        .ifPresent(tf -> med.setForm(tf.generateRandom()));
    fuzzerContext
        .getTypeFuzzerFor(Extension.class)
        .ifPresent(tf -> med.setExtension(List.of(tf.generateRandom())));
    fuzzerContext
        .getTypeFuzzerFor(CodeableConcept.class)
        .ifPresent(tf -> med.setCode(tf.generateRandom()));
    fuzzerContext.getTypeFuzzerFor(Ratio.class).ifPresent(tf -> med.setAmount(tf.generateRandom()));
    med.setStatus(
        fuzzerContext.getRandomOneOfClass(
            Medication.MedicationStatus.class, List.of(Medication.MedicationStatus.NULL)));
    return med;
  }

  @Override
  public FuzzerContext getContext() {
    return fuzzerContext;
  }
}
