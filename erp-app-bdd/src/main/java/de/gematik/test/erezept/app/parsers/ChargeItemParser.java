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

package de.gematik.test.erezept.app.parsers;

import static java.lang.String.format;

import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItemBundle;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
public class ChargeItemParser {
  public static String getExpectedHandedOverDate(ErxChargeItemBundle erxChargeItemBundle) {
    val abgabedatenBundle = erxChargeItemBundle.getAbgabedatenBundle();
    val expectedHandedOverDate = abgabedatenBundle.getDispensedMedication().getWhenHandedOver();

    val fTime = new SimpleDateFormat("HH:mm").format(expectedHandedOverDate);
    return format("Heute, %s", fTime);
  }

  public static String getExpectedEnterer(ErxChargeItemBundle erxChargeItemBundle) {
    val abgabedatenBundle = erxChargeItemBundle.getAbgabedatenBundle();
    return abgabedatenBundle.getPharmacy().getName();
  }

  public static String getExpectedEnteredDate(ErxChargeItemBundle erxChargeItemBundle) {
    val chargeItem = erxChargeItemBundle.getChargeItem();
    val expectedEnteredDate = chargeItem.getEnteredDate();

    val fTime = new SimpleDateFormat("HH:mm").format(expectedEnteredDate);
    return format("Heute, %s", fTime);
  }

  public static String getExpectedPrice(ErxChargeItemBundle erxChargeItemBundle) {
    val abgabedatenBundle = erxChargeItemBundle.getAbgabedatenBundle();
    val expectedPrice = abgabedatenBundle.getInvoice().getTotalPrice();

    val fmt = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    return fmt.format(expectedPrice);
  }
}
