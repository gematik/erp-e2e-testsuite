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

package de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer;

import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.fhirfuzz.FhirTypeFuzz;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.StringType;

public class AnnotationTypeFuzzImpl implements FhirTypeFuzz<Annotation> {
  private final FuzzerContext fuzzerContext;

  public AnnotationTypeFuzzImpl(FuzzerContext fuzzerContext) {
    this.fuzzerContext = fuzzerContext;
  }

  @Override
  public Annotation fuzz(Annotation value) {
    val m = fuzzerContext.getRandomPart(getMutators());
    for (FuzzingMutator<Annotation> f : m) {
      f.accept(value);
    }
    return value;
  }

  private List<FuzzingMutator<Annotation>> getMutators() {
    val manipulators = new LinkedList<FuzzingMutator<Annotation>>();
    manipulators.add(this::fuzzText);
    manipulators.add(this::fuzzAuth);
    manipulators.add(this::fuzzTime);
    return manipulators;
  }

  private void fuzzText(Annotation a) {
    val text = a.hasText() ? a.getText() : null;
    fuzzerContext.getStringFuzz().fuzz(a::hasText, a::getText, a::setText);
    fuzzerContext.addLog(
        new FuzzOperationResult<>(
            "set Text  in Annotation", text, a.hasText() ? a.getText() : null));
  }

  private void fuzzTime(Annotation a) {
    val time = a.hasTime() ? a.getTime() : null;
    if (time == null) {
      val newTime = fuzzerContext.getRandomDate();
      a.setTime(newTime);
      fuzzerContext.addLog(new FuzzOperationResult<>("set Time  in Annotation", null, newTime));
    } else {
      val orgTime = a.getTime();
      val newTime = fuzzerContext.getRandomDate();
      a.setTime(newTime);
      fuzzerContext.addLog(new FuzzOperationResult<>("fuzz Time  in Annotation", orgTime, newTime));
    }
  }

  private void fuzzAuth(Annotation a) {
    val auth = a.hasAuthor() ? a.getAuthor() : null;
    var newAuth = new StringType(fuzzerContext.getStringFuzz().generateRandom(15));
    if (auth == null) {
      a.setAuthor(newAuth);
      fuzzerContext.addLog(new FuzzOperationResult<>("set Author  in Annotation", null, newAuth));
    } else {
      newAuth = new StringType(fuzzerContext.getStringFuzz().fuzz(auth.toString()));
      a.setAuthor(newAuth);
      fuzzerContext.addLog(
          new FuzzOperationResult<>("fuzz Author  in Annotation", auth, newAuth.getValue()));
    }
    a.setAuthor(newAuth);
  }

  @Override
  public Annotation generateRandom() {
    return new Annotation()
        .setText(fuzzerContext.getStringFuzz().generateRandom(15))
        .setAuthor(new StringType(fuzzerContext.getStringFuzz().generateRandom(15)))
        .setTime(fuzzerContext.getRandomDate());
  }

  @Override
  public FuzzerContext getContext() {
    return fuzzerContext;
  }
}
