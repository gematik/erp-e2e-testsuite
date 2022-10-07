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

package de.gematik.test.erezept.fhir.values;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.exceptions.InvalidBaseANR;
import de.gematik.test.erezept.fhir.parser.profiles.ErpCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import de.gematik.test.erezept.fhir.valuesets.IdentifierTypeDe;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.Objects;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;

public abstract class BaseANR {

  @Getter
  public enum ANRType {
    LANR(ErpCodeSystem.HL7_V2_0203, ErpNamingSystem.KBV_NS_BASE_ANR, IdentifierTypeDe.LANR),
    ZANR(
        ErpCodeSystem.IDENTIFIER_TYPE_DE_BASIS,
        ErpNamingSystem.ZAHNARZTNUMMER,
        IdentifierTypeDe.ZANR);

    private final ErpCodeSystem codeSystem;
    private final ErpNamingSystem namingSystem;
    private final IdentifierTypeDe codeType;

    ANRType(ErpCodeSystem codeSystem, ErpNamingSystem namingSystem, IdentifierTypeDe codeType) {
      this.codeSystem = codeSystem;
      this.namingSystem = namingSystem;
      this.codeType = codeType;
    }

    public static ANRType fromCode(@NonNull String code) {
      return ANRType.valueOf(code.toUpperCase());
    }
  }

  @Getter private final ANRType type;
  @Getter private final String value;

  protected BaseANR(ANRType type, String value) {
    this.type = type;
    this.value = value;
  }

  public final boolean checkValue() {
    val rawVal = value.substring(0, 6);
    val actContNo = value.substring(6, 7);
    val expConNo = GemFaker.generateControlNo(rawVal);
    return String.valueOf(expConNo).equals(actContNo);
  }

  public final ErpNamingSystem getNamingSystem() {
    return this.type.getNamingSystem();
  }

  public final String getNamingSystemUrl() {
    return this.getNamingSystem().getCanonicalUrl();
  }

  public Identifier asIdentifier() {
    val id = new Identifier();
    id.getType()
        .addCoding(new Coding().setCode(this.type.name()).setSystem(this.getCodeSystemUrl()));
    id.setSystem(this.getNamingSystemUrl()).setValue(value);
    return id;
  }

  public final ErpCodeSystem getCodeSystem() {
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
    BaseANR anr;
    if (type == ANRType.ZANR) {
      anr = new ZANR(value);
    } else if (type == ANRType.LANR) {
      anr = new LANR(value);
    } else {
      throw new InvalidBaseANR(type);
    }
    return anr;
  }

  public static BaseANR randomFromQualification(QualificationType qualificationType) {
    BaseANR doctorNumber;
    if (qualificationType == QualificationType.DOCTOR) {
      doctorNumber = LANR.random();
    } else if (qualificationType == QualificationType.DENTIST) {
      doctorNumber = ZANR.random();
    } else {
      throw new IllegalArgumentException(
          format("Profession for Doctors of Type {0} not implemented", qualificationType));
    }
    return doctorNumber;
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
