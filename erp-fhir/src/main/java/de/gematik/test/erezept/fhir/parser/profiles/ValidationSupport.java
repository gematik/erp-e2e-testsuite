/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.parser.profiles;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.context.support.ValidationSupportContext;
import ca.uhn.fhir.parser.IParser;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.profiles.version.VersionedProfile;
import java.util.*;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

@Slf4j
public class ValidationSupport implements IValidationSupport {

  private final FhirContext ctx;

  @Getter private final VersionedProfile<?> profile;

  private final Map<String, StructureDefinition> structureDefinitions;
  private final Map<String, NamingSystem> namingSystems;
  private final Map<String, CodeSystem> codeSystems;
  private final Map<String, ValueSet> valueSets;

  @SneakyThrows
  protected ValidationSupport(
      VersionedProfile<?> profile, List<String> profileFiles, FhirContext context) {

    log.trace(format("Instantiate ValidationSupport for {0}", profile));
    this.profile = profile;
    this.ctx = context;

    this.structureDefinitions = new HashMap<>();
    this.namingSystems = new HashMap<>();
    this.codeSystems = new HashMap<>();
    this.valueSets = new HashMap<>();

    IParser xmlParser = this.ctx.newXmlParser();
    IParser jsonParser = this.ctx.newJsonParser();

    profileFiles.stream()
        .map(
            line ->
                line.replace(
                    "\\", "/")) // change directory separators for being able to load from jar
        .forEach(line -> initProfile(line, xmlParser, jsonParser));
  }

  @SneakyThrows
  private void initProfile(
      final String profilePath, final IParser xmlParser, final IParser jsonParser) {
    log.trace(format("Load Profile File {0}", profilePath));
    val inputStream = Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(profilePath));

    val encodingType = EncodingType.fromString(profilePath);
    val fittingParser = encodingType.chooseAppropriateParser(xmlParser, jsonParser);
    val resource = fittingParser.parseResource(inputStream);

    this.addResource(resource);
  }

  private <T extends IBaseResource> void addResource(T resource) {
    if (resource instanceof StructureDefinition structureDefinition) {
      val key = structureDefinition.getUrl();
      log.trace(format("Put StructureDefinition {0} to profile {1}", key, this.profile));
      this.structureDefinitions.put(key, structureDefinition);
    } else if (resource instanceof NamingSystem namingSystem) {
      val key = namingSystem.getUniqueId().get(0).getValue();
      log.trace(format("Put NamingSystem {0} to profile {1}", key, this.profile));
      this.namingSystems.put(key, namingSystem);
    } else if (resource instanceof CodeSystem codeSystem) {
      val key = codeSystem.getUrl();
      log.trace(format("Put CodeSystem {0} to profile {1}", key, this.profile));
      this.codeSystems.put(key, codeSystem);
    } else if (resource instanceof ValueSet valueSet) {
      val key = valueSet.getUrl();
      log.trace(format("Put ValueSet {0} to profile {1}", key, this.profile));
      this.valueSets.put(key, valueSet);
    }
  }

  @Override
  public List<IBaseResource> fetchAllConformanceResources() {
    ArrayList<IBaseResource> retVal = new ArrayList<>();
    retVal.addAll(codeSystems.values());
    retVal.addAll(structureDefinitions.values());
    retVal.addAll(valueSets.values());
    retVal.addAll(namingSystems.values());
    return retVal;
  }

  @Override
  public boolean isCodeSystemSupported(
      ValidationSupportContext theValidationSupportContext, String theSystem) {
    return this.codeSystems.containsKey(theSystem);
  }

  @Override
  public boolean isValueSetSupported(
      ValidationSupportContext theValidationSupportContext, String theValueSetUrl) {
    return this.valueSets.containsKey(theValueSetUrl);
  }

  @Override
  public FhirContext getFhirContext() {
    return this.ctx;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IBaseResource> List<T> fetchAllStructureDefinitions() {
    val retVal = new ArrayList<>(this.structureDefinitions.values());
    return (List<T>) Collections.unmodifiableList(retVal);
  }

  @Override
  public IBaseResource fetchCodeSystem(String theSystem) {
    if (!this.profile.getProfile().matchesClaim(theSystem)) return null;
    return fetchBaseResource(theSystem, this.codeSystems);
  }

  @Override
  public IBaseResource fetchStructureDefinition(String theUrl) {
    if (!this.profile.getProfile().matchesClaim(theUrl)) return null;
    return fetchBaseResource(theUrl, this.structureDefinitions);
  }

  @Override
  public IBaseResource fetchValueSet(String theValueSetUrl) {
    if (!this.profile.getProfile().matchesClaim(theValueSetUrl)) return null;
    return fetchBaseResource(theValueSetUrl, this.valueSets);
  }

  private <R extends IBaseResource> IBaseResource fetchBaseResource(
      String resourceUrl, Map<String, R> map) {
    val tokens = resourceUrl.split("\\|");
    val url = tokens[0];
    val version = tokens.length > 1 ? tokens[1] : null;

    R resource = null;

    boolean exactVersionMatch = false;
    if (version == null) {
      // no version given, try to find simply by URL
      // this results in a greedy fetch between competing profile versions
      resource = map.get(url);
    } else if (profile.getProfileVersion().isEqual(version)) {
      // profile version does match exactly
      resource = map.get(url);
      exactVersionMatch = true;
    }

    val matched = resource != null;
    if (matched) {
      val em = exactVersionMatch ? "with exact version" : "without version";
      log.debug(format("Matched {0} in profile {1} {2}", resourceUrl, this.profile, em));
    } else {
      log.debug(format("Could not find Resource {0} in {1}", resourceUrl, this.profile));
    }

    return resource;
  }
}
