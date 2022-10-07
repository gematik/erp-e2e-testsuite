/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.parser;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.parser.IParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeSystem;
import org.hl7.fhir.r4.model.NamingSystem;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.ValueSet;

@Slf4j
public class CustomProfileSupport implements IValidationSupport {

  public static final String CUSTOM_PROFILES_INDEX = "Profiles.gen";

  private final FhirContext ctx;

  private final HashMap<String, StructureDefinition> structureDefinitions = new HashMap<>();
  private final HashMap<String, NamingSystem> namingSystems = new HashMap<>();
  private final HashMap<String, CodeSystem> codeSystems = new HashMap<>();
  private final HashMap<String, ValueSet> valueSets = new HashMap<>();

  @SneakyThrows
  protected CustomProfileSupport(FhirContext context) {
    this.ctx = context;

    IParser xmlParser = this.ctx.newXmlParser();
    IParser jsonParser = this.ctx.newJsonParser();

    val profilesIndexInputStream =
        Objects.requireNonNull(ClassLoader.getSystemResourceAsStream(CUSTOM_PROFILES_INDEX));
    try (val profileIndex = new BufferedReader(new InputStreamReader(profilesIndexInputStream))) {
      profileIndex
          .lines()
          .filter(line -> !line.startsWith("//")) // filter comment lines
          .filter(line -> line.length() != 0) // filter empty lines
          .map(
              line ->
                  line.replace(
                      "\\", "/")) // change directory separators for being able to load from jar
          .forEach(line -> initProfile(line, xmlParser, jsonParser));
    }
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
    if (resource instanceof StructureDefinition) {
      val structureDefinition = (StructureDefinition) resource;
      structureDefinitions.put(structureDefinition.getUrl(), structureDefinition);
    } else if (resource instanceof NamingSystem) {
      val namingSystem = (NamingSystem) resource;
      String url = namingSystem.getUniqueId().get(0).getValue();
      namingSystems.put(url, namingSystem);
    } else if (resource instanceof CodeSystem) {
      val codeSystem = (CodeSystem) resource;
      codeSystems.put(codeSystem.getUrl(), codeSystem);
    } else if (resource instanceof ValueSet) {
      val valueSet = (ValueSet) resource;
      valueSets.put(valueSet.getUrl(), valueSet);
    }
  }

  @Override
  public FhirContext getFhirContext() {
    return this.ctx;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IBaseResource> List<T> fetchAllStructureDefinitions() {
    return structureDefinitions.values().stream().map(sd -> (T) sd).collect(Collectors.toList());
  }

  @Override
  public IBaseResource fetchCodeSystem(String theSystem) {
    return codeSystems.get(theSystem);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends IBaseResource> T fetchResource(Class<T> theClass, String theUri) {
    // if a URI contains a version (...|1.0.1) just simply cut off the version and use the plain URI
    val fixedUri = theUri.split("\\|")[0];
    return (T) structureDefinitions.get(fixedUri);
  }

  @Override
  public IBaseResource fetchStructureDefinition(String theUrl) {
    return structureDefinitions.get(theUrl);
  }

  @Override
  public IBaseResource fetchValueSet(String theValueSetUrl) {
    return valueSets.get(theValueSetUrl);
  }
}
