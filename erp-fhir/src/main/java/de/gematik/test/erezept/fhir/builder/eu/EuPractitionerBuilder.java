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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.fhir.builder.eu;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.EuPractitioner;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.StringType;

public class EuPractitionerBuilder extends ResourceBuilder<EuPractitioner, EuPractitionerBuilder> {

  private EuVersion version = EuVersion.getDefaultVersion();
  private HumanName name;
  private Identifier identifier;

  public static EuPractitionerBuilder buildPractitioner(HumanName name) {
    val builder = new EuPractitionerBuilder();
    builder.name = name;
    return builder;
  }

  public EuPractitionerBuilder version(EuVersion version) {
    this.version = version;
    return this;
  }

  public static EuPractitionerBuilder buildPractitioner() {
    val famName = GemFaker.getFaker().name().lastName();
    val givenName = GemFaker.getFaker().name().firstName();
    val hName =
        new HumanName()
            .setFamily(famName)
            .setText(format("{0} {1}", givenName, famName))
            .setGiven(List.of(new StringType(givenName)));

    return buildPractitioner(hName);
  }

  public static EuPractitioner buildSimplePractitioner() {
    val famName = GemFaker.getFaker().name().lastName();
    val givenName = GemFaker.getFaker().name().firstName();
    val hname =
        new HumanName()
            .setFamily(famName)
            .setText(format("{0} {1}", givenName, famName))
            .setGiven(List.of(new StringType(givenName)));
    val builder = buildPractitioner(hname);
    builder.identifier(
        new Identifier().setValue("unknownDoctor Type").setSystem("https://www.unknownSystem.eu"));
    return builder.build();
  }

  public EuPractitionerBuilder identifier(Identifier identifier) {
    this.identifier = identifier;
    return this;
  }

  @Override
  public EuPractitioner build() {
    checkRequired();
    val practitioner =
        this.createResource(EuPractitioner::new, GemErpEuStructDef.EU_PRACTITIONER, version);
    Optional.ofNullable(name).ifPresent(n -> practitioner.setName(List.of(n)));
    Optional.ofNullable(identifier).ifPresent(id -> practitioner.getIdentifier().add(id));
    return practitioner;
  }

  private void checkRequired() {
    this.checkRequired(
        identifier, "EuPractitioner has to have an identifier with description based on a System");
  }
}
