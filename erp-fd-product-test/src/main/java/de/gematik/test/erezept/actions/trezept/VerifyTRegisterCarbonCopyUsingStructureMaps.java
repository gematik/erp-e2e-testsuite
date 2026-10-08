/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.actions.trezept;

import static de.gematik.test.erezept.fhir.profiles.definitions.GemErpTPrescStructDef.*;
import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.compare.trans.StructureMapsTransformerFactory;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.abilities.UseResourceComparator;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.erp.tprescription.ErpTPrescriptionCarbonCopy;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.trezept.TRegisterLog;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Performable;
import org.hl7.fhir.r4.model.*;

@Slf4j
public class VerifyTRegisterCarbonCopyUsingStructureMaps implements Performable {

  private final List<TRegisterLog> logs;
  private final PrescriptionId prescriptionId;
  private final ErxMedicationDispenseBundle medDispenseBundle;
  private final ErxTask erxTask;
  private final KbvErpBundle kbvErpBundle;
  private final PharmacyActor pharmacy;
  private boolean shouldCheckAuthHeadder = false; // NOSONAR // for Later Merge
  private static final String OPERATION = "operation";
  private static final String TYPE = "type";
  private static final String PATH = "path";
  private static final String ADD = "add";
  private static final String NAME = "name";
  private static final String VALUE = "value";

  private VerifyTRegisterCarbonCopyUsingStructureMaps(
      List<TRegisterLog> logs,
      PrescriptionId prescriptionId,
      @Nullable ErxMedicationDispenseBundle medDispenseBundle,
      boolean checkAuthHeadder,
      ErxTask task,
      KbvErpBundle kbvErpBundle,
      PharmacyActor pharmacy) {
    this.logs = logs;
    this.prescriptionId = prescriptionId;
    this.medDispenseBundle = medDispenseBundle;
    this.shouldCheckAuthHeadder = checkAuthHeadder;
    this.erxTask = task;
    this.kbvErpBundle = kbvErpBundle;
    this.pharmacy = pharmacy;
  }

  public static VerifyTRegisterCarbonCopyUsingStructureMaps from(
      List<TRegisterLog> logs,
      PrescriptionId prescriptionId,
      ErxMedicationDispenseBundle medDisp,
      boolean shouldCheckAuthHeadder,
      ErxTask task,
      KbvErpBundle kbvErpBundle,
      PharmacyActor pharmacy) {

    return new VerifyTRegisterCarbonCopyUsingStructureMaps(
        logs, prescriptionId, medDisp, shouldCheckAuthHeadder, task, kbvErpBundle, pharmacy);
  }

  private static void addTypeCodingIn(Parameters patch, String target, String system, String code) {
    val addIdentifierType = patch.addParameter().setName(OPERATION);

    addIdentifierType.addPart().setName(TYPE).setValue(new CodeType(ADD));
    addIdentifierType.addPart().setName(PATH).setValue(new StringType(target));

    addIdentifierType.addPart().setName(NAME).setValue(new StringType(TYPE));

    val type = new CodeableConcept();
    type.addCoding().setSystem(system).setCode(code);

    addIdentifierType.addPart().setName(VALUE).setValue(type);
  }

  private static void addIdentifierTypeIn(
      Parameters patch, String target, String system, String code) {
    val addIdentifierType = patch.addParameter().setName(OPERATION);
    addIdentifierType.addPart().setName(TYPE).setValue(new CodeType(ADD));
    addIdentifierType.addPart().setName(PATH).setValue(new StringType(target));
    addIdentifierType.addPart().setName(NAME).setValue(new StringType(TYPE));

    val value = addIdentifierType.addPart().setName(VALUE);
    value.addPart().setName("coding").setValue(new Coding().setSystem(system).setCode(code));
  }

  private static void addMetaProfileIn(Parameters patch, String target, WithSystem profileValue) {
    val addDspProfile = patch.addParameter().setName(OPERATION);
    addDspProfile.addPart().setName(TYPE).setValue(new CodeType(ADD));
    addDspProfile.addPart().setName(PATH).setValue(new StringType(target));
    addDspProfile.addPart().setName(NAME).setValue(new StringType("meta"));
    val meta = addDspProfile.addPart().setName(VALUE);
    meta.addPart().setName("profile").setValue(new CanonicalType(profileValue.getCanonicalUrl()));
  }

  private static void addSignatureDate(Parameters patch, String presSignatureDate) {
    var signDatePatcher = patch.addParameter().setName(OPERATION);
    signDatePatcher.addPart().setName(TYPE).setValue(new CodeType(ADD));
    signDatePatcher
        .addPart()
        .setName(PATH)
        .setValue(new StringType("Parameters.parameter.where(name='rxPrescription')"));
    signDatePatcher.addPart().setName(NAME).setValue(new StringType("part"));
    var value = signDatePatcher.addPart().setName(VALUE);
    value.addPart().setName(NAME).setValue(new StringType("prescriptionSignatureDate"));
    value
        .addPart()
        .setName(VALUE)
        .setValue(new InstantType(presSignatureDate).setTimeZone(TimeZone.getTimeZone("UTC")));
  }

  @Override
  @Step(
      "{0} verifiziert die Korrektheit des digitalen E-Rezept-Durchschlags vom Bfarm Mock zur"
          + " Prescription #prescriptionId und den logs #logs ")
  public void performAs(Actor actor) {
    if (logs == null || logs.isEmpty()) {
      throw new AssertionError("No carbon copy found in T-Register");
    }
    val parser = pharmacy.abilityTo(UseTheErpClient.class).getFhir();
    val comparator = actor.abilityTo(UseResourceComparator.class);

    val httpRequest = logs.get(0).request();
    val body = httpRequest.bodyAsString();
    val carbCopyByFD = parser.decode(ErpTPrescriptionCarbonCopy.class, body);
    val presSignatureDate = carbCopyByFD.getPrescriptionSignatureDate();

    val vzdSearchSetString =
        ResourceLoader.readFileFromResource("sMapsSources/example-case-01-VZDSearchSet.xml");
    val vzdSearchSet = parser.decode(Bundle.class, vzdSearchSetString);

    Optional.ofNullable(medDispenseBundle)
        .ifPresent(
            medDispBundle -> {
              val medDispensePair = medDispBundle.getDispensePairBy(prescriptionId).get(0);
              val medDispense = medDispensePair.getLeft();
              val medication = medDispensePair.getRight();

              val fullMedicationId = medication.getId();
              val gemMedicationId = fullMedicationId.split("/")[1];
              medication.setId(gemMedicationId);

              val kbvMedication = kbvErpBundle.getMedication();
              val kbvMedicationId = kbvMedication.getId().split("/")[1];
              kbvMedication.setId(kbvMedicationId);

              val medDispenseParameters =
                  GemOperationInputParameterBuilder.forClosingPharmaceuticals()
                      .with(medDispense, medication)
                      .build();

              val transformer = StructureMapsTransformerFactory.buildFor("T-Rezept");
              val list = List.of(erxTask, kbvErpBundle, medDispenseParameters, vzdSearchSet);

              // add knowing Issues
              Parameters patch = new Parameters();
              // add SignatureDate
              addSignatureDate(patch, presSignatureDate);
              // add MedDsp Meta Profile
              addMetaProfileIn(
                  patch,
                  "Parameters.parameter.where(name='rxDispensation').part.where(name='dispenseInformation').part.where(name='medicationDispense').resource",
                  MEDICATION_DISPENSE);
              // add DispenseOrganization Meta Profile
              addMetaProfileIn(
                  patch,
                  "Parameters.parameter.where(name='rxDispensation').part.where(name='dispenseOrganization').resource",
                  DISPENSE_ORGANIZATION);
              // Add MedRequest MetaProfile
              addMetaProfileIn(
                  patch,
                  "Parameters.parameter.where(name='rxPrescription').part.where(name='medicationRequest').resource",
                  MEDICATION_REQUEST);
              // add Identifier Type in
              addIdentifierTypeIn(
                  patch,
                  "Parameters.parameter.where(name='rxDispensation').part.where(name='dispenseOrganization').resource.identifier",
                  "http://terminology.hl7.org/CodeSystem/v2-0203",
                  "PRN");

              // addOptional type.coding to Identifier
              addTypeCodingIn(
                  patch,
                  "Parameters.parameter.where(name='rxDispensation').part.where(name='dispenseOrganization').resource.identifier",
                  "http://terminology.hl7.org/CodeSystem/v2-0203",
                  "PRN");

              val carbonCopyByStrucMaps = transformer.transform(list, Parameters.class, patch);
              val result = comparator.compare(carbCopyByFD, carbonCopyByStrucMaps);

              if (!result.isEmpty())
                result
                    .getParameter()
                    .forEach(
                        p -> p.getPart().forEach(part -> log.info(part.getValue().toString())));
              log.info(
                  "Carbon Copy Diffs Interpretation"
                      + "These differences show how Carbon Copy received from Fachdienst should be"
                      + " changed to match expected Carbon Copy");

              val diffsString = parser.encode(result, EncodingType.JSON, true);

              assertTrue(
                  result.isEmpty(),
                  format(
                      "Carbon Copy does not match the expected structure. Differences : {0} ",
                      diffsString));
            });
  }
}
