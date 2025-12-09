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

package de.gematik.test.erezept.fhir.r4.eu;

import static de.gematik.test.erezept.fhir.valuesets.eu.EuPartNaming.COUNTRY_CODE;
import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowCodeSystem;
import de.gematik.test.erezept.fhir.profiles.systems.KbvNamingSystem;
import de.gematik.test.erezept.fhir.r4.AbstractOrganization;
import de.gematik.test.erezept.fhir.r4.ErpFhirResource;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.values.KZVA;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.*;

@SuppressWarnings({"java:S110"})
public class EuOrganization extends AbstractOrganization implements ErpFhirResource {

  public Optional<IsoCountryCode> getEuCountry() {
    return Optional.ofNullable(
        this.getExtension().stream()
            .filter(GemErpEuStructDef.NCPEH_COUNTRY_EXT::matches)
            .map(code -> IsoCountryCode.fromCode(code.castToCoding(code.getValue()).getCode()))
            .findFirst()
            .orElseThrow(() -> new MissingFieldException(this.getClass(), COUNTRY_CODE.getCode())));
  }

  public Optional<TelematikID> getTelematikID() {
    return this.getIdentifier().stream()
        .filter(TelematikID::matches)
        .map(TelematikID::from)
        .findFirst();
  }

  public Optional<BSNR> getBsnr() {
    return this.getIdentifier().stream()
        .filter(KbvNamingSystem.BASE_BSNR::matches)
        .findFirst()
        .map(i -> BSNR.from(i.getValue()));
  }

  public Optional<KZVA> getKzva() {
    return this.getIdentifier().stream()
        .filter(DeBasisProfilNamingSystem.KZBV_KZVA_ABRECHNUNGSNUMMER_SID::matches)
        .findFirst()
        .map(i -> KZVA.from(i.getValue()));
  }

  public Optional<IKNR> getIknr() {
    return this.getIdentifier().stream()
        .filter(
            i ->
                WithSystem.anyOf(DeBasisProfilNamingSystem.IKNR, DeBasisProfilNamingSystem.IKNR_SID)
                    .matches(i))
        .map(IKNR::from)
        .findFirst();
  }

  private static WithSystem[] identifierList = {
    DeBasisProfilNamingSystem.IKNR,
    DeBasisProfilNamingSystem.IKNR_SID,
    DeBasisProfilNamingSystem.TELEMATIK_ID_SID,
    KbvNamingSystem.BASE_BSNR,
    DeBasisProfilNamingSystem.KZBV_KZVA_ABRECHNUNGSNUMMER_SID
  };

  public List<Identifier> getUnknownIdentifier() {
    return this.getIdentifier().stream()
        .filter(it -> !WithSystem.anyOf(identifierList).matches(it))
        .toList();
  }

  public Optional<EuHealthcareFacilityType> getProviderType() {
    return this.getType().stream()
        .flatMap(cc -> cc.getCoding().stream())
        .filter(ErpWorkflowCodeSystem.PROFESSION_OID::matches)
        .map(coding -> new EuHealthcareFacilityType(coding.getCode(), coding.getDisplay()))
        .findFirst();
  }

  public Optional<String> getProfession() {
    return this.getType().stream()
        .flatMap(cc -> cc.getCoding().stream())
        .filter(it -> !it.hasSystem())
        .map(Coding::getCode)
        .findFirst();
  }

  public Optional<String> getNameOptional() {
    return Optional.of(this.getName());
  }

  public Optional<Address> getEuAddress() {
    return this.getAddress().stream().findFirst();
  }

  public static EuOrganization fromOrganisation(Organization adaptee) {
    if (adaptee instanceof EuOrganization euOrganization) {
      return euOrganization;
    } else {
      val euMedication = new EuOrganization();
      adaptee.copyValues(euMedication);
      return euMedication;
    }
  }

  public static EuOrganization fromOrganisation(Resource adaptee) {
    return fromOrganisation((Organization) adaptee);
  }

  @Override
  public String getDescription() {
    return format(
        "{0} ({1}) aus {2} {3}",
        this.getNameOptional(),
        getNameOptional().isPresent() ? getNameOptional() : "noNmae",
        this.getPostalCode(),
        this.getCity());
  }
}
