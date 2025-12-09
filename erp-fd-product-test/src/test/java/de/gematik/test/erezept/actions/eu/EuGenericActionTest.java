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

package de.gematik.test.erezept.actions.eu;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.abilities.ProvidePharmacyBaseData;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.actors.EuPharmacyActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.config.dto.actor.EuPharmacyConfiguration;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class EuGenericActionTest {

  private static EuPharmacyActor euPharmacy;
  private static PatientActor sina;

  @BeforeAll
  static void setup() {
    CoverageReporter.getInstance().startTestcase("don't care");

    val cfg = new EuPharmacyConfiguration();
    cfg.setCountryCode(IsoCountryCode.NL.getCode());
    euPharmacy = new EuPharmacyActor("Hannes Vogt");
    euPharmacy.can(mock(UseTheErpClient.class));
    euPharmacy.can(ProvidePharmacyBaseData.fromConfiguration(cfg));

    sina = new PatientActor("sina");
    sina.can(mock(UseTheErpClient.class));
    val mockBaseData = mock(ProvidePatientBaseData.class);
    when(mockBaseData.getKvnr()).thenReturn(KVNR.random());
    sina.can(mockBaseData);
  }

  public static Stream<Arguments> testDataProviderPatientActions() {
    return Stream.of(
        Arguments.of(
            (Supplier<ErpAction<?>>)
                () -> GrantEuAccessPermission.withRandomAccessCode().forCountry(IsoCountryCode.NL)),
        Arguments.of(
            (Supplier<ErpAction<?>>)
                () -> GrantEuAccessPermission.withRandomAccessCode().forCountryOf(euPharmacy)),
        Arguments.of((Supplier<ErpAction<?>>) GrantEuConsent::forPatient),
        Arguments.of(
            (Supplier<ErpAction<?>>)
                () -> PatchPrescriptionForEuRedemption.of(TaskId.from("1234567890"))));
  }

  @ParameterizedTest
  @MethodSource("testDataProviderPatientActions")
  void shouldPerformActionForPatientActor(Supplier<ErpAction<?>> action) {
    assertDoesNotThrow(() -> sina.performs(action.get()));
  }
}
