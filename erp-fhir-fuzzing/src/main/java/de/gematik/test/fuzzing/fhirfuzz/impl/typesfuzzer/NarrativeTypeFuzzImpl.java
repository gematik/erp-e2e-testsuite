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
import de.gematik.test.fuzzing.fhirfuzz.impl.ListFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.utilities.xhtml.XhtmlDocument;

public class NarrativeTypeFuzzImpl implements FhirTypeFuzz<Narrative> {
  private final FuzzerContext fuzzerContext;

  public NarrativeTypeFuzzImpl(FuzzerContext fuzzerContext) {
    this.fuzzerContext = fuzzerContext;
  }

  @Override
  public FuzzerContext getContext() {
    return fuzzerContext;
  }

  @Override
  public void fuzz(Supplier<Narrative> getter, Consumer<Narrative> setter) {
    FhirTypeFuzz.super.fuzz(getter, setter);
  }

  @Override
  public Narrative fuzz(Narrative type) {
    val m = fuzzerContext.getRandomPart(getMutators());
    for (val f : m) {
      f.accept(type);
    }
    return type;
  }

  public Narrative generateRandom() {
    Narrative narrative = new Narrative();
    narrative.setStatus(
        fuzzerContext.getRandomOneOfClass(
            Narrative.NarrativeStatus.class, Narrative.NarrativeStatus.NULL));
    narrative.setDiv(new XhtmlDocument().setValue(fuzzerContext.getStringFuzz().generateRandom()));
    narrative.setId(fuzzerContext.getIdFuzzer().generateRandom());
    return narrative;
  }

  private List<FuzzingMutator<Narrative>> getMutators() {
    val manipulators = new LinkedList<FuzzingMutator<Narrative>>();
    manipulators.add(this::fuzzExtension);
    manipulators.add(this::fuzzStatus);
    manipulators.add(this::fuzzId);
    return manipulators;
  }

  private void fuzzStatus(Narrative n) {
    if (!n.hasStatus()) {
      val nStatus =
          fuzzerContext.getRandomOneOfClass(
              Narrative.NarrativeStatus.class, Narrative.NarrativeStatus.NULL);
      n.setStatus(nStatus);
      fuzzerContext.addLog(new FuzzOperationResult<>("set Status at Narrative", null, nStatus));
    } else {
      val org = n.getStatus();
      val newNar =
          fuzzerContext.getRandomOneOfClass(
              Narrative.NarrativeStatus.class, List.of(org, Narrative.NarrativeStatus.NULL));
      n.setStatus(newNar);
      fuzzerContext.addLog(new FuzzOperationResult<>("Changes Status at Narrative", org, newNar));
    }
  }

  private void fuzzId(Narrative n) {
    if (!n.hasId()) {
      val newId = fuzzerContext.getIdFuzzer().generateRandom();
      n.setId(newId);
      fuzzerContext.addLog(new FuzzOperationResult<>("set Id at Narrative", null, newId));
    } else {
      val org = n.getId();
      fuzzerContext.getIdFuzzer().fuzz(n::getId, n::setId);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("Changes ID at Narrative", org, n.hasId() ? n.getId() : null));
    }
  }

  private void fuzzExtension(Narrative n) {
    val extensionFuzzer =
        fuzzerContext.getTypeFuzzerFor(
            Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext));
    if (!n.hasExtension()) {
      val ext = extensionFuzzer.generateRandom();
      n.setExtension(List.of(ext));
      fuzzerContext.addLog(new FuzzOperationResult<>("Extension in Narrative", null, ext));
    } else {
      val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzzer);
      listFuzzer.fuzz(n::getExtension, n::setExtension);
    }
  }
}
