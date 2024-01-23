/*
 * Copyright 2023 gematik GmbH
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
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.HumanNameFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.IdentifierFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Practitioner;

public class PractitionerFuzzImpl implements FhirResourceFuzz<Practitioner> {
  private final FuzzerContext fuzzerContext;

  public PractitionerFuzzImpl(FuzzerContext fuzzerContext) {
    this.fuzzerContext = fuzzerContext;
  }

  @Override
  public Practitioner fuzz(Practitioner practitioner) {
    val m = fuzzerContext.getRandomPart(getMutators());
    for (FuzzingMutator<Practitioner> f : m) {
      f.accept(practitioner);
    }
    return practitioner;
  }

  private List<FuzzingMutator<Practitioner>> getMutators() {
    val manipulators = new LinkedList<FuzzingMutator<Practitioner>>();
    if (getMapContent("KBV").toLowerCase().matches("true")) {
      manipulators.add(this::idFuzz);
      manipulators.add(this::metaFuzz);
      manipulators.add(this::identifyFuzz);
      manipulators.add(this::nameFuzz);
    } else {
      manipulators.add(this::idFuzz);
      manipulators.add(this::langFuzz);
      manipulators.add(this::metaFuzz);
      manipulators.add(this::textFuzz);
      manipulators.add(this::identifyFuzz);
      manipulators.add(this::birthdayFuzz);
      manipulators.add(this::addressFuzz);
      manipulators.add(this::activeFuzz);
      manipulators.add(this::nameFuzz);
      manipulators.add(this::extensionFuzz);
      manipulators.add(this::telecomFuzz);
    }
    return manipulators;
  }

  @Override
  public Practitioner generateRandom() {
    val prac = new Practitioner();
    prac.setId(fuzzerContext.getIdFuzzer().generateRandom());
    fuzzerContext.getTypeFuzzerFor(Meta.class).ifPresent(tf -> prac.setMeta(tf.generateRandom()));
    fuzzerContext
        .getTypeFuzzerFor(Identifier.class)
        .ifPresent(tf -> prac.setIdentifier(List.of(tf.generateRandom())));
    fuzzerContext
        .getTypeFuzzerFor(HumanName.class)
        .ifPresent(tf -> prac.setName(List.of(tf.generateRandom())));
    return prac;
  }

  private void idFuzz(Practitioner p) {
    val id = p.hasId() ? p.getId() : null;
    fuzzerContext.getIdFuzzer().fuzz(p::hasId, p::getId, p::setId);
    fuzzerContext.addLog(
        new FuzzOperationResult<>("fuzzed Id in Practitioner", id, p.hasId() ? p.getId() : null));
  }

  private void langFuzz(Practitioner p) {
    var org = p.hasLanguage() ? p.getLanguage() : null;
    fuzzerContext.getLanguageCodeFuzzer().fuzz(p::hasLanguage, p::getLanguage, p::setLanguage);
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "set Language in Practitioner", org, p.hasLanguage() ? p.getLanguage() : null));
  }

  private void metaFuzz(Practitioner p) {
    val meta = p.hasMeta() ? p.getMeta() : null;
    fuzzerContext
        .getTypeFuzzerFor(Meta.class)
        .ifPresent(tf -> tf.fuzz(p::hasMeta, p::getMeta, p::setMeta));
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "fuzz Meta in Practitioner", meta, p.hasMeta() ? p.getMeta() : null));
  }

  private void textFuzz(Practitioner p) {
    val org = p.hasText() ? p.getText() : null;
    fuzzerContext
        .getTypeFuzzerFor(Narrative.class)
        .ifPresent(tf -> tf.fuzz(p::hasText, p::getText, p::setText));
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "fuzz Text in Practitioner", org, p.hasText() ? p.getText() : null));
  }

  private void identifyFuzz(Practitioner p) {
    val identifyFuzzer =
        fuzzerContext.getTypeFuzzerFor(
            Identifier.class, () -> new IdentifierFuzzerImpl(fuzzerContext));
    if (!p.hasIdentifier()) {
      val ident = identifyFuzzer.generateRandom();
      p.setIdentifier(List.of(ident));
      fuzzerContext.addLog(
          new FuzzOperationResult<>("set Identifier in Practitioner", null, ident));
    } else {
      val org = p.getIdentifierFirstRep().copy();
      val listFuzz = new ListFuzzerImpl<>(fuzzerContext, identifyFuzzer);
      listFuzz.fuzz(p::getIdentifier, p::setIdentifier);
      // nullSave for KBV profile
      if (!p.hasIdentifier())
        fuzzerContext
            .getTypeFuzzerFor(Identifier.class)
            .ifPresent(tf -> p.setIdentifier(List.of(tf.generateRandom())));
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "fuzz Identifier in Practitioner",
              org,
              p.hasIdentifier() ? p.getIdentifierFirstRep() : null));
    }
  }

  private void birthdayFuzz(Practitioner p) {
    String info = "fuzz BirthDate in Practitioner";
    if (!p.hasBirthDate()) {
      val birth = fuzzerContext.getRandomDate(5);
      p.setBirthDate(birth);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("set new BirthDate in Practitioner", null, birth));
    } else {
      if (fuzzerContext.conditionalChance()) {
        val org = p.getBirthDate();
        p.setBirthDate(null);
        fuzzerContext.addLog(new FuzzOperationResult<>(info, org, null));
      } else {
        val org = p.getBirthDate();
        val birth = fuzzerContext.getRandomDate();
        p.setBirthDate(birth);
        fuzzerContext.addLog(new FuzzOperationResult<>(info, org, birth));
      }
    }
  }

  private void addressFuzz(Practitioner p) {
    val addressFuzzer =
        fuzzerContext.getTypeFuzzerFor(Address.class, () -> new AddressFuzzerImpl(fuzzerContext));
    if (!p.hasAddress()) {
      p.setAddress(List.of(addressFuzzer.generateRandom()));
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "set Address in Practitioner", null, p.hasAddress() ? p.getAddress() : null));
    } else {
      val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, addressFuzzer);
      listFuzzer.fuzz(p::getAddress, p::setAddress);
    }
  }

  private void activeFuzz(Practitioner p) {
    if (!p.hasActive()) {
      p.setActive(true);
      fuzzerContext.addLog(new FuzzOperationResult<>("set Active in Practitioner", null, true));
    } else {
      val old = p.getActive();
      val active = !old;
      p.setActive(active);
      fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Language in Practitioner", old, active));
    }
  }

  private void nameFuzz(Practitioner p) {
    val nameFuzzer =
        fuzzerContext.getTypeFuzzerFor(
            HumanName.class, () -> new HumanNameFuzzerImpl(fuzzerContext));
    if (!p.hasName()) {
      val hName = nameFuzzer.generateRandom();
      p.setName(List.of(hName));
      fuzzerContext.addLog(
          new FuzzOperationResult<>("set HumanName 1st Entry in Practitioner", null, hName));
    } else {
      val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, nameFuzzer);
      listFuzzer.fuzz(p::getName, p::setName);
    }
  }

  private void extensionFuzz(Practitioner p) {
    val extensionFuzz =
        fuzzerContext.getTypeFuzzerFor(
            Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext));
    if (!p.hasExtension()) {
      val ex = extensionFuzz.generateRandom();
      p.setExtension(List.of(ex));
      fuzzerContext.addLog(
          new FuzzOperationResult<>("set Extension in Practitioner", null, ex.getUrl()));
    } else {
      val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzz);
      val org = p.getExtension();
      listFuzzer.fuzz(p::getExtension, p::setExtension);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "fuz Extension in Practitioner", org, p.hasExtension() ? p.getExtension() : null));
    }
  }

  private void telecomFuzz(Practitioner p) {
    val contPointFuzz =
        fuzzerContext.getTypeFuzzerFor(
            ContactPoint.class, () -> new ContactPointFuzzImpl(fuzzerContext));
    val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, contPointFuzz);
    val cp = p.hasTelecom() ? p.getTelecom() : null;
    if (cp == null) {
      val newVal = contPointFuzz.generateRandom();
      p.setTelecom(List.of(newVal));
      fuzzerContext.addLog(
          new FuzzOperationResult<>("fuzzed Telecom in Practitioner", null, newVal));
    } else {
      listFuzzer.fuzz(p::getTelecom, p::setTelecom);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "fuzzed Id in Practitioner", cp, p.hasTelecom() ? p.getTelecom() : null));
    }
  }

  @Override
  public FuzzerContext getContext() {
    return fuzzerContext;
  }
}
