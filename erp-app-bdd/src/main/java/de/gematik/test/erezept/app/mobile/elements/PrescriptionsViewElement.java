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

import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.PrescriptionStatus;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PrescriptionsViewElement implements PageElement {

  @Nullable private final String prescriptionName;
  private final PrescriptionStatus status;
  private final boolean isMvo;

  @Override
  public String extractSourceLabel(PlatformType platform) {
    if (prescriptionName != null)
      return this.prescriptionName;
    else 
      return this.status.getLabel();
  }

  @Override
  public String getElementName() {
    if (prescriptionName != null)
      return format("Prescription {0} ({1})", prescriptionName, status.getLabel());
    else 
      return format("Prescription with Status {0}", status.getLabel());
  }

  @Override
  public Supplier<By> getAndroidLocator() {
    return null;
  }

  @Override
  public Supplier<By> getIosLocator() {
    String expression;

    var buttonName = "erx_detailed_prescription_name-erx_detailed_prescription_validity-erx_detailed_status";
    if (isMvo) {
      buttonName += "-erx_detailed";
      if (status == PrescriptionStatus.LATER_REDEEMABLE) {
        buttonName += "_status-erx_detailed_multiple_prescription_index";
      } else if (status == PrescriptionStatus.REDEEMABLE) {
        buttonName += "_multiple_prescription_index";
      }
    }
    
    if (prescriptionName != null) {
      expression =
          format(
              "//XCUIElementTypeButton[@name=\"{0}\" and (XCUIElementTypeStaticText[@name=\"erx_detailed_prescription_name\" and @value=\"{1}\"] and XCUIElementTypeStaticText[@name=\"erx_detailed_status\" and @value=\"{2}\"])]",
              buttonName, this.prescriptionName, this.status.getLabel());
    } else {
      expression =
          format(
              "//XCUIElementTypeButton[@name=\"{0}\" and XCUIElementTypeStaticText[@name=\"erx_detailed_status\" and @value=\"{1}\"]]",
              buttonName, this.status.getLabel());
    }

    return () -> By.xpath(expression);
  }

  public static Builder withStatus(@Nullable PrescriptionStatus status) {
    return new Builder(status);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    @Nullable private final PrescriptionStatus status;
    private boolean isMvo = false;

    public Builder asMvo(boolean isMvo) {
      this.isMvo = isMvo;
      return this;
    }

    public PrescriptionsViewElement andPrescriptionName(String name) {
      return new PrescriptionsViewElement(name, status, isMvo);
    }

    public PrescriptionsViewElement withoutPrescriptionName() {
      return andPrescriptionName(null);
    }
  }
}
