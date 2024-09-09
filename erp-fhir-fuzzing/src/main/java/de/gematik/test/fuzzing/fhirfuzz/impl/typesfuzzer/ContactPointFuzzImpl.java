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

package de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer;

import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirTypeFuzz;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Period;

public class ContactPointFuzzImpl implements FhirTypeFuzz<ContactPoint> {

  final FuzzerContext fuzzerContext;

  public ContactPointFuzzImpl(FuzzerContext fuzzerContext) {
    this.fuzzerContext = fuzzerContext;
  }

  @Override
  public ContactPoint fuzz(ContactPoint cp) {
    val m = fuzzerContext.getRandomPart(getMutators());
    for (FuzzingMutator<ContactPoint> f : m) {
      f.accept(cp);
    }
    return cp;
  }

  private List<FuzzingMutator<ContactPoint>> getMutators() {
    val manipulators = new LinkedList<FuzzingMutator<ContactPoint>>();
    manipulators.add(this::systemFuzz);
    manipulators.add(this::fuzzText);
    manipulators.add(this::periodFuzz);
    manipulators.add(this::fuzzRank);
    manipulators.add(this::useFuzz);
    return manipulators;
  }

  @Override
  public ContactPoint generateRandom() {
    var contPoint = new ContactPoint();
    contPoint.setSystem(
        fuzzerContext.getRandomOneOfClass(
            ContactPoint.ContactPointSystem.class, ContactPoint.ContactPointSystem.NULL));
    contPoint.setValue(fuzzerContext.getStringFuzz().generateRandom());
    contPoint.setUse(
        fuzzerContext.getRandomOneOfClass(
            ContactPoint.ContactPointUse.class, ContactPoint.ContactPointUse.NULL));
    contPoint.setRank(fuzzerContext.nextInt());
    fuzzerContext
        .getTypeFuzzerFor(Period.class)
        .ifPresent(tf -> contPoint.setPeriod(tf.generateRandom()));
    return contPoint;
  }

  private void systemFuzz(ContactPoint c) {
    if (!c.hasSystem()) {
      val compStatus =
          fuzzerContext.getRandomOneOfClass(
              ContactPoint.ContactPointSystem.class, ContactPoint.ContactPointSystem.NULL);
      c.setSystem(compStatus);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("set Status in ContactPoint:", null, compStatus));
    } else {
      val org = c.getSystem();
      val newEntry =
          fuzzerContext.getRandomOneOfClass(
              ContactPoint.ContactPointSystem.class,
              List.of(org, ContactPoint.ContactPointSystem.NULL));
      c.setSystem(newEntry);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("fuzz Status in ContactPoint:", org, newEntry));
    }
  }

  private void useFuzz(ContactPoint c) {
    if (!c.hasUse()) {
      val conUse =
          fuzzerContext.getRandomOneOfClass(
              ContactPoint.ContactPointUse.class, ContactPoint.ContactPointUse.NULL);
      c.setUse(conUse);
      fuzzerContext.addLog(new FuzzOperationResult<>("set Status in ContactPoint:", null, conUse));
    } else {
      val org = c.getUse();
      val newEntry =
          fuzzerContext.getRandomOneOfClass(
              ContactPoint.ContactPointUse.class, List.of(org, ContactPoint.ContactPointUse.NULL));
      c.setUse(newEntry);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("fuzz Status in ContactPoint:", org, newEntry));
    }
  }

  private void fuzzText(ContactPoint c) {
    val value = c.hasValue() ? c.getValue() : null;
    fuzzerContext.getStringFuzz().fuzz(c::hasValue, c::getValue, c::setValue);
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "fuzz Value in ContactPoint ", value, c.hasValue() ? c.getValue() : null));
  }

  private void fuzzRank(ContactPoint c) {
    val org = c.hasRank() ? c.getRank() : null;
    fuzzerContext.getIntFuzz().fuzz(c::hasRank, c::getRank, c::setRank);
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "fuzz Value in ContactPoint ", org, c.hasRank() ? c.getRank() : null));
  }

  private void periodFuzz(ContactPoint c) {
    val org = c.hasPeriod() ? c.getPeriod() : null;
    fuzzerContext
        .getTypeFuzzerFor(Period.class)
        .ifPresent(tf -> tf.fuzz(c::hasPeriod, c::getPeriod, c::setPeriod));
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "fuzz Period in ContactPoint", org, c.hasPeriod() ? c.getPeriod() : null));
  }

  @Override
  public FuzzerContext getContext() {
    return fuzzerContext;
  }
}
