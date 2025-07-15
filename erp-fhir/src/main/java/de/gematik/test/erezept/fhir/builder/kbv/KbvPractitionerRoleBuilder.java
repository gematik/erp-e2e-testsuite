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

package de.gematik.test.erezept.fhir.builder.kbv;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.ErpFhirResource;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitionerRole;
import de.gematik.test.erezept.fhir.values.AsvTeamNumber;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;

public class KbvPractitionerRoleBuilder
    extends ResourceBuilder<KbvPractitionerRole, KbvPractitionerRoleBuilder> {

  private KbvItaForVersion kbvItaForVersion = KbvItaForVersion.getDefaultVersion();
  private Reference practitioner;
  private Identifier asvTeamNummer;

  public static KbvPractitionerRoleBuilder builder() {
    return new KbvPractitionerRoleBuilder();
  }

  public KbvPractitionerRoleBuilder version(KbvItaForVersion version) {
    this.kbvItaForVersion = version;
    return this;
  }

  public KbvPractitionerRoleBuilder practitioner(KbvPractitioner practitioner) {
    this.practitioner =
        ErpFhirResource.createReference(ResourceType.Practitioner, practitioner.getId());
    return this;
  }

  public KbvPractitionerRoleBuilder teamNumber(AsvTeamNumber asvTeamNumber) {
    this.asvTeamNummer = asvTeamNumber.asIdentifier();
    return this;
  }

  @Override
  public KbvPractitionerRole build() {
    val practitionerRole =
        this.createResource(
            KbvPractitionerRole::new, KbvItaForStructDef.PRACTITIONER_ROLE, kbvItaForVersion);
    practitionerRole.setPractitioner(practitioner);
    practitionerRole.getOrganization().setIdentifier(asvTeamNummer);
    return practitionerRole;
  }
}
