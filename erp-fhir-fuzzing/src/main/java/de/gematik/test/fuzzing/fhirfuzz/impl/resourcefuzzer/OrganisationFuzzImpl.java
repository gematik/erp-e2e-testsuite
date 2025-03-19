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
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.AddressFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ContactPointFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.IdentifierFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Organization;

public class OrganisationFuzzImpl implements FhirResourceFuzz<Organization> {
  private final FuzzerContext fuzzerContext;

  public OrganisationFuzzImpl(FuzzerContext fuzzerContext) {
    this.fuzzerContext = fuzzerContext;
  }

  @Override
  public Organization fuzz(Organization org) {
    val m = fuzzerContext.getRandomPart(getMutators());
    for (FuzzingMutator<Organization> f : m) {
      f.accept(org);
    }
    return org;
  }

  @Override
  public Organization generateRandom() {
    val org = new Organization();
    org.setId(fuzzerContext.getIdFuzzer().generateRandom());
    fuzzerContext.getTypeFuzzerFor(Meta.class).ifPresent(tf -> org.setMeta(tf.generateRandom()));
    org.setLanguage(fuzzerContext.getLanguageCodeFuzzer().generateRandom());
    fuzzerContext
        .getTypeFuzzerFor(Narrative.class)
        .ifPresent(tf -> org.setText(tf.generateRandom()));
    fuzzerContext
        .getTypeFuzzerFor(Extension.class)
        .ifPresent(tf -> org.setExtension(List.of(tf.generateRandom())));
    fuzzerContext
        .getTypeFuzzerFor(Identifier.class)
        .ifPresent(tf -> org.setIdentifier(List.of(tf.generateRandom())));
    org.setName(fuzzerContext.getStringFuzz().generateRandom(15));
    fuzzerContext
        .getTypeFuzzerFor(ContactPoint.class)
        .ifPresent(tf -> org.setTelecom(List.of(tf.generateRandom())));
    fuzzerContext
        .getTypeFuzzerFor(Address.class)
        .ifPresent(tf -> org.setAddress(List.of(tf.generateRandom())));
    return org;
  }

  private List<FuzzingMutator<Organization>> getMutators() {
    val manipulators = new LinkedList<FuzzingMutator<Organization>>();
    manipulators.add(this::idFuzz);
    manipulators.add(this::metaFuzz);
    manipulators.add(this::identifyFuzz);
    manipulators.add(this::langFuzz);
    manipulators.add(this::activeFuzz);
    manipulators.add(this::textFuzz);
    manipulators.add(this::nameFuzz);
    manipulators.add(this::addressFuzz);
    manipulators.add(this::extensionFuzz);
    manipulators.add((this::telecomFuzz));
    return manipulators;
  }

  private void telecomFuzz(Organization o) {
    val contPointFuzz =
        fuzzerContext.getTypeFuzzerFor(
            ContactPoint.class, () -> new ContactPointFuzzImpl(fuzzerContext));
    val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, contPointFuzz);
    val cp = o.hasTelecom() ? o.getTelecom() : null;
    if (cp == null) {
      val newVal = contPointFuzz.generateRandom();
      o.setTelecom(List.of(newVal));
      fuzzerContext.addLog(
          new FuzzOperationResult<>("fuzzed Telecom in Organization", null, newVal));
    } else {
      listFuzzer.fuzz(o::getTelecom, o::setTelecom);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "fuzzed Telecom in Organization",
              cp,
              o.hasTelecom()
                  ? o.getTelecom().stream().map(Object::toString).collect(Collectors.joining(", "))
                  : null));
    }
  }

  private void idFuzz(Organization o) {
    val id = o.hasId() ? o.getId() : null;
    fuzzerContext.getIdFuzzer().fuzz(o::hasId, o::getId, o::setId);
    if (!o.hasId()) o.setId(fuzzerContext.getIdFuzzer().generateRandom());
    fuzzerContext.addLog(
        new FuzzOperationResult<>("fuzzed Id in Organization", id, o.hasId() ? o.getId() : null));
  }

  private void metaFuzz(Organization o) {
    val meta = o.hasMeta() ? o.getMeta() : null;
    fuzzerContext
        .getTypeFuzzerFor(Meta.class)
        .ifPresent(tf -> tf.fuzz(o::hasMeta, o::getMeta, o::setMeta));
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "fuzz Meta in Organization", meta, o.hasMeta() ? o.getMeta() : null));
  }

  private void identifyFuzz(Organization o) {
    if (!o.hasIdentifier()) {
      fuzzerContext
          .getTypeFuzzerFor(Identifier.class)
          .ifPresent(tf -> o.setIdentifier(List.of(tf.generateRandom())));
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "set Identifier in Organization",
              null,
              o.hasIdentifier() ? o.getIdentifier() : null));
    } else {
      val org = o.getIdentifierFirstRep().copy();
      val listFuzz =
          new ListFuzzerImpl<>(
              fuzzerContext,
              fuzzerContext.getTypeFuzzerFor(
                  Identifier.class, () -> new IdentifierFuzzerImpl(fuzzerContext)));
      listFuzz.fuzz(o::getIdentifier, o::setIdentifier);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "fuzz Identifier in Organization:",
              org,
              o.hasIdentifier() ? o.getIdentifierFirstRep() : null));
    }
  }

  private void langFuzz(Organization o) {
    var org = o.hasLanguage() ? o.getLanguage() : null;
    fuzzerContext.getLanguageCodeFuzzer().fuzz(o::getLanguage, o::setLanguage);
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "set Language in Organization", org, o.hasLanguage() ? o.getLanguage() : null));
  }

  private void activeFuzz(Organization o) {
    if (!o.hasActive()) {
      o.setActive(true);
      fuzzerContext.addLog(new FuzzOperationResult<>("set active in Organization", null, true));
    } else {
      val old = o.getActive();
      val active = !old;
      o.setActive(active);
      fuzzerContext.addLog(new FuzzOperationResult<>("fuzz active in Organization", old, active));
    }
  }

  private void textFuzz(Organization o) {

    val org = o.hasText() ? o.getText() : null;
    fuzzerContext
        .getTypeFuzzerFor(Narrative.class)
        .ifPresent(tf -> tf.fuzz(o::hasText, o::getText, o::setText));
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "fuzz Text in Organization", org, o.hasText() ? o.getText() : null));
  }

  private void nameFuzz(Organization o) {
    val org = o.hasName() ? o.getName() : null;
    fuzzerContext.getStringFuzz().fuzz(o::getName, o::setName);
    fuzzerContext.addLog(
        new FuzzOperationResult<>("Name  in Organization", org, o.hasName() ? o.getName() : null));
  }

  private void extensionFuzz(Organization o) {
    val extensionFuzz =
        fuzzerContext.getTypeFuzzerFor(
            Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext));
    if (!o.hasExtension()) {
      val ex = extensionFuzz.generateRandom();
      o.setExtension(List.of(ex));
      fuzzerContext.addLog(
          new FuzzOperationResult<>("set Extension in Organization", null, ex.getValue()));
    } else {
      val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzz);
      val org = o.getExtension();
      listFuzzer.fuzz(o::getExtension, o::setExtension);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "fuzz Extension in Organization", org, o.hasExtension() ? o.getExtension() : null));
    }
  }

  private void addressFuzz(Organization o) {
    val addressFuzzer =
        fuzzerContext.getTypeFuzzerFor(Address.class, () -> new AddressFuzzerImpl(fuzzerContext));
    if (!o.hasAddress()) {
      val address = addressFuzzer.generateRandom();
      o.setAddress(List.of(address));
      fuzzerContext.addLog(new FuzzOperationResult<>("set Address in Organization", null, address));
    } else {
      val org = o.hasAddress() ? o.getAddress() : null;
      val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, addressFuzzer);
      listFuzzer.fuzz(o::getAddress, o::setAddress);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "fuzz Address in Organization", org, o.hasAddress() ? o.getAddress() : null));
    }
  }

  @Override
  public FuzzerContext getContext() {
    return fuzzerContext;
  }
}
