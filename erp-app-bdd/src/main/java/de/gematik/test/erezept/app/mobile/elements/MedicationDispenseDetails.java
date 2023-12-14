package de.gematik.test.erezept.app.mobile.elements;

import io.appium.java_client.AppiumBy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;

import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public enum MedicationDispenseDetails implements PageElement {
  PRESCRIBED(
      "prescribed medication",
      () -> AppiumBy.accessibilityId("prsc_dtl_med_ov_btn_subscribed_medication")),
  DISPENSED(
      "dispensed medication",
      () -> AppiumBy.accessibilityId("prsc_dtl_med_ov_btn_dispensed_medication"));

  private final String elementName;
  private final Supplier<By> iosLocator;

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> null;
  }
}
