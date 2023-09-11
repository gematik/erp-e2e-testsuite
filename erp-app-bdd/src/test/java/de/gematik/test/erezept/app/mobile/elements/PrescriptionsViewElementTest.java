/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.app.mobile.elements;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.app.exceptions.UnavailablePageElementLocatorException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.PrescriptionStatus;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

class PrescriptionsViewElementTest {

  @Test
  void shouldExtractOnlyLabel() {
    val pe =
        PrescriptionsViewElement.withStatus(PrescriptionStatus.REDEEMABLE)
            .withoutPrescriptionName();
    assertEquals(PrescriptionStatus.REDEEMABLE.getLabel(), pe.extractSourceLabel(PlatformType.IOS));
    assertTrue(pe.getElementName().contains(format("with Status {0}", PrescriptionStatus.REDEEMABLE.getLabel())));
  }

  @Test
  void shouldExtractOnlyPrescriptionName() {
    val pe =
        PrescriptionsViewElement.withStatus(PrescriptionStatus.REDEEMABLE)
            .andPrescriptionName("Schmerzmittel");
    assertEquals("Schmerzmittel", pe.extractSourceLabel(PlatformType.IOS));
    assertEquals("Prescription Schmerzmittel (Einlösbar)", pe.getElementName());
  }

  @Test
  void shouldNotHaveAndroidImplementation() {
    val pe = PrescriptionsViewElement.withStatus(PrescriptionStatus.REDEEMABLE)
            .withoutPrescriptionName();
    assertThrows(UnavailablePageElementLocatorException.class, () -> pe.forPlatform(PlatformType.ANDROID));
  }
  
  @Test
  void shouldHaveIosImplementation() {
    val pe = PrescriptionsViewElement.withStatus(PrescriptionStatus.REDEEMABLE)
            .withoutPrescriptionName();
    val by = pe.forPlatform(PlatformType.IOS);
    assertNotNull(by);
    assertEquals(By.ByXPath.class, by.getClass());
    assertTrue(by.toString().contains(PrescriptionStatus.REDEEMABLE.getLabel()));
  }

  @Test
  void shouldHaveIosImplementationWithPrescriptionName() {
    val pe = PrescriptionsViewElement.withStatus(PrescriptionStatus.REDEEMABLE)
            .andPrescriptionName("Schmerzmittel");
    val by = pe.forPlatform(PlatformType.IOS);
    assertNotNull(by);
    assertEquals(By.ByXPath.class, by.getClass());
    assertTrue(by.toString().contains(PrescriptionStatus.REDEEMABLE.getLabel()));
    assertTrue(by.toString().contains("Schmerzmittel"));
  }

  @Test
  void shouldHaveIosImplementationWithPrescriptionNameForMvo() {
    val pe = PrescriptionsViewElement.withStatus(PrescriptionStatus.REDEEMABLE)
            .asMvo(true)
            .andPrescriptionName("Schmerzmittel");
    val by = pe.forPlatform(PlatformType.IOS);
    assertNotNull(by);
    assertEquals(By.ByXPath.class, by.getClass());
    assertTrue(by.toString().contains(PrescriptionStatus.REDEEMABLE.getLabel()));
    assertTrue(by.toString().contains("Schmerzmittel"));
    assertTrue(by.toString().contains("_multiple_prescription_index"));
  }

  @Test
  void shouldHaveIosImplementationWithPrescriptionNameForMvoLaterRedeemable() {
    val pe = PrescriptionsViewElement.withStatus(PrescriptionStatus.LATER_REDEEMABLE)
            .asMvo(true)
            .andPrescriptionName("Schmerzmittel");
    val by = pe.forPlatform(PlatformType.IOS);
    assertNotNull(by);
    assertEquals(By.ByXPath.class, by.getClass());
    assertTrue(by.toString().contains(PrescriptionStatus.LATER_REDEEMABLE.getLabel()));
    assertTrue(by.toString().contains("Schmerzmittel"));
    assertTrue(by.toString().contains("_status-erx_detailed_multiple_prescription_index"));
  }
}
