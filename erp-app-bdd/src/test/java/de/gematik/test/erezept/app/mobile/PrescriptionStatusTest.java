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

package de.gematik.test.erezept.app.mobile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedicationRequest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.Test;

class PrescriptionStatusTest {

  @Test
  void shouldCreateRedeemableFromPeriod() {
    val p = new Period();
    p.setStart(new Date());
    val status = PrescriptionStatus.from(p);
    assertEquals(PrescriptionStatus.REDEEMABLE, status);
  }

  @Test
  void shouldCreateLaterRedeemableFromPeriod() {
    val p = new Period();
    p.setStart(
        Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    val status = PrescriptionStatus.from(p);
    assertEquals(PrescriptionStatus.LATER_REDEEMABLE, status);
  }
  
  @Test
  void shouldCreateClaimedForInProgressTask() {
    val bundle = mock(ErxPrescriptionBundle.class);
    val task = mock(ErxTask.class);
    when(bundle.getTask()).thenReturn(task);
    when(task.getStatus()).thenReturn(Task.TaskStatus.INPROGRESS);
    
    val status = PrescriptionStatus.from(bundle);
    assertEquals(PrescriptionStatus.CLAIMED, status);
  }

  @Test
  void shouldCreateLaterRedeemableFromPrescriptionBundle() {
    val bundle = mock(ErxPrescriptionBundle.class);
    val task = mock(ErxTask.class);
    val kbvBundle = mock(KbvErpBundle.class);
    val medicationRequest = mock(KbvErpMedicationRequest.class);
    
    when(bundle.getTask()).thenReturn(task);
    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(bundle.getKbvBundle()).thenReturn(kbvBundle);
    when(kbvBundle.getMedicationRequest()).thenReturn(medicationRequest);

    val mvoPeriod = new Period();
    mvoPeriod.setStart(
            Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    when(medicationRequest.getMvoPeriod()).thenReturn(Optional.of(mvoPeriod));

    val status = PrescriptionStatus.from(bundle);
    assertEquals(PrescriptionStatus.LATER_REDEEMABLE, status);
  }

  @Test
  void shouldCreateRedeemableFromPrescriptionBundle() {
    val bundle = mock(ErxPrescriptionBundle.class);
    val task = mock(ErxTask.class);
    val kbvBundle = mock(KbvErpBundle.class);
    val medicationRequest = mock(KbvErpMedicationRequest.class);

    when(bundle.getTask()).thenReturn(task);
    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(bundle.getKbvBundle()).thenReturn(kbvBundle);
    when(kbvBundle.getMedicationRequest()).thenReturn(medicationRequest);

    val mvoPeriod = new Period();
    mvoPeriod.setStart(
            Date.from(LocalDate.now().minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant()));
    when(medicationRequest.getMvoPeriod()).thenReturn(Optional.of(mvoPeriod));

    val status = PrescriptionStatus.from(bundle);
    assertEquals(PrescriptionStatus.REDEEMABLE, status);
  }
}
