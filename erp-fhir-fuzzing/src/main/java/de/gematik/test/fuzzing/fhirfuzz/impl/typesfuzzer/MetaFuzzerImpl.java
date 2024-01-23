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
import de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes.IdFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.stringtypes.UrlFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzOperationResult;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.val;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.InstantType;
import org.hl7.fhir.r4.model.Meta;

public class MetaFuzzerImpl implements FhirTypeFuzz<Meta> {

  private final FuzzerContext fuzzerContext;

  public MetaFuzzerImpl(FuzzerContext fuzzerContext) {
    this.fuzzerContext = fuzzerContext;
  }

  @Override
  public Meta fuzz(Meta meta) {
    val m = fuzzerContext.getRandomPart(getMutators());
    for (val f : m) {
      f.accept(meta);
    }
    return meta;
  }

  @Override
  public FuzzerContext getContext() {
    return fuzzerContext;
  }

  private List<FuzzingMutator<Meta>> getMutators() {
    val manipulators = new LinkedList<FuzzingMutator<Meta>>();
    manipulators.add(this::fuzzSource);
    manipulators.add(this::fuzzLastUpdate);
    manipulators.add(this::fuzzMeta);
    manipulators.add(this::fuzzProfile);
    manipulators.add(this::fuzzExtension);
    manipulators.add(this::fuzzSecurity);
    manipulators.add(this::fuzzTag);
    manipulators.add(this::fuzzURL);
    return manipulators;
  }

  private void fuzzMeta(Meta m) {
    if (m.hasVersionId()) {
      val id = fuzzerContext.getIdFuzzer().generateRandom();
      m.setVersionId(id);
      fuzzerContext.addLog(new FuzzOperationResult<>("set VersionId in Meta:", null, id));
    } else {
      val org = m.getVersionId();
      fuzzerContext.getIdFuzzer().fuzz(m::getVersionId, m::setVersionId);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "set VersionId in Meta:", org, m.hasVersionId() ? m.getVersionId() : null));
    }
  }

  private void fuzzURL(Meta m) {
    UrlFuzzImpl urlFuzz = fuzzerContext.getUrlFuzz();
    if (!m.hasSource()) {
      val newSource = urlFuzz.generateRandom();
      m.setSource(newSource);
      fuzzerContext.addLog(new FuzzOperationResult<>("set Source in Meta:", null, newSource));
    } else {
      val source = m.getSource();
      urlFuzz.fuzz(m::getSource, m::setSource);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "set Source in Meta:", source, m.hasSource() ? m.getSource() : null));
    }
  }

  private void fuzzLastUpdate(Meta m) {
    val infoString = "set LastUpdate in Meta:";
    if (!m.hasLastUpdated()) {
      val date = fuzzerContext.getRandomDate();
      m.setLastUpdated(date);
      fuzzerContext.addLog(new FuzzOperationResult<>(infoString, null, date));
    } else if (Boolean.TRUE.equals(fuzzerContext.shouldFuzz(m.getLastUpdated()))) {
      val orgDate = m.getLastUpdated();
      m.setLastUpdated(null);
      fuzzerContext.addLog(new FuzzOperationResult<>(infoString, orgDate, null));
    } else {
      val orgDate = m.getLastUpdated();
      val newDate = new DateTimeType(fuzzerContext.getRandomDate());
      m.setLastUpdatedElement(new InstantType(newDate));
      fuzzerContext.addLog(new FuzzOperationResult<>("fuzz LastUpdate in Meta:", orgDate, newDate));
    }
  }

  private void fuzzSource(Meta m) {
    if (!m.hasSource()) {
      val orgVersionId = m.getVersionId();
      val orgSource = m.getSource();
      m.setSource(orgVersionId);
      m.setVersionId(orgSource);
      if (!m.hasSource()) m.setSource(fuzzerContext.getUrlFuzz().generateRandom());
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "switched Source and VersionId in Meta // original VersionId:",
              orgVersionId,
              orgVersionId));
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "switched Source and VersionId in Meta // original Source", orgSource, orgSource));

      if (!m.hasVersionId()) {
        val versionId = fuzzerContext.getUrlFuzz().generateRandom();
        m.setVersionId(versionId);
        fuzzerContext.addLog(
            new FuzzOperationResult<>(
                "add VersionId after switched Source and VersionId without SourceEntry in Meta",
                null,
                versionId));
      }
    }
  }

  private void fuzzProfile(Meta m) {
    val canonicalTypeFuzzer =
        fuzzerContext.getTypeFuzzerFor(
            CanonicalType.class, () -> new CanonicalTypeFuzzerImpl(fuzzerContext));
    if (!m.hasProfile()) {
      val canonicalType = canonicalTypeFuzzer.generateRandom();
      List<CanonicalType> profiles = new LinkedList<>();
      profiles.add(canonicalType);
      m.setProfile(profiles);
      val res = profiles.stream().map(Object::toString).collect(Collectors.joining("\n"));
      fuzzerContext.addLog(new FuzzOperationResult<>("set Profile in Meta:", null, res));
    } else {
      val prof = m.getProfile().get(0).copy();
      canonicalTypeFuzzer.fuzz(m.getProfile().get(0));
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "fuzzed Profile  in Meta", prof, m.hasProfile() ? m.getProfile().get(0) : null));
      if (!m.hasProfile()) {
        m.setProfile(List.of(canonicalTypeFuzzer.generateRandom()));
        fuzzerContext.addLog(
            new FuzzOperationResult<>(
                "set Profile  in Meta for KBV caused be cardinality 1..1",
                prof,
                m.hasProfile() ? m.getProfile().get(0) : null));
      }
    }
  }

  private void fuzzSecurity(Meta m) {
    val codingTypeFuzzerImpl =
        fuzzerContext.getTypeFuzzerFor(Coding.class, () -> new CodingTypeFuzzerImpl(fuzzerContext));
    if (m.getSecurity() == null) {
      val coding = codingTypeFuzzerImpl.generateRandom();
      m.setSecurity(List.of(coding));
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "set Security in Meta",
              null,
              coding.getSystem()
                  + " "
                  + coding.getVersion()
                  + " "
                  + coding.getCode()
                  + " "
                  + coding.getDisplay()
                  + " "
                  + coding.getUserSelected()));
    } else {
      val listFuzz = new ListFuzzerImpl<>(fuzzerContext, codingTypeFuzzerImpl);
      val sec = m.getSecurity();
      listFuzz.fuzz(m::getSecurity, m::setSecurity);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "fuzz Security in Meta ", sec, m.hasSecurity() ? m.getSecurity().get(0) : null));
    }
  }

  private void fuzzTag(Meta m) {
    val codingTypeFuzzerImpl = fuzzerContext.getTypeFuzzerFor(Coding.class);
    if (m.getTag() == null) {
      codingTypeFuzzerImpl.ifPresent(tf -> m.setTag(List.of(tf.generateRandom())));
      val coding = m.getTag().get(0);
      fuzzerContext.addLog(
          new FuzzOperationResult<>(
              "set Tag in Meta:",
              null,
              coding.getSystem()
                  + " "
                  + coding.getVersion()
                  + " "
                  + coding.getCode()
                  + " "
                  + coding.getDisplay()
                  + " "
                  + coding.getUserSelected()));
    } else {
      val sec = m.getTag();
      for (Coding c : sec) {
        val org = c.copy();
        codingTypeFuzzerImpl.ifPresent(tf -> tf.fuzz(c));
        fuzzerContext.addLog(new FuzzOperationResult<>("set tag in Meta:", org.getId(), c.getId()));
      }
    }
  }

  private void fuzzExtension(Meta m) {
    val extensionFuzzer =
        fuzzerContext.getTypeFuzzerFor(
            Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext));
    if (!m.hasExtension()) {
      val ext = extensionFuzzer.generateRandom();
      m.setExtension(List.of(ext));
      fuzzerContext.addLog(new FuzzOperationResult<>("Extension in Meta", null, ext));
    } else {
      val listFuzzer = new ListFuzzerImpl<>(fuzzerContext, extensionFuzzer);
      listFuzzer.fuzz(m::getExtension, m::setExtension);
    }
  }

  public Meta generateRandom() {
    Meta meta = new Meta();
    val idfuzzer = new IdFuzzerImpl(fuzzerContext);
    val urlFuzzer = new UrlFuzzImpl(fuzzerContext);
    meta.setVersionId(idfuzzer.generateRandom());
    meta.setLastUpdated(fuzzerContext.getRandomDate());
    meta.setSource(urlFuzzer.generateRandom());
    meta.setId(idfuzzer.generateRandom());
    CanonicalType canonicalType = new CanonicalType(urlFuzzer.generateRandom());
    List<CanonicalType> profiles = List.of(canonicalType);
    meta.setProfile(profiles);
    return meta;
  }
}
