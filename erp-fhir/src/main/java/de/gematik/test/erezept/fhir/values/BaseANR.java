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

package de.gematik.test.erezept.fhir.values;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.WithChecksum;
import de.gematik.bbriccs.fhir.coding.WithCodeSystem;
import de.gematik.bbriccs.fhir.coding.WithNamingSystem;
import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.bbriccs.fhir.de.HL7CodeSystem;
import de.gematik.bbriccs.fhir.de.valueset.IdentifierTypeDe;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.exceptions.InvalidBaseANR;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvNamingSystem;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;

@Getter
public abstract class BaseANR implements WithChecksum {

  @Getter
  public enum ANRType {
    LANR(HL7CodeSystem.HL7_V2_0203, KbvNamingSystem.BASE_ANR, IdentifierTypeDe.LANR),
    ZANR(
        DeBasisProfilCodeSystem.IDENTIFIER_TYPE_DE_BASIS,
        DeBasisProfilNamingSystem.KZBV_ZAHNARZTNUMMER,
        IdentifierTypeDe.ZANR);

    private final WithCodeSystem codeSystem;
    private final WithNamingSystem namingSystem;
    private final IdentifierTypeDe codeType;

    ANRType(WithCodeSystem codeSystem, WithNamingSystem namingSystem, IdentifierTypeDe codeType) {
      this.codeSystem = codeSystem;
      this.namingSystem = namingSystem;
      this.codeType = codeType;
    }

    public static ANRType fromCode(String code) {
      return ANRType.valueOf(code.toUpperCase());
    }

    public static List<WithCodeSystem> validCodeSystems() {
      return Arrays.stream(ANRType.values()).map(ANRType::getCodeSystem).toList();
    }

    public static List<WithNamingSystem> validNamingSystems() {
      return Arrays.stream(ANRType.values()).map(ANRType::getNamingSystem).toList();
    }

    public static List<IdentifierTypeDe> validCodeTypes() {
      return Arrays.stream(ANRType.values()).map(ANRType::getCodeType).toList();
    }
  }

  private final ANRType type;
  private final String value;

  protected BaseANR(ANRType type, String value) {
    this.type = type;
    this.value = value;
  }

  @Override
  public boolean isValid() {
    val rawVal = value.substring(0, 6);
    val actContNo = value.substring(6, 7);
    val expConNo = GemFaker.generateControlNo(rawVal);
    return String.valueOf(expConNo).equals(actContNo);
  }

  @Override
  public int getChecksum() {
    return Integer.parseInt(value.substring(6, 7));
  }

  public final WithNamingSystem getNamingSystem() {
    return this.type.getNamingSystem();
  }

  public final String getNamingSystemUrl() {
    return this.getNamingSystem().getCanonicalUrl();
  }

  public Identifier asIdentifier() {
    return asIdentifier(this.getNamingSystem());
  }

  public Identifier asIdentifier(WithNamingSystem namingSystem) {
    val id = new Identifier();
    id.getType()
        .addCoding(new Coding().setCode(this.type.name()).setSystem(this.getCodeSystemUrl()));
    id.setSystem(namingSystem.getCanonicalUrl()).setValue(value);
    return id;
  }

  public final WithCodeSystem getCodeSystem() {
    return this.type.getCodeSystem();
  }

  public final String getCodeSystemUrl() {
    return this.getCodeSystem().getCanonicalUrl();
  }

  @Override
  public int hashCode() {
    return Objects.hash(getValue());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final BaseANR other = (BaseANR) o;
    return getValue().equals(other.getValue()) && getType().equals(other.getType());
  }

  @Override
  public String toString() {
    return format("{0}: {1}", this.type.name(), this.value);
  }

  public static BaseANR fromIdentifier(Identifier identifier) {
    val type = decideType(identifier);
    return fromTypedValue(type, identifier.getValue());
  }

  public static BaseANR fromTypedValue(ANRType type, String value) {
    return switch (type) {
      case ZANR -> new ZANR(value);
      case LANR -> new LANR(value);
    };
  }

  public static BaseANR forQualification(QualificationType qualificationType, String value) {
    BaseANR doctorNumber;
    if (qualificationType == QualificationType.DOCTOR) {
      doctorNumber = new LANR(value);
    } else if (qualificationType == QualificationType.DENTIST) {
      doctorNumber = new ZANR(value);
    } else {
      throw new IllegalArgumentException(
          format("Profession for Doctors of Type {0} not implemented", qualificationType));
    }
    return doctorNumber;
  }

  public static BaseANR randomFromQualification(QualificationType qualificationType) {
    return switch (qualificationType) {
      case DOCTOR, DOCTOR_AS_REPLACEMENT, DOCTOR_IN_TRAINING -> LANR.random();
      case DENTIST -> ZANR.random();
      case MIDWIFE -> throw new IllegalArgumentException(
          format("Profession midwife do not have an ANR"));
    };
  }

  public static boolean hasValidIdentifier(Identifier identifier) {
    val validNamingSystems =
        List.of(
            KbvNamingSystem.BASE_ANR.getCanonicalUrl(),
            KbvNamingSystem.ZAHNARZTNUMMER.getCanonicalUrl(),
            DeBasisProfilNamingSystem.KZBV_ZAHNARZTNUMMER.getCanonicalUrl());
    return validNamingSystems.contains(identifier.getSystem());
  }

  public static boolean isPractitioner(Identifier identifier) {
    return isPractitioner(identifier.getType().getCodingFirstRep());
  }

  public static boolean isPractitioner(Coding coding) {
    val practitionerCodeSystems =
        ANRType.validCodeSystems().stream().map(WithSystem::getCanonicalUrl).toList();

    if (!practitionerCodeSystems.contains(coding.getSystem())) {
      return false; // no CodeSystem of a ANRType found
    }

    return ANRType.validCodeTypes().stream()
        .anyMatch(anrCodeType -> anrCodeType.getCode().equals(coding.getCode()));
  }

  private static ANRType decideType(Identifier identifier) {
    ANRType type;
    val coding = identifier.getType().getCodingFirstRep();
    if (coding.getSystem().equals(ANRType.ZANR.getCodeSystem().getCanonicalUrl())
        && coding.getCode().equals(ANRType.ZANR.getCodeType().getCode())) {
      type = ANRType.ZANR;
    } else if (coding.getSystem().equals(ANRType.LANR.getCodeSystem().getCanonicalUrl())
        && coding.getCode().equals(ANRType.LANR.getCodeType().getCode())) {
      type = ANRType.LANR;
    } else {
      throw new InvalidBaseANR(identifier);
    }
    return type;
  }
}
