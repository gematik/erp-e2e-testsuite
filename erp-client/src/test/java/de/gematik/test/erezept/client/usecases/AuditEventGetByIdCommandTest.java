package de.gematik.test.erezept.client.usecases;

import de.gematik.test.erezept.client.usecases.search.AuditEventSearch;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditEventGetByIdCommandTest {

  @Test
  void getRequestLocator() {
    val prescriptionId = PrescriptionId.random();
    val cmd = AuditEventSearch.getAuditEventsFor(prescriptionId);

    val actual = cmd.getRequestLocator();
    assertTrue(actual.contains("/Task"));       // Not AuditEvent because of _revinclude
    assertTrue(actual.contains("_revinclude=AuditEvent")); // from Task to AuditEvent via revinclude
    assertTrue(actual.contains("entity.what"));
    assertTrue(actual.contains(prescriptionId.getValue()));
  }

  @Test
  void shouldNotHaveBody() {
    val prescriptionId = PrescriptionId.random();
    val cmd = AuditEventSearch.getAuditEventsFor(prescriptionId);
    assertTrue(cmd.getRequestBody().isEmpty());
  }
}
