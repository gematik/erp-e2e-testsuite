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

package de.gematik.test.fuzzing.erx;

import static de.gematik.test.fuzzing.erx.ErxChargeItemManipulatorFactory.binaryVersionManipulator;
import static de.gematik.test.fuzzing.erx.ErxChargeItemManipulatorFactory.supportingReferenceManipulator;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.dav.DavPkvAbgabedatenFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemFaker;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvBasisStructDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.val;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ErxChargeItemManipulatorFactoryTest extends ErpFhirBuildingTest {

  private static String getReference(ErxChargeItem cI, KbvBasisStructDef structDef) {
    return cI.getSupportingInformation().stream()
        .filter(si -> structDef.matches(si.getDisplay()))
        .findFirst()
        .get()
        .getReference();
  }

  private static String getReference(ErxChargeItem cI, ErpWorkflowStructDef structDef) {
    return cI.getSupportingInformation().stream()
        .filter(si -> structDef.matches(si.getDisplay()))
        .findFirst()
        .get()
        .getReference();
  }

  private static void setReference(
      ErxChargeItem cI, ErpWorkflowStructDef strukDef, String reference) {
    cI.getSupportingInformation()
        .add(new Reference().setDisplay(strukDef.getCanonicalUrl()).setReference(reference));
  }

  private static void setReference(ErxChargeItem cI, KbvBasisStructDef strukDef, String reference) {
    cI.getSupportingInformation()
        .add(new Reference().setDisplay(strukDef.getCanonicalUrl()).setReference(reference));
  }

  @Test
  void shouldManipulateChargeItem() {
    val cI = ErxChargeItemFaker.builder().fake();
    val orgProfile = cI.getContained().get(0).getMeta().getProfile();
    val manipulators = binaryVersionManipulator();
    manipulators.forEach(m -> m.getParameter().accept(cI));
    assertNotEquals(orgProfile, cI.getContained().get(0).getMeta().getProfile());
    assertTrue(
        cI.getContained()
            .get(0)
            .getMeta()
            .getProfile()
            .get(0)
            .getValue()
            .contains(ErpWorkflowStructDef.BINARY.getCanonicalUrl()));
  }

  @Test
  void shouldDetectChargeItemBinaryVersionAfterManipulation() {
    val cI = ErxChargeItemFaker.builder().fake();
    assertFalse(cI.getContained().get(0).getMeta().hasProfile());

    val manipulators = binaryVersionManipulator();
    manipulators.forEach(m -> m.getParameter().accept(cI));
    assertTrue(
        cI.getContained()
            .get(0)
            .getMeta()
            .getProfile()
            .get(0)
            .getValue()
            .contains(ErpWorkflowStructDef.BINARY.getCanonicalUrl()));
  }

  @Test
  void shouldDetectChargeItemSupportingInformation() {
    val cI = ErxChargeItemFaker.builder().fake();
    assertTrue(cI.hasSupportingInformation());
    val manipulators = supportingReferenceManipulator();
    manipulators.forEach(
        m -> {
          assertDoesNotThrow(() -> m.getParameter().accept(cI));
        });
  }

  @Test
  void shouldHaveCorrectCount() {
    val manipulators = supportingReferenceManipulator();
    assertEquals(12, manipulators.size());
  }

  @Test
  void shouldManipulate_AllSupportingInformation() {
    val cI = ErxChargeItemFaker.builder().fake();
    assertTrue(cI.hasSupportingInformation());
    val davAbgabedatenBundle = DavPkvAbgabedatenFaker.builder(PrescriptionId.random()).fake();
    val signedDavBundle = "konnektor.signDocumentWithSmcb(davXml).getPayload()".getBytes();
    val changedChargeItem =
        cI.withChangedContainedBinaryData(davAbgabedatenBundle.getReference(), signedDavBundle);
    val manipulators = supportingReferenceManipulator();
    manipulators.forEach(
        m -> {
          assertDoesNotThrow(() -> m.getParameter().accept(changedChargeItem));
        });
  }

  ///////////////////////////////////  ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE
  // ////////////////////////////////////////////
  @Test
  void shouldManipulateAllSupportingInformationF() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = "123456789";
    setReference(cI, ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE, reference);
    supportingReferenceManipulator()
        .forEach(
            m -> {
              assertDoesNotThrow(() -> m.getParameter().accept(cI));
            });
    var manipulatedRef = getReference(cI, ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE);
    assertNotEquals(reference, manipulatedRef);
  }

  @Test
  void shouldManipulateAllSupportingInformationWhileDelete() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = "123456789";
    setReference(cI, ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE, reference);
    val action = supportingReferenceManipulator().get(0).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE);
    assertNotEquals(reference, manipulatedRef);
  }

  @Test
  void shouldManipulateAllSupportingInformationWhileCutFirstQuarter() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = "123456789";
    setReference(cI, ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE, reference);
    val action = supportingReferenceManipulator().get(1).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef =
        cI.getSupportingInformation().stream()
            .filter(si -> ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE.matches(si.getDisplay()))
            .findFirst()
            .get()
            .getReference();

    assertNotEquals(reference, manipulatedRef);
  }

  @Test
  void shouldManipulateSUPPORTING_RECEIPT_REFWhileCutLastQuarterOfAllSupportingInformation() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = "123456789";
    setReference(cI, ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE, reference);
    val action = supportingReferenceManipulator().get(2).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE);
    assertNotEquals(reference, manipulatedRef);
  }

  @Test
  void shouldManipulateSUPPORTING_RECEIPT_REFToNull() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = "123456789";
    setReference(cI, ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE, reference);

    val action = supportingReferenceManipulator().get(4).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE);
    assertNull(manipulatedRef);
  }

  @Test
  void shouldManipulateSUPPORTING_RECEIPT_REFWhileCutFirstQuarter() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = "123456789";
    setReference(cI, ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE, reference);
    val action = supportingReferenceManipulator().get(6).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE);
    assertTrue(reference.length() > manipulatedRef.length());
  }

  @Test
  void shouldManipulateSUPPORTING_RECEIPT_REFWhileCutLastQuarter() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = "123456789";
    setReference(cI, ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE, reference);
    val action = supportingReferenceManipulator().get(7).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, ErpWorkflowStructDef.GEM_ERP_PR_BUNDLE);
    assertTrue(reference.length() > manipulatedRef.length());
  }

  ///////////////////////////////////  KbvBasisStructDef.SUPPORTING_PRESCRIPTION_REF
  // ////////////////////////////////////////////

  @Test
  void shouldManipulateSUPPORTING_PRESCRIPTION_REFToNullWhileSetAllReferencesToNull() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = "123456789";
    setReference(cI, KbvBasisStructDef.SUPPORTING_PRESCRIPTION_REF, reference);
    val action = supportingReferenceManipulator().get(0).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, KbvBasisStructDef.SUPPORTING_PRESCRIPTION_REF);
    assertNotEquals(reference, manipulatedRef);
  }

  @Test
  void shouldManipulateSUPPORTING_PRESCRIPTION_REFByCuttingAllFirstQuarterSupportingInformation() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = getReference(cI, KbvBasisStructDef.SUPPORTING_PRESCRIPTION_REF);
    val action = supportingReferenceManipulator().get(1).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, KbvBasisStructDef.SUPPORTING_PRESCRIPTION_REF);
    assertNotEquals(reference, manipulatedRef);
  }

  @Test
  void shouldManipulateSUPPORTING_PRESCRIPTION_REFByCuttingAllLastQuarterSupportingInformation() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = getReference(cI, KbvBasisStructDef.SUPPORTING_PRESCRIPTION_REF);
    val action = supportingReferenceManipulator().get(2).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, KbvBasisStructDef.SUPPORTING_PRESCRIPTION_REF);
    assertNotEquals(reference, manipulatedRef);
  }

  @Test
  void shouldManipulateSUPPORTING_PRESCRIPTION_REFoNull() {
    val cI = ErxChargeItemFaker.builder().fake();
    val action = supportingReferenceManipulator().get(3).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, KbvBasisStructDef.SUPPORTING_PRESCRIPTION_REF);
    assertNull(manipulatedRef);
  }

  @Test
  void shouldManipulateSUPPORTING_PRESCRIPTION_REFWhileCutFirstQuarter() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = getReference(cI, KbvBasisStructDef.SUPPORTING_PRESCRIPTION_REF);
    val action = supportingReferenceManipulator().get(5).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, KbvBasisStructDef.SUPPORTING_PRESCRIPTION_REF);
    assertTrue(reference.length() > manipulatedRef.length());
  }

  @Test
  void shouldManipulateSUPPORTING_PRESCRIPTION_REFWhileCutLastQuarter() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = getReference(cI, KbvBasisStructDef.SUPPORTING_PRESCRIPTION_REF);
    val action = supportingReferenceManipulator().get(8).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, KbvBasisStructDef.SUPPORTING_PRESCRIPTION_REF);
    assertTrue(reference.length() > manipulatedRef.length());
  }

  ///////////////////////////////////  KbvBasisStructDef.BINARY
  // ////////////////////////////////////////////
  @ParameterizedTest
  @ValueSource(ints = {1, 0, 2})
  void shouldManipulateBinaryWhileCuFirstQuarterOfAllSupportingInformationReferences(
      int useMutator) {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = getReference(cI, KbvBasisStructDef.BINARY);
    val action = supportingReferenceManipulator().get(useMutator).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, KbvBasisStructDef.BINARY);
    assertNotEquals(reference, manipulatedRef);
  }

  @Test
  void shouldManipulateBINARYToNull() {
    val cI = ErxChargeItemFaker.builder().fake();
    val action = supportingReferenceManipulator().get(9).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, KbvBasisStructDef.BINARY);
    assertNull(manipulatedRef);
  }

  @Test
  void shouldManipulateBINARYWhileCutFirstQuarter() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = getReference(cI, KbvBasisStructDef.BINARY);
    val action = supportingReferenceManipulator().get(10).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, KbvBasisStructDef.BINARY);
    assertTrue(reference.length() > manipulatedRef.length());
  }

  @Test
  void shouldManipulateBINARYWhileCutLastQuarter() {
    val cI = ErxChargeItemFaker.builder().fake();
    var reference = getReference(cI, KbvBasisStructDef.BINARY);
    val action = supportingReferenceManipulator().get(11).getParameter();
    assertDoesNotThrow(() -> action.accept(cI));
    var manipulatedRef = getReference(cI, KbvBasisStructDef.BINARY);
    assertTrue(reference.length() > manipulatedRef.length());
  }
}
