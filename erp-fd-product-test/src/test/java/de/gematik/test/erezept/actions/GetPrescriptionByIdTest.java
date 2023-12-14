package de.gematik.test.erezept.actions;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class GetPrescriptionByIdTest {

    private static PharmacyActor pharmacy;

    @BeforeAll
    static void setup() {
        CoverageReporter.getInstance().startTestcase("don't care");
        // init pharmacy
        pharmacy = new PharmacyActor("Am Flughafen");
        UseTheErpClient useErpClient = mock(UseTheErpClient.class);
        pharmacy.can(useErpClient);
    }

    @Test
    void shouldPerformCorrectCommandWithoutAuthentication() {
        val action = GetPrescriptionById.withTaskId(TaskId.from("123")).withoutAuthentication();
        Assertions.assertDoesNotThrow(() -> action.answeredBy(pharmacy));

    }
    @Test
    void shouldPerformCorrectCommandWithAccessCode() {
        val action = GetPrescriptionById.withTaskId(TaskId.from("123")).withAccessCode(AccessCode.fromString("321"));
        Assertions.assertDoesNotThrow(() -> action.answeredBy(pharmacy));

    }
    @Test
    void shouldPerformCorrectCommandWithSecret() {
        val action = GetPrescriptionById.withTaskId(TaskId.from("123")).withSecret(Secret.fromString("secret"));
        Assertions.assertDoesNotThrow(() -> action.answeredBy(pharmacy));

    }



}