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

package de.gematik.test.erezept.primsys.rest.data;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static de.gematik.test.erezept.primsys.utils.Strings.getOrDefault;
import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.fhir.valuesets.PersonGroup;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import de.gematik.test.erezept.primsys.rest.data.util.NullableEnumMapper;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.val;

@Data
@XmlRootElement
public class CoverageData {

  private String iknr;
  private String insuranceName;
  private String wop;
  private String insuranceState;
  private String insuranceKind;
  private String payorType;
  private String personGroup;

  public @Nullable String getIknr() {
    return iknr;
  }

  public @Nullable String getInsuranceName() {
    return insuranceName;
  }

  @XmlTransient
  public @Nullable Wop getEnumWop() {
    try {
      return NullableEnumMapper.mapNullable(wop, Wop::fromCode);
    } catch (InvalidValueSetException ivse) {
      throw new WebApplicationException(
          Response.status(400)
              .entity(new ErrorResponse(format("{0} is not a valid WOP Code", wop)))
              .build());
    }
  }

  @XmlTransient
  public @Nullable PersonGroup getEnumPersonGroup() {
    try {
      return NullableEnumMapper.mapNullable(personGroup, PersonGroup::fromCode);
    } catch (InvalidValueSetException ivse) {
      throw new WebApplicationException(
          Response.status(400)
              .entity(
                  new ErrorResponse(format("{0} is not a valid Code for PersonGroup", personGroup)))
              .build());
    }
  }

  @XmlTransient
  public @Nullable VersichertenStatus getEnumInsuranceState() {
    try {
      return NullableEnumMapper.mapNullable(insuranceState, VersichertenStatus::fromCode);
    } catch (InvalidValueSetException ivse) {
      throw new WebApplicationException(
          Response.status(400)
              .entity(
                  new ErrorResponse(
                      format("{0} is not a valid Code InsuranceState", insuranceState)))
              .build());
    }
  }

  @XmlTransient
  public @Nullable VersicherungsArtDeBasis getEnumInsuranceKind() {
    try {
      return NullableEnumMapper.mapNullable(insuranceKind, VersicherungsArtDeBasis::fromCode);
    } catch (InvalidValueSetException ivse) {
      throw new WebApplicationException(
          Response.status(400)
              .entity(
                  new ErrorResponse(
                      format("{0} is not a valid Code for InsuranceKind", insuranceKind)))
              .build());
    }
  }

  public static CoverageData create() {
    val c = new CoverageData();
    c.iknr = fakerIknr();
    var tmpWop = fakerValueSet(Wop.class);
    while (tmpWop.equals(Wop.DUMMY)) {
      tmpWop = fakerValueSet(Wop.class); // prevent DUMMY Wop
    }
    c.wop = tmpWop.getCode();
    c.insuranceName = insuranceName(tmpWop);
    c.insuranceState = fakerValueSet(VersichertenStatus.class).getCode();
    c.insuranceKind = VersicherungsArtDeBasis.GKV.getCode();
    c.personGroup = fakerValueSet(PersonGroup.class).getCode();
    return c;
  }

  public CoverageData fakeMissing() {
    this.iknr = getOrDefault(this.iknr, fakerIknr());
    var tmpWop = fakerValueSet(Wop.class);
    this.wop = getOrDefault(this.wop, tmpWop.getCode());
    this.insuranceName = getOrDefault(this.insuranceName, insuranceName(tmpWop));
    this.insuranceState =
        getOrDefault(this.insuranceState, fakerValueSet(VersichertenStatus.class).getCode());
    this.insuranceKind = getOrDefault(this.insuranceKind, VersicherungsArtDeBasis.GKV.getCode());
    this.payorType = getOrDefault(this.payorType, fakerValueSet(PayorType.class).getCode());
    this.personGroup = getOrDefault(this.personGroup, fakerValueSet(PersonGroup.class).getCode());
    return this;
  }

  public static CoverageData fromKbvBundle(KbvErpBundle bundle) {
    val c = new CoverageData();
    c.iknr = bundle.getCoverageIknr();
    bundle.getCoverageWop().ifPresent(wop -> c.wop = wop.getCode());
    c.insuranceName = bundle.getCoverageName();
    c.insuranceState = bundle.getCoverageState().getCode();
    c.insuranceKind = mapInsuranceKind(bundle);
    c.payorType = mapPayorType(bundle);
    c.personGroup = bundle.getCoveragePersonGroup().getCode();
    return c;
  }

  private static @Nullable String mapInsuranceKind(KbvErpBundle bundle) {
    String ret = null;
    if (bundle.hasCoverageKind()) {
      ret = bundle.getCoverageKind().getCode();
    }
    return ret;
  }

  private static @Nullable String mapPayorType(KbvErpBundle bundle) {
    String ret = null;
    if (bundle.hasPayorType()) {
      ret = bundle.getPayorType().getCode();
    }
    return ret;
  }
}
