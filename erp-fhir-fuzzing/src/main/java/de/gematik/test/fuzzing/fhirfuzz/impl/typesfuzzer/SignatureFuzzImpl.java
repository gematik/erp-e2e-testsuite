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

package de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer;

import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirTypeFuzz;
import de.gematik.test.fuzzing.fhirfuzz.impl.ListFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Signature;

public class SignatureFuzzImpl implements FhirTypeFuzz<Signature> {

  private final FuzzerContext fuzzerContext;

  public SignatureFuzzImpl(FuzzerContext fuzzerContext) {
    this.fuzzerContext = fuzzerContext;
  }

  @Override
  public Signature fuzz(Signature sig) {
    val m = fuzzerContext.getRandomPart(getMutators());
    for (val f : m) {
      f.accept(sig);
    }
    return sig;
  }

  private List<FuzzingMutator<Signature>> getMutators() {
    val manipulators = new LinkedList<FuzzingMutator<Signature>>();
    manipulators.add(this::fuzzType);
    manipulators.add(this::fuzzWho);
    manipulators.add(this::fuzzId);
    manipulators.add(this::fuzzWhen);
    manipulators.add(this::fuzzOnBehalfOf);
    manipulators.add(this::fuzzSigFormat);
    manipulators.add(this::fuzzTargetFormat);
    manipulators.add(this::fuzzData);
    manipulators.add(this::extensionFuzz);
    return manipulators;
  }

  private void fuzzData(Signature s) {
    val org = s.hasData() ? s.getData() : null;
    if (org == null) {
      val data = (fuzzerContext.getIdFuzzer().generateRandom()).getBytes();
      s.setData(data);
      fuzzerContext.addLog(new FuzzOperationResult<>("set Data in Signature :", null, data));
    } else {
      if (Boolean.TRUE.equals(fuzzerContext.shouldFuzz(s.getData()))) {
        s.setData(null);
        fuzzerContext.addLog(new FuzzOperationResult<>("set Data in Signature :", org, null));
      } else {
        fuzzerContext.getStringFuzz().fuzz(String.valueOf(org));
        fuzzerContext.addLog(
            new FuzzOperationResult<>(
                "fuzz Data in Signature :", org.toString(), s.hasData() ? s.getData() : null));
      }
    }
  }

  private void fuzzTargetFormat(Signature s) {
    val org = s.hasTargetFormat() ? s.getTargetFormat() : null;
    fuzzerContext.getStringFuzz().fuzz(s::hasTargetFormat, s::getTargetFormat, s::setTargetFormat);
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "set TargetFormat in Signature :",
            org,
            s.hasTargetFormat() ? s.getTargetFormat() : null));
  }

  private void fuzzSigFormat(Signature s) {
    val org = s.hasSigFormat() ? s.getSigFormat() : null;
    fuzzerContext.getStringFuzz().fuzz(s::hasSigFormat, s::getSigFormat, s::setSigFormat);
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "set SigFormat in Signature :", org, s.hasSigFormat() ? s.getSigFormat() : null));
  }

  private void fuzzType(Signature s) {
    val org = s.hasType() ? s.getType() : null;
    val codingTypeFuzz = fuzzerContext.getTypeFuzzerFor(Coding.class);
    if (org == null) {
      codingTypeFuzz.ifPresent(tf -> s.setType(List.of(tf.generateRandom())));
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "set Type in Signature :", org, s.hasType() ? s.getType() : null));
    } else {
      val typeList = s.getType();
      for (val t : typeList) {
        val orgEntry = t.copy();
        codingTypeFuzz.ifPresent(tf -> tf.fuzz(t));
        fuzzerContext.addLog(
            new FuzzOperationResult<>("fuzz Type Element in Signature :", orgEntry, t));
      }
    }
  }

  private void fuzzWhen(Signature s) {
    val infoText = "set When in Signature";
    if (!s.hasWhen()) {
      var date = fuzzerContext.getRandomDate();
      s.setWhen(date);
      fuzzerContext.addLog(new FuzzOperationResult<>(infoText, null, date));
    } else if (Boolean.TRUE.equals(fuzzerContext.shouldFuzz(s.getWhen()))) {
      var orgDate = s.getWhen();
      s.setWhen(null);
      fuzzerContext.addLog(new FuzzOperationResult<>(infoText, orgDate, null));
    } else {
      var orgDate = s.getWhen();
      var newDate = new DateTimeType(fuzzerContext.getRandomDate());
      s.setWhen(fuzzerContext.getRandomDate());
      fuzzerContext.addLog(new FuzzOperationResult<>("fuzz When in Signature:", orgDate, newDate));
    }
  }

  private void fuzzWho(Signature s) {
    val org = s.hasWho() ? s.getWho() : null;
    val refFusser = fuzzerContext.getTypeFuzzerFor(Reference.class);
    refFusser.ifPresent(tf -> tf.fuzz(s::hasWho, s::getWho, s::setWho));
    fuzzerContext.addLog(
        new FuzzOperationResult<>("fuzz Who in Signature :", org, s.hasWho() ? s.getWho() : null));
  }

  private void fuzzOnBehalfOf(Signature s) {
    val org = s.hasOnBehalfOf() ? s.getOnBehalfOf() : null;
    val refFusser = fuzzerContext.getTypeFuzzerFor(Reference.class);
    refFusser.ifPresent(tf -> tf.fuzz(s::hasOnBehalfOf, s::getOnBehalfOf, s::setOnBehalfOf));
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "fuzz onBehalfOf in Signature :", org, s.hasOnBehalfOf() ? s.getOnBehalfOf() : null));
  }

  private void fuzzId(Signature s) {
    val id = s.hasId() ? s.getId() : null;
    fuzzerContext.getIdFuzzer().fuzz(s::hasId, s::getId, s::setId);
    fuzzerContext.addLog(
        new FuzzOperationResult<>("fuzz Id in Signature ", id, s.hasId() ? s.getId() : null));
  }

  private void extensionFuzz(Signature s) {
    val extensionFuzzer =
        fuzzerContext.getTypeFuzzerFor(
            Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext));
    if (!s.hasExtension()) {
      val ext = extensionFuzzer.generateRandom();
      s.setExtension(List.of(ext));
      fuzzerContext.addLog(new FuzzOperationResult<>("Extension in Signature", null, ext));
    } else {
      val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzzer);
      val org = s.getExtension();
      listFuzzer.fuzz(s::getExtension, s::setExtension);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "Extension in Signature", org, s.hasExtension() ? s.getExtension() : null));
    }
  }

  @Override
  public Signature generateRandom() {
    val sig = new Signature();
    fuzzerContext.getTypeFuzzerFor(Coding.class).ifPresent(tf -> sig.addType(tf.generateRandom()));
    sig.setWhen(fuzzerContext.getRandomDate());
    fuzzerContext
        .getTypeFuzzerFor(Reference.class)
        .ifPresent(tf -> sig.setWho(tf.generateRandom()));
    sig.setTargetFormat(fuzzerContext.getIdFuzzer().generateRandom());
    sig.setSigFormat(fuzzerContext.getIdFuzzer().generateRandom());
    sig.setData((fuzzerContext.getIdFuzzer().generateRandom()).getBytes());
    sig.setId(fuzzerContext.getIdFuzzer().generateRandom());
    fuzzerContext
        .getTypeFuzzerFor(Extension.class)
        .ifPresent(tf -> sig.setExtension(List.of(tf.generateRandom())));
    return sig;
  }

  @Override
  public FuzzerContext getContext() {
    return this.fuzzerContext;
  }
}
