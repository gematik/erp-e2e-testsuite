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
import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.exceptions.InvalidValueSetException;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.MedicationType;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.primsys.rest.data.util.NullableEnumMapper;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Ratio;

@Slf4j
@Data
@XmlRootElement
public class MedicationData {

  private String category;
  private String type;
  private String standardSize;
  private String supplyForm;
  private int amount;
  private String ingredientStrength;
  private int packageQuantity;
  private String pzn;
  private String freeText;
  private String name;
  private String ingredient;
  private String dosage;
  private String note;
  private String lotNumber;
  private Date expirationDate;
  private boolean substitutionAllowed;

  @XmlTransient
  public @Nullable MedicationCategory getEnumCategory() {
    try {
      return NullableEnumMapper.mapNullable(category, MedicationCategory::fromCode);
    } catch (InvalidValueSetException ivse) {
      throw new WebApplicationException(
          Response.status(400)
              .entity(
                  new ErrorResponse(
                      format("{0} is not a valid Code for MedicationCategory", category)))
              .build());
    }
  }

  @XmlTransient
  public @Nullable MedicationType getEnumType() {
    try {
      return NullableEnumMapper.mapNullable(type, MedicationType::fromCode);
    } catch (InvalidValueSetException ivse) {
      throw new WebApplicationException(
          Response.status(400)
              .entity(
                  new ErrorResponse(
                      format("{0} is not a valid Code for MedicationCategory", category)))
              .build());
    }
  }

  @XmlTransient
  public @Nullable StandardSize getEnumStandardSize() {
    try {
      return NullableEnumMapper.mapNullable(standardSize, StandardSize::fromCode);
    } catch (InvalidValueSetException ivse) {
      throw new WebApplicationException(
          Response.status(400)
              .entity(
                  new ErrorResponse(
                      format("{0} is not a valid Code for StandardSize", standardSize)))
              .build());
    }
  }

  @XmlTransient
  public @Nullable Darreichungsform getEnumDarreichungsForm() {
    try {
      return NullableEnumMapper.mapNullable(supplyForm, Darreichungsform::fromCode);
    } catch (InvalidValueSetException ivse) {
      log.error(format("{0} is not a valid Code for Darreichungsform", supplyForm));
      throw new WebApplicationException(
          Response.status(400)
              .entity(
                  new ErrorResponse(
                      format("{0} is not a valid Code for Darreichungsform", supplyForm)))
              .build());
    }
  }

  @XmlTransient
  public @Nullable String getExpirationDateString() {
    if (expirationDate != null) return new SimpleDateFormat("dd-MM-yyyy").format(expirationDate);
    else return null;
  }

  public static MedicationData create() {
    val m = new MedicationData();
    m.category = fakerValueSet(MedicationCategory.class).getCode();
    m.type = fakerValueSet(MedicationType.class).getCode();
    m.standardSize = fakerValueSet(StandardSize.class).getCode();
    m.supplyForm = fakerValueSet(Darreichungsform.class).getCode();
    m.amount = fakerAmount(1, 20);
    m.packageQuantity = fakerAmount(1, 20);
    m.pzn = fakerPzn();
    m.name = fakerDrugName();
    m.dosage = fakerDosage();
    m.substitutionAllowed = fakerBool();
    m.note = ""; // no notes for now, just an empty string
    m.lotNumber = fakerLotNumber();
    m.expirationDate = fakerFutureExpirationDate();
    return m;
  }

  public static MedicationData fromKbvBundle(KbvErpBundle bundle) {
    val m = fromMedication(bundle.getMedication());
    m.packageQuantity = bundle.getDispenseQuantity();
    m.dosage = bundle.getDosageInstruction();
    m.substitutionAllowed = bundle.isSubstitutionAllowed();
    m.note = bundle.getKbvErpMedicationRequestAsCopy().getNoteTextOrEmpty();

    return m;
  }

  public static MedicationData fromMedication(KbvErpMedication medication) {
    val m = new MedicationData();
    m.category = medication.getCategoryFirstRep().getCode();
    medication.getMedicationType().ifPresent(mt -> m.type = mt.getCode());
    m.standardSize = medication.getStandardSize().getCode();
    medication.getDarreichungsformFirstRep().ifPresent(sf -> m.supplyForm = sf.getCode());
    m.amount = getMedicationAmount(medication.getAmount());

    m.pzn = getPzn(medication.getPzn());
    m.freeText = medication.getFreeText();
    m.name = medication.getMedicationName();
    medication.getIngredientText().ifPresent(it -> m.ingredient = it);
    medication.getIngredientStrengthString().ifPresent(an -> m.ingredientStrength = an);
    m.lotNumber = medication.getBatch().getLotNumber();
    m.expirationDate = medication.getBatch().getExpirationDate();
    return m;
  }

  private static @Nullable String getPzn(List<String> pznList) {
    String ret = null;
    if (!pznList.isEmpty()) {
      ret = pznList.get(0);
    }
    return ret;
  }

  private static int getMedicationAmount(Ratio ratio) {
    int ret = 0; // Note: return simply 0 if amount was not given as this field is optional
    if (ratio.getNumerator().getValue() != null) {
      ret = ratio.getNumerator().getValue().intValue();
    }
    return ret;
  }
}
