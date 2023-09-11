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

import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorAlertTest {

  @Test
  void shouldExtractErrorMessage() {
    val errorLines =
        List.of(
            "Fehler beim Lesen der Gesundheitskarte",
            "Bitte versuchen Sie es erneut\n\nFehlernummern:  i-00406, i-00503");
    val errorMessage = ErrorAlert.GENERIC.extractErrorMessage(errorLines);
    assertTrue(errorMessage.contains("Fehler beim Lesen der Gesundheitskarte:"));
    assertTrue(errorMessage.contains("i-00406, i-00503"));
  }

  @Test
  void onlyAvailableForIos() {
    val errorAlert = ErrorAlert.GENERIC;
    assertThrows(
        UnsupportedPlatformException.class,
        () -> errorAlert.extractSourceLabel(PlatformType.ANDROID));
  }
}
