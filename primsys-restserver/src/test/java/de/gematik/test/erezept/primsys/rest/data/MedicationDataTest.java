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

import static de.gematik.test.erezept.primsys.rest.data.MedicationData.create;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MedicationDataTest {

  private ObjectMapper mapper;

  @BeforeEach
  public void setup() {
    mapper = new ObjectMapper();
  }

  @Test
  void roundTripSerialize() throws JsonProcessingException {
    val data = create();
    data.setNote("example note");
    Assertions.assertTrue(mapper.canSerialize(MedicationData.class));
    assertNotNull(data);
    val json = mapper.writeValueAsString(data);
    assertNotNull(json);
  }

  @Test
  void roundTripSerializeFromKbvBundle() throws JsonProcessingException {
    val kbvBundle = KbvErpBundleBuilder.faker("X123456789", "04773414").build();
    val data = MedicationData.fromKbvBundle(kbvBundle);
    assertNotNull(data);
    Assertions.assertEquals(kbvBundle.getMedication().getMedicationName(), data.getName());
    Assertions.assertEquals(kbvBundle.getMedication().getPzn().get(0), data.getPzn());
    val json = mapper.writeValueAsString(data);
    assertNotNull(json);
  }

  @Test
  void createAndFillNull() {
    var partMediData = new MedicationData();
    partMediData.setName("NoNameProduct");
    partMediData.fakeMissing();
    assertFalse(partMediData.getCategory().isEmpty());
    assertFalse(partMediData.getType().isEmpty());
    assertFalse(partMediData.getStandardSize().isEmpty());
    assertFalse(partMediData.getSupplyForm().isEmpty());
    Assertions.assertTrue(0 < partMediData.getAmount());
    assertTrue(partMediData.getAmount() <= 20);
    assertFalse(partMediData.getIngredientStrength().isEmpty());
    assertFalse(partMediData.getPackageQuantity() < 1);
    assertFalse(partMediData.getPzn().isEmpty());
    assertNotNull(partMediData.getPzn());
    assertFalse(partMediData.getName().isEmpty());
    assertNotNull(partMediData.getName());
    assertFalse(partMediData.getIngredient().isEmpty());
    assertNotNull(partMediData.getIngredient());
    assertFalse(partMediData.getDosage().isEmpty());
    assertNotNull(partMediData.getDosage());
    assertFalse(partMediData.getDosage().isEmpty());
    assertNotNull(partMediData.getNote());
    assertFalse(partMediData.getLotNumber().isEmpty());
    assertNotNull(partMediData.getLotNumber());
    assertFalse(Objects.requireNonNull(partMediData.getExpirationDateString()).isEmpty());
    assertNotNull(partMediData.getExpirationDate());
  }

  @Test
  void createAndFillOnlyLotNum() {
    var medicationData1 = create();
    medicationData1.setLotNumber(null);
    medicationData1.fakeMissing();
    assertNotNull(medicationData1.getLotNumber());
    assertFalse(medicationData1.getLotNumber().isEmpty());
  }

  @Test
  void getExpirationDateIsNotNull1() {
    var medicationData1 = create();
    assertNotNull(medicationData1.getExpirationDate());
  }

  @Test
  void getExpirationDateIsNotNull2() {
    var medicationData1 = new MedicationData();
    medicationData1.setExpirationDate(null);
    medicationData1.fakeMissing();
    assertNotNull(medicationData1.getExpirationDate());
  }

  @Test
  void getExpirationDateIsNotNull3() {
    var medicationData1 = new MedicationData();
    medicationData1.fakeMissing();
    assertNotNull(medicationData1.getExpirationDate());
  }

  @SneakyThrows
  @Test
  void getExpirationDateIsNotNullAfterBuildAnJson() {
    var medicationData1 = new MedicationData();
    medicationData1.setExpirationDate(null);
    medicationData1.fakeMissing();
    val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(medicationData1);
    assertFalse(json.split(",")[14].contains("null"));
  }

  @Test
  void createMediDataWithNullPreset() {
    var medData = new MedicationData();
    medData.setCategory(null);
    medData.setType(null);
    medData.setStandardSize(null);
    medData.setSupplyForm(null);
    medData.setIngredientStrength(null);
    medData.setPackageQuantity(0);
    medData.setPzn(null);
    medData.setFreeText(null);
    medData.setName(null);
    medData.setIngredient(null);
    medData.setDosage(null);
    medData.setNote(null);
    medData.setLotNumber(null);
    medData.setExpirationDate(null);
    medData.setSubstitutionAllowed(false);
    medData.fakeMissing();
    assertFalse(medData.getCategory().isEmpty());
    assertFalse(medData.getType().isEmpty());
    assertFalse(medData.getStandardSize().isEmpty());
    assertFalse(medData.getSupplyForm().isEmpty());
    assertFalse(medData.getIngredientStrength().isEmpty());
    assertFalse(medData.getPackageQuantity() <= 0);
    assertFalse(medData.getFreeText().isEmpty());
    assertFalse(medData.getPzn().isEmpty());
    assertFalse(medData.getFreeText().isEmpty());
    assertFalse(medData.getName().isEmpty());
    assertFalse(medData.getIngredient().isEmpty());
    assertFalse(medData.getDosage().isEmpty());
    assertFalse(medData.getNote().isEmpty());
    assertFalse(medData.getLotNumber().isEmpty());
    assertNotNull(medData.getExpirationDate());
  }

  @Test
  void createMediDataWithEmptyPreset() {
    var medData = new MedicationData();
    medData.setCategory("");
    medData.setType("");
    medData.setStandardSize("");
    medData.setSupplyForm("");
    medData.setIngredientStrength("");
    medData.setPackageQuantity(0);
    medData.setPzn("");
    medData.setFreeText("");
    medData.setName("");
    medData.setIngredient("");
    medData.setDosage("");
    medData.setNote("");
    medData.setLotNumber("");
    medData.fakeMissing();
    assertFalse(medData.getCategory().isEmpty());
    assertFalse(medData.getType().isEmpty());
    assertFalse(medData.getStandardSize().isEmpty());
    assertFalse(medData.getSupplyForm().isEmpty());
    assertFalse(medData.getIngredientStrength().isEmpty());
    assertFalse(medData.getPackageQuantity() <= 0);
    assertFalse(medData.getFreeText().isEmpty());
    assertFalse(medData.getPzn().isEmpty());
    assertFalse(medData.getFreeText().isEmpty());
    assertFalse(medData.getName().isEmpty());
    assertFalse(medData.getIngredient().isEmpty());
    assertFalse(medData.getDosage().isEmpty());
    assertFalse(medData.getNote().isEmpty());
    assertFalse(medData.getLotNumber().isEmpty());
  }

  @Test
  void fakeMissingAcceptNoMvoData() {
    var medData = MedicationData.create();
    assertNull(medData.getMvoData());
    medData.fakeMissing();
    assertNull(medData.getMvoData());
  }
}
