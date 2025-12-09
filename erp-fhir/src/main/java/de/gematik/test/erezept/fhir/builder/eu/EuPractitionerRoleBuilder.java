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

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.EuOrganization;
import de.gematik.test.erezept.fhir.r4.eu.EuOrganizationProfession;
import de.gematik.test.erezept.fhir.r4.eu.EuPractitioner;
import de.gematik.test.erezept.fhir.r4.eu.EuPractitionerRole;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Reference;

public class EuPractitionerRoleBuilder
    extends ResourceBuilder<EuPractitionerRole, EuPractitionerRoleBuilder> {

  private EuVersion version = EuVersion.getDefaultVersion();
  private EuPractitioner euPractitioner;
  private EuOrganization euOrganization;
  private EuOrganizationProfession euOrganizationProfession;

  public static EuPractitionerRole getSimplePractitionerRole() {
    return EuPractitionerRoleBuilder.builder()
        .euPractitioner(EuPractitionerBuilder.buildSimplePractitioner())
        .euOrganization(EuOrganizationFaker.faker().fake())
        .defaultProfessionOID()
        .build();
  }

  public static EuPractitionerRoleBuilder builder() {
    return new EuPractitionerRoleBuilder();
  }

  public EuPractitionerRoleBuilder version(EuVersion version) {
    this.version = version;
    return this;
  }

  public EuPractitionerRoleBuilder euPractitioner(EuPractitioner euPractitioner) {
    this.euPractitioner = euPractitioner;
    return this;
  }

  public EuPractitionerRoleBuilder euOrganization(EuOrganization euOrganization) {
    this.euOrganization = euOrganization;
    return this;
  }

  public EuPractitionerRoleBuilder euOrganizationProfessionOID(
      EuOrganizationProfession euOrganizationProfession) {
    this.euOrganizationProfession = euOrganizationProfession;
    return this;
  }

  public EuPractitionerRoleBuilder defaultProfessionOID() {
    this.euOrganizationProfession = EuOrganizationProfession.getDefaultPharmacist();
    return this;
  }

  @Override
  public EuPractitionerRole build() {

    val euPractRole =
        this.createResource(
            EuPractitionerRole::new, GemErpEuStructDef.EU_PRACTITIONER_ROLE, version);
    Optional.ofNullable(euPractitioner)
        .ifPresent(
            pr ->
                euPractRole.setPractitioner(
                    new Reference().setReference("Practitioner/" + euPractitioner.getId())));
    Optional.ofNullable(euOrganization)
        .ifPresent(
            pr ->
                euPractRole.setOrganization(
                    new Reference().setReference("Organisation/" + euOrganization.getId())));
    Optional.ofNullable(euOrganizationProfession)
        .ifPresent(oID -> euPractRole.addCode(new CodeableConcept(oID.asCoding())));
    return euPractRole;
  }
}
