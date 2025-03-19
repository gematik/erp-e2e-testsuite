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

package de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer;

import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirResourceFuzz;
import de.gematik.test.fuzzing.fhirfuzz.impl.ListFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ReferenceFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;

public class CoverageFuzzImpl implements FhirResourceFuzz<Coverage> {
  private final FuzzerContext fuzzerContext;

  public CoverageFuzzImpl(FuzzerContext fuzzerContext) {
    this.fuzzerContext = fuzzerContext;
  }

  public Coverage fuzz(Coverage coverage) {
    val m = fuzzerContext.getRandomPart(getMutators());
    for (FuzzingMutator<Coverage> f : m) {
      f.accept(coverage);
    }
    return coverage;
  }

  private List<FuzzingMutator<Coverage>> getMutators() {
    val manipulators = new LinkedList<FuzzingMutator<Coverage>>();
    if (getMapContent("KBV").toLowerCase().matches("true")) {
      manipulators.add(this::idFuzz);
      manipulators.add(this::metaFuzz);
      manipulators.add(this::extensionFuzz);
      manipulators.add(this::statusFuzzer);
      manipulators.add(this::typeFuzz);
      manipulators.add(this::beneficiarityFuzz);
      manipulators.add(this::periodFuzz);
    } else {
      manipulators.add(this::idFuzz);
      manipulators.add(this::metaFuzz);
      manipulators.add(this::extensionFuzz);
      manipulators.add(this::statusFuzzer);
      manipulators.add(this::beneficiarityFuzz);
      manipulators.add(this::typeFuzz);
      manipulators.add(this::payorFuzz);
      manipulators.add(this::periodFuzz);
    }
    return manipulators;
  }

  @Override
  public Coverage generateRandom() {
    val cov = new Coverage();
    cov.setId(fuzzerContext.getIdFuzzer().generateRandom());
    fuzzerContext.getTypeFuzzerFor(Meta.class).ifPresent(tf -> cov.setMeta(tf.generateRandom()));
    fuzzerContext
        .getTypeFuzzerFor(Extension.class)
        .ifPresent(tf -> cov.addExtension(tf.generateRandom()));
    cov.setStatus(
        fuzzerContext.getRandomOneOfClass(
            Coverage.CoverageStatus.class, Coverage.CoverageStatus.NULL));
    fuzzerContext
        .getTypeFuzzerFor(CodeableConcept.class)
        .ifPresent(tf -> cov.setType(tf.generateRandom()));
    fuzzerContext
        .getTypeFuzzerFor(Reference.class)
        .ifPresent(tf -> cov.setBeneficiary(tf.generateRandom()));
    fuzzerContext
        .getTypeFuzzerFor(Period.class)
        .ifPresent(tf -> cov.setPeriod(tf.generateRandom()));
    fuzzerContext
        .getTypeFuzzerFor(Reference.class)
        .ifPresent(tf -> cov.setPayor(List.of(tf.generateRandom())));
    return cov;
  }

  private void payorFuzz(Coverage c) {
    val referenceFuzzer =
        fuzzerContext.getTypeFuzzerFor(
            Reference.class, () -> new ReferenceFuzzerImpl(fuzzerContext));
    val org = c.hasPayor() ? c.getPayorFirstRep() : null;
    if (org == null) {
      val newEntry = referenceFuzzer.generateRandom();
      c.setPayor(List.of(newEntry));
      fuzzerContext.addLog(new FuzzOperationResult<>("set Payor in Coverage", null, newEntry));
    } else {
      val listFuzz = new ListFuzzerImpl<>(fuzzerContext, referenceFuzzer);
      listFuzz.fuzz(c::getPayor, c::setPayor);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "set Payor in Coverage", org, c.hasPayor() ? c.getPayorFirstRep() : null));
    }
  }

  private void periodFuzz(Coverage c) {
    if (!c.hasPeriod()) {
      val newEntry = new Period().setStart(fuzzerContext.getRandomDate());
      c.setPeriod(newEntry);
      fuzzerContext.addLog(new FuzzOperationResult<>("set Period in Coverage", null, newEntry));
    } else {
      val org = c.getPeriod();
      fuzzerContext
          .getTypeFuzzerFor(Period.class)
          .ifPresent(tf -> tf.fuzz(c::getPeriod, c::setPeriod));
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "fuzz Period in Coverage", org, c.hasPeriod() ? c.getPeriod() : null));
    }
  }

  private void beneficiarityFuzz(Coverage c) {
    val org = c.hasBeneficiary() ? c.getBeneficiary() : null;
    fuzzerContext
        .getTypeFuzzerFor(Reference.class)
        .ifPresent(tf -> tf.fuzz(c::hasBeneficiary, c::getBeneficiary, c::setBeneficiary));
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "set Beneficiary in Coverage", org, c.hasBeneficiary() ? c.getBeneficiary() : null));
  }

  private void typeFuzz(Coverage c) {
    val orgCoding = c.hasType() ? c.getType().copy() : null;
    fuzzerContext
        .getTypeFuzzerFor(CodeableConcept.class)
        .ifPresent(tf -> tf.fuzz(c::hasType, c::getType, c::setType));
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "fuzz Type in Coverage:", orgCoding, c.hasType() ? c.getType() : null));
  }

  private void statusFuzzer(Coverage c) {
    val status = c.hasStatus() ? c.getStatus() : null;
    if (status == null) {
      val newStatus =
          fuzzerContext.getRandomOneOfClass(
              Coverage.CoverageStatus.class, Coverage.CoverageStatus.NULL);
      c.setStatus(newStatus);
      fuzzerContext.addLog(new FuzzOperationResult<>("set Status in Coverage:", null, newStatus));
    } else {
      val newStatus =
          fuzzerContext.getRandomOneOfClass(
              Coverage.CoverageStatus.class, List.of(Coverage.CoverageStatus.NULL, status));
      c.setStatus(newStatus);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("fuzz Status in Coverage:", status, newStatus));
    }
  }

  private void extensionFuzz(Coverage c) {
    if (fuzzerContext.getFuzzConfig().getDetailSetup() == null)
      fuzzerContext.getFuzzConfig().setDetailSetup(new HashMap<>());
    fuzzerContext.getFuzzConfig().getDetailSetup().put("TriggertByCoverage", "TRUE");
    val extensionFuzzer =
        fuzzerContext.getTypeFuzzerFor(
            Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext));
    if (!c.hasExtension()) {
      val ext = extensionFuzzer.generateRandom();
      c.setExtension(List.of(ext));
      fuzzerContext.addLog(new FuzzOperationResult<>("Extension in Coverage", null, ext));
    } else {
      val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzzer);
      val org = c.getExtension();
      listFuzzer.fuzz(c::getExtension, c::setExtension);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "Extension in Coverage", org, c.hasExtension() ? c.getExtension() : null));
    }
    fuzzerContext.getFuzzConfig().getDetailSetup().remove("TriggertByCoverage");
  }

  private void metaFuzz(Coverage c) {
    val meta = c.hasMeta() ? c.getMeta() : null;
    fuzzerContext
        .getTypeFuzzerFor(Meta.class)
        .ifPresent(tf -> tf.fuzz(c::hasMeta, c::getMeta, c::setMeta));
    fuzzerContext.addLog(
        new FuzzOperationResult<>("set Meta in Coverage:", meta, c.hasMeta() ? c.getMeta() : null));
  }

  private void idFuzz(Coverage c) {
    val orgId = c.hasId() ? c.getId() : null;
    fuzzerContext.getUrlFuzz().fuzz(c::hasId, c::getId, c::setId);
    fuzzerContext.addLog(
        new FuzzOperationResult<>("set ID in Coverage:", orgId, c.hasId() ? c.getId() : null));
  }

  @Override
  public FuzzerContext getContext() {
    return fuzzerContext;
  }
}
