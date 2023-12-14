package de.gematik.test.erezept.app.mobile.elements;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class MedicationDispenseDetailsTest {

  @Test
  void shouldHaveLocatorForIos() {
    Arrays.stream(MedicationDispenseDetails.values())
        .map(MedicationDispenseDetails::getIosLocator)
        .forEach(element -> assertNotNull(element.get()));
  }

  @Test
  void shouldNotHaveLocatorsForAndroid() {
    Arrays.stream(MedicationDispenseDetails.values())
        .map(MedicationDispenseDetails::getAndroidLocator)
        .forEach(element -> assertNull(element.get()));
  }
}
