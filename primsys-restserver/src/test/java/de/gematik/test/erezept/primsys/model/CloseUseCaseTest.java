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

package de.gematik.test.erezept.primsys.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import de.gematik.test.erezept.primsys.model.actor.Pharmacy;
import de.gematik.test.erezept.primsys.rest.data.AcceptData;
import de.gematik.test.smartcard.SmcB;
import jakarta.ws.rs.WebApplicationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Map;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class CloseUseCaseTest {

  private static final String taskId = "123435asdfgh";
  private static final String accessCode = "getAcsessWithThatCode123456879";
  private static final String secret = "verrySecrets3cr3t";
  private static final String kbvBundleAsString =
      "<Bundle xmlns=\"http://hl7.org/fhir\">\n  <id value=\"1f339db0-9e55-4946-9dfa-f1b30953be9b\" />\n  <meta>\n    <lastUpdated value=\"2021-05-19T08:30:00Z\" />\n    <profile value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.2\" />\n  </meta>\n  <identifier>\n    <system value=\"https://gematik.de/fhir/NamingSystem/PrescriptionID\" />\n    <value value=\"160.100.000.000.037.28\" />\n  </identifier>\n  <type value=\"document\" />\n  <timestamp value=\"2021-05-19T08:30:00Z\" />\n  <entry>\n    <fullUrl value=\"http://pvs.praxis.local/fhir/Composition/88c4029f-dfab-415a-b6de-64fd1a4058e6\" />\n    <resource>\n      <Composition xmlns=\"http://hl7.org/fhir\">\n        <id value=\"88c4029f-dfab-415a-b6de-64fd1a4058e6\" />\n        <meta>\n          <profile value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Composition|1.0.2\" />\n        </meta>\n        <extension url=\"https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis\">\n          <valueCoding>\n            <system value=\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_STATUSKENNZEICHEN\" />\n            <code value=\"00\" />\n          </valueCoding>\n        </extension>\n        <status value=\"final\" />\n        <type>\n          <coding>\n            <system value=\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_FORMULAR_ART\" />\n            <code value=\"e16A\" />\n          </coding>\n        </type>\n        <subject>\n          <reference value=\"Patient/93866fdc-3e50-4902-a7e9-891b54737b5e\" />\n        </subject>\n        <date value=\"2021-05-19T08:00:00Z\" />\n        <author>\n          <reference value=\"Practitioner/d6f3b55d-3095-4655-96dc-da3bec21271c\" />\n          <type value=\"Practitioner\" />\n        </author>\n        <author>\n          <type value=\"Device\" />\n          <identifier>\n            <system value=\"https://fhir.kbv.de/NamingSystem/KBV_NS_FOR_Pruefnummer\" />\n            <value value=\"Y/400/2107/36/999\" />\n          </identifier>\n        </author>\n        <title value=\"elektronische Arzneimittelverordnung\" />\n        <custodian>\n          <reference value=\"Organization/2a555cd3-0543-483c-88b3-f68647620962\" />\n        </custodian>\n        <section>\n          <code>\n            <coding>\n              <system value=\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\" />\n              <code value=\"Prescription\" />\n            </coding>\n          </code>\n          <entry>\n            <!-- Referenz auf Verordnung (MedicationRequest) -->\n            <reference value=\"MedicationRequest/43c2b7ae-ad11-4387-910a-e6b7a3c38d4f\" />\n          </entry>\n        </section>\n        <section>\n          <code>\n            <coding>\n              <system value=\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Section_Type\" />\n              <code value=\"Coverage\" />\n            </coding>\n          </code>\n          <entry>\n            <!-- Referenz auf Krankenkasse/KostentrĂ¤ger  -->\n            <reference value=\"Coverage/1b89236c-ab14-4e92-937e-5af0b59d0cd4\" />\n          </entry>\n        </section>\n      </Composition>\n    </resource>\n  </entry>\n  <entry>\n    <fullUrl value=\"http://pvs.praxis.local/fhir/MedicationRequest/43c2b7ae-ad11-4387-910a-e6b7a3c38d4f\" />\n    <resource>\n      <MedicationRequest xmlns=\"http://hl7.org/fhir\">\n        <!--Beispiel MedicationRequest für eine PZN-Verordnung -->\n        <id value=\"43c2b7ae-ad11-4387-910a-e6b7a3c38d4f\" />\n        <meta>\n          <profile value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Prescription|1.0.2\" />\n        </meta>\n        <extension url=\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_StatusCoPayment\">\n          <valueCoding>\n            <system value=\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_StatusCoPayment\" />\n            <code value=\"0\" />\n          </valueCoding>\n        </extension>\n        <extension url=\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_EmergencyServicesFee\">\n          <valueBoolean value=\"false\" />\n        </extension>\n        <extension url=\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_BVG\">\n          <valueBoolean value=\"false\" />\n        </extension>\n        <extension url=\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Multiple_Prescription\">\n          <extension url=\"Kennzeichen\">\n            <valueBoolean value=\"false\" />\n          </extension>\n        </extension>\n        <status value=\"active\" />\n        <intent value=\"order\" />\n        <medicationReference>\n          <reference value=\"Medication/5ff1bd22-ce14-484e-be56-d2ba4adeac31\" />\n        </medicationReference>\n        <subject>\n          <reference value=\"Patient/93866fdc-3e50-4902-a7e9-891b54737b5e\" />\n        </subject>\n        <authoredOn value=\"2021-05-19\" />\n        <requester>\n          <reference value=\"Practitioner/d6f3b55d-3095-4655-96dc-da3bec21271c\" />\n        </requester>\n        <insurance>\n          <reference value=\"Coverage/1b89236c-ab14-4e92-937e-5af0b59d0cd4\" />\n        </insurance>\n        <dispenseRequest>\n          <quantity>\n            <value value=\"1\" />\n            <system value=\"http://unitsofmeasure.org\" />\n            <code value=\"{Package}\" />\n          </quantity>\n        </dispenseRequest>\n        <substitution>\n          <allowedBoolean value=\"true\" />\n        </substitution>\n      </MedicationRequest>\n    </resource>\n  </entry>\n  <entry>\n    <fullUrl value=\"http://pvs.praxis.local/fhir/Medication/5ff1bd22-ce14-484e-be56-d2ba4adeac31\" />\n    <resource>\n      <Medication xmlns=\"http://hl7.org/fhir\">\n        <id value=\"5ff1bd22-ce14-484e-be56-d2ba4adeac31\" />\n        <meta>\n          <profile value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Medication_PZN|1.0.2\" />\n        </meta>\n        <extension url=\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category\">\n          <valueCoding>\n            <system value=\"https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Category\" />\n            <code value=\"00\" />\n          </valueCoding>\n        </extension>\n        <extension url=\"https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Vaccine\">\n          <valueBoolean value=\"false\" />\n        </extension>\n        <extension url=\"http://fhir.de/StructureDefinition/normgroesse\">\n          <valueCode value=\"N1\" />\n        </extension>\n        <code>\n          <coding>\n            <system value=\"http://fhir.de/CodeSystem/ifa/pzn\" />\n            <code value=\"07765007\" />\n          </coding>\n          <text value=\"NEUPRO 8MG/24H PFT 7 ST\" />\n        </code>\n        <form>\n          <coding>\n            <system value=\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DARREICHUNGSFORM\" />\n            <code value=\"PFT\" />\n          </coding>\n        </form>\n      </Medication>\n    </resource>\n  </entry>\n  <entry>\n    <fullUrl value=\"http://pvs.praxis.local/fhir/Patient/93866fdc-3e50-4902-a7e9-891b54737b5e\" />\n    <resource>\n      <Patient xmlns=\"http://hl7.org/fhir\">\n        <id value=\"93866fdc-3e50-4902-a7e9-891b54737b5e\" />\n        <meta>\n          <profile value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient|1.0.3\" />\n        </meta>\n        <identifier>\n          <type>\n            <coding>\n              <system value=\"http://fhir.de/CodeSystem/identifier-type-de-basis\" />\n              <code value=\"GKV\" />\n            </coding>\n          </type>\n          <system value=\"http://fhir.de/NamingSystem/gkv/kvid-10\" />\n          <value value=\"K220635158\" />\n        </identifier>\n        <name>\n          <use value=\"official\" />\n          <family value=\"Königsstein\">\n            <extension url=\"http://hl7.org/fhir/StructureDefinition/humanname-own-name\">\n              <valueString value=\"Königsstein\" />\n            </extension>\n          </family>\n          <given value=\"Ludger\" />\n        </name>\n        <birthDate value=\"1935-06-22\" />\n        <address>\n          <type value=\"both\" />\n          <line value=\"Blumenweg\">\n            <extension url=\"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName\">\n              <valueString value=\"Blumenweg\" />\n            </extension>\n          </line>\n          <city value=\"Esens\" />\n          <postalCode value=\"26427\" />\n          <country value=\"D\" />\n        </address>\n      </Patient>\n    </resource>\n  </entry>\n  <entry>\n    <fullUrl value=\"http://pvs.praxis.local/fhir/Practitioner/d6f3b55d-3095-4655-96dc-da3bec21271c\" />\n    <resource>\n      <Practitioner xmlns=\"http://hl7.org/fhir\">\n        <id value=\"d6f3b55d-3095-4655-96dc-da3bec21271c\" />\n        <meta>\n          <profile value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner|1.0.3\" />\n        </meta>\n        <identifier>\n          <type>\n            <coding>\n              <system value=\"http://terminology.hl7.org/CodeSystem/v2-0203\" />\n              <code value=\"LANR\" />\n            </coding>\n          </type>\n          <system value=\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_ANR\" />\n          <value value=\"754236701\" />\n        </identifier>\n        <name>\n          <use value=\"official\" />\n          <family value=\"Schulz\">\n            <extension url=\"http://hl7.org/fhir/StructureDefinition/humanname-own-name\">\n              <valueString value=\"Schulz\" />\n            </extension>\n          </family>\n          <given value=\"Ben\" />\n        </name>\n        <qualification>\n          <code>\n            <coding>\n              <system value=\"https://fhir.kbv.de/CodeSystem/KBV_CS_FOR_Qualification_Type\" />\n              <code value=\"00\" />\n            </coding>\n          </code>\n        </qualification>\n        <qualification>\n          <code>\n            <text value=\"Facharzt für Allgemeinmedizin\" />\n          </code>\n        </qualification>\n      </Practitioner>\n    </resource>\n  </entry>\n  <entry>\n    <fullUrl value=\"http://pvs.praxis.local/fhir/Organization/2a555cd3-0543-483c-88b3-f68647620962\" />\n    <resource>\n      <Organization xmlns=\"http://hl7.org/fhir\">\n        <id value=\"2a555cd3-0543-483c-88b3-f68647620962\" />\n        <meta>\n          <profile value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization|1.0.3\" />\n        </meta>\n        <identifier>\n          <type>\n            <coding>\n              <system value=\"http://terminology.hl7.org/CodeSystem/v2-0203\" />\n              <code value=\"BSNR\" />\n            </coding>\n          </type>\n          <system value=\"https://fhir.kbv.de/NamingSystem/KBV_NS_Base_BSNR\" />\n          <value value=\"724444400\" />\n        </identifier>\n        <name value=\"Hausarztpraxis\" />\n        <telecom>\n          <system value=\"phone\" />\n          <value value=\"030321654987\" />\n        </telecom>\n        <telecom>\n          <system value=\"email\" />\n          <value value=\"hausarztpraxis@e-mail.de\" />\n        </telecom>\n        <address>\n          <type value=\"both\" />\n          <line value=\"Herbert-Lewin-Platz 2\">\n            <extension url=\"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber\">\n              <valueString value=\"2\" />\n            </extension>\n            <extension url=\"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName\">\n              <valueString value=\"Herbert-Lewin-Platz\" />\n            </extension>\n          </line>\n          <line value=\"Erdgeschoss\">\n            <extension url=\"http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-additionalLocator\">\n              <valueString value=\"Erdgeschoss\" />\n            </extension>\n          </line>\n          <city value=\"Berlin\" />\n          <postalCode value=\"10623\" />\n          <country value=\"D\" />\n        </address>\n      </Organization>\n    </resource>\n  </entry>\n  <entry>\n    <fullUrl value=\"http://pvs.praxis.local/fhir/Coverage/1b89236c-ab14-4e92-937e-5af0b59d0cd4\" />\n    <resource>\n      <Coverage xmlns=\"http://hl7.org/fhir\">\n        <id value=\"1b89236c-ab14-4e92-937e-5af0b59d0cd4\" />\n        <meta>\n          <profile value=\"https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.0.3\" />\n        </meta>\n        <extension url=\"http://fhir.de/StructureDefinition/gkv/besondere-personengruppe\">\n          <valueCoding>\n            <system value=\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_PERSONENGRUPPE\" />\n            <code value=\"00\" />\n          </valueCoding>\n        </extension>\n        <extension url=\"http://fhir.de/StructureDefinition/gkv/dmp-kennzeichen\">\n          <valueCoding>\n            <system value=\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_DMP\" />\n            <code value=\"05\" />\n          </valueCoding>\n        </extension>\n        <extension url=\"http://fhir.de/StructureDefinition/gkv/wop\">\n          <valueCoding>\n            <system value=\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ITA_WOP\" />\n            <code value=\"17\" />\n          </valueCoding>\n        </extension>\n        <extension url=\"http://fhir.de/StructureDefinition/gkv/versichertenart\">\n          <valueCoding>\n            <system value=\"https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_KBV_VERSICHERTENSTATUS\" />\n            <code value=\"5\" />\n          </valueCoding>\n        </extension>\n        <status value=\"active\" />\n        <type>\n          <coding>\n            <system value=\"http://fhir.de/CodeSystem/versicherungsart-de-basis\" />\n            <code value=\"GKV\" />\n          </coding>\n        </type>\n        <beneficiary>\n          <reference value=\"Patient/93866fdc-3e50-4902-a7e9-891b54737b5e\" />\n        </beneficiary>\n        <payor>\n          <identifier>\n            <system value=\"http://fhir.de/NamingSystem/arge-ik/iknr\" />\n            <value value=\"109719018\" />\n          </identifier>\n          <display value=\"AOK Nordost\" />\n        </payor>\n      </Coverage>\n    </resource>\n  </entry>\n</Bundle>";

  private AcceptData createAcceptData() {
    val acceptData = new AcceptData();
    acceptData.setTaskId(taskId);
    acceptData.setAccessCode(accessCode);
    acceptData.setSecret(secret);
    acceptData.setKbvBundle(kbvBundleAsString);
    return acceptData;
  }

  @Test
  void constructorShouldNotBeCallable() throws NoSuchMethodException {
    val constructor = CloseUseCase.class.getDeclaredConstructor();
    assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    constructor.setAccessible(true);
    assertThrows(InvocationTargetException.class, constructor::newInstance);
  }

  @Test
  void closePrescriptionShouldWork() {
    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mockActorContext = mock(ActorContext.class);
      mockedStaticActor.when(ActorContext::getInstance).thenReturn(mockActorContext);
      val mockPharmacyActor = mock(Pharmacy.class);
      val mockClient = mock(ErpClient.class);
      when(mockPharmacyActor.getClient()).thenReturn(mockClient);
      val mockSmcb = mock(SmcB.class);
      when(mockPharmacyActor.getSmcb()).thenReturn(mockSmcb);
      when(mockSmcb.getTelematikId()).thenReturn("1233456_TelematikId");
      val mockFhir = mock(FhirParser.class);
      when(mockClient.getFhir()).thenReturn(mockFhir);
      when(mockFhir.decode(
              eq(KbvErpBundle.class),
              anyString())) // (Class<T> expectedClass, @NonNull final String content
          .thenReturn(KbvErpBundleBuilder.faker().build());
      val acceptDataList = new ArrayList<AcceptData>();
      acceptDataList.add(this.createAcceptData());
      val erpResponseMock = mock(ErpResponse.class);
      when(mockPharmacyActor.erpRequest(any())).thenReturn(erpResponseMock);
      when(erpResponseMock.getStatusCode()).thenReturn(204);
      when(mockActorContext.getAcceptedPrescriptions()).thenReturn(acceptDataList);
      try (val response = CloseUseCase.closePrescription(mockPharmacyActor, taskId, secret)) {
        assertEquals(200, response.getStatus());
      }
    }
  }

  @Test
  void closePrescrptionShoulShouldThrowWebAppExceptionBecauseNoAcceptData() {
    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mockActorContext = mock(ActorContext.class);
      mockedStaticActor.when(ActorContext::getInstance).thenReturn(mockActorContext);
      val operationOutcome = FhirTestResourceUtil.createOperationOutcome();
      val mockPharmacyActor = mock(Pharmacy.class);
      when(mockPharmacyActor.erpRequest(any()))
          .thenReturn(new ErpResponse(500, Map.of(), operationOutcome));
      try (val response =
          CloseUseCase.closePrescription(mockPharmacyActor, "taskId", "accessCode")) {
        fail("RejectUseCase did not throw the expected Exception");
      } catch (WebApplicationException wae) {
        assertEquals(WebApplicationException.class, wae.getClass());
        assertEquals(404, wae.getResponse().getStatus());
      }
    }
  }
}
