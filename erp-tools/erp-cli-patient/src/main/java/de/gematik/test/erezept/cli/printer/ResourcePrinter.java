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

package de.gematik.test.erezept.cli.printer;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvHealthAppRequest;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import java.io.PrintStream;
import java.util.Optional;
import lombok.val;

public class ResourcePrinter {

  private final PrintStream ps;

  @SuppressWarnings("java:S106") // by default print to System.out
  public ResourcePrinter() {
    this(System.out);
  }

  public ResourcePrinter(PrintStream ps) {
    this.ps = ps;
  }

  public void print(ErxMedicationDispenseBundle mdBundle) {
    ps.println("\nMedicationDispense(s):");
    mdBundle
        .getMedicationDispenses()
        .forEach(
            md -> {
              ps.println(
                  format(
                      "Abgabe durch: {0} am {1}",
                      md.getPerformerIdFirstRep(), md.getZonedWhenHandedOver()));
              if (md.isDiGA()) {
                printDiGA(md);
              } else {
                val pairs = mdBundle.getDispensePairBy(md.getPrescriptionId());
                pairs.forEach(p -> print(p.getRight()));
              }
            });
  }

  public void print(KbvHealthAppRequest healthApp) {
    ps.println(format("DiGA: {0}", healthApp.getName()));
    ps.println(format("\tPZN: {0}", healthApp.getPzn().getValue()));
    ps.println(format("\tSER: {0}", healthApp.relatesToSocialCompensationLaw()));
    ps.println(format("\tAccident: {0}", healthApp.hasAccidentExtension()));
  }

  public void print(KbvErpMedication medication) {
    ps.println(
        format(
            "Kategorie: {0} / {1}",
            medication.getCategoryFirstRep().getCode(),
            medication.getCategoryFirstRep().getDisplay()));

    ps.println(format("\t{0}", medication.getDescription()));
    medication.getIngredientText().ifPresent(text -> ps.println(format("\tIngredient: {0}", text)));
    medication
        .getIngredientStrengthString()
        .ifPresent(text -> ps.println(format("\tIngredient strength: {0}", text)));
    medication
        .getDarreichungsform()
        .ifPresent(
            darreichungsform ->
                ps.println(format("\tDarreichungsform: {0}", darreichungsform.getDisplay())));
    ps.println(format("\tImpfung: {0}", medication.isVaccine()));
  }

  public void print(GemErpMedication medication) {
    val name = medication.getNameFromCodeOreContainedRessource().orElse("n/a");
    val pzn = medication.getPzn().map(PZN::getValue).orElse("n/a");
    val df = medication.getDarreichungsform().map(Darreichungsform::getCode).orElse("n/a");
    ps.println(format("\tMedikament: {0} {1} {2}", name, pzn, df));
  }

  public void printDiGA(ErxMedicationDispense mdDiga) {
    val medicationRef = mdDiga.getMedicationReference();
    val name = medicationRef.getDisplay();
    val pzn = medicationRef.getIdentifier().getValue();

    // TODO: no getters yet
    val redeemCode =
        Optional.ofNullable(
                mdDiga.getExtensionByUrl(ErpWorkflowStructDef.REDEEM_CODE.getCanonicalUrl()))
            .map(ext -> ext.getValue().castToString(ext.getValue()).getValue())
            .orElse("n/a");
    val deepLink =
        Optional.ofNullable(
                mdDiga.getExtensionByUrl(ErpWorkflowStructDef.DEEP_LINK.getCanonicalUrl()))
            .map(ext -> ext.getValue().castToString(ext.getValue()).getValue())
            .orElse("n/a");

    ps.println(format("DIGA: {0} ({1})", name, pzn));
    ps.println(format("\tRedeemCode: {0}", redeemCode));
    ps.println(format("\tDeeplink: {0}", deepLink));
  }
}
