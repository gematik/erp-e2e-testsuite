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

package de.gematik.test.erezept.lei.steps;

import static java.text.MessageFormat.format;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static net.serenitybdd.screenplay.GivenWhenThen.then;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.screenplay.questions.ResponseOfAbortOperation;
import de.gematik.test.erezept.screenplay.task.AbortPrescription;
import de.gematik.test.erezept.screenplay.task.CheckTheReturnCode;
import de.gematik.test.erezept.screenplay.task.IssuePrescription;
import de.gematik.test.erezept.screenplay.task.Negate;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Dann;
import io.cucumber.java.de.Wenn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.core.PendingStepException;
import net.serenitybdd.screenplay.actors.OnStage;

/** Testschritte die aus der Perspektive eines Arztes bzw. einer Ärztin ausgeführt werden */
@Slf4j
public class DoctorSteps {

  @Angenommen(
      "^(?:der Arzt|die Ärztin) (.+) verschreibt folgende(?:s)? E-Rezept(?:e)? an (?:den|die) Versicherte(?:n)? (.+):$")
  public void givenIssueMultiplePrescriptionsToActor(
      String docName, String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    givenThat(theDoctor)
        .attemptsTo(IssuePrescription.forPatient(thePatient).from(medications.asMaps()));
  }

  @Angenommen(
      "^(?:der Arzt|die Ärztin) verschreibt folgende(?:s)? E-Rezept(?:e)? an (?:den|die) Versicherte(?:n)? (.+):$")
  public void givenIssueMultiplePrescriptionsToActor(String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);

    givenThat(theDoctor)
        .attemptsTo(IssuePrescription.forPatient(thePatient).from(medications.asMaps()));
  }

  @Angenommen(
      "^(?:der Arzt|die Ärztin) (.+) verschreibt ein E-Rezept an (?:den|die) Versicherte(?:n)? (.+)$")
  public void givenIssueSingleRandomPrescriptionsToActor(String docName, String patientName) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    givenThat(theDoctor).attemptsTo(IssuePrescription.forPatient(thePatient).randomPrescription());
  }

  @Angenommen(
      "^(?:der Arzt|die Ärztin) verschreibt ein E-Rezept an (?:den|die) Versicherte(?:n)? (.+)$")
  public void givenIssueSingleRandomPrescriptionsToActor(String patientName) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);

    givenThat(theDoctor).attemptsTo(IssuePrescription.forPatient(thePatient).randomPrescription());
  }

  @Angenommen(
      "^(?:der Arzt|die Ärztin) (.+) verschreibt folgende(?:s)? E-Rezept(?:e)? an die KVNR (\\w+):$")
  public void givenIssueMultiplePrescriptionsToKvnr(
      String docName, String kvnr, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);

    givenThat(theDoctor).attemptsTo(IssuePrescription.forKvnr(kvnr).from(medications.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) folgende(?:s)? E-Rezept(?:e)? an (?:den|die) Versicherte(?:n)? (.+) verschreibt:$")
  public void whenIssueMultiplePrescriptionsToActor(
      String docName, String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .from(medications.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) folgende(?:s)? E-Rezept(?:e)? an (?:den|die) Versicherte(?:n)? (.+) verschreibt:$")
  public void whenIssueMultiplePrescriptionsToActor(String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .from(medications.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) ein E-Rezept an (?:den|die) Versicherte(?:n)? (.+) verschreibt$")
  public void whenIssueSingleRandomPrescriptionsToActor(String docName, String patientName) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .randomPrescription());
  }

  @Wenn("^(?:der Arzt|die Ärztin) ein E-Rezept an (?:den|die) Versicherte(?:n)? (.+) verschreibt$")
  public void whenIssueMultiplePrescriptionsToActor(String patientName) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .randomPrescription());
  }

  /**
   * TMD-1598 Variante Apothekenpflichtiges Medikament
   *
   * @param docName ist der Name des ausstellenden Arztes
   * @param patientName ist der Name des empfangenden Patienten
   * @param medications ist die Liste der Medikationen die der Arzt dem Patienten ausstellt
   */
  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) folgende(?:s)? apothekenpflichtige(?:s)? Medikament(?:e)? verschreibt:$")
  public void whenIssuePharmacyOnlyPrescriptionToActor(
      String docName, String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .from(medications.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (?:dem|der) Versicherten (.+) folgende(?:s)? apothekenpflichtige(?:s)? Medikament(?:e)? verschreibt:$")
  public void whenIssuePharmacyOnlyPrescriptionToActor(String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .from(medications.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) ein apothekenpflichtiges Medikament verschreibt$")
  public void whenIssueSingleRandomPharmacyOnlyPrescriptionToActor(
      String docName, String patientName) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .randomPrescription());
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (?:dem|der) Versicherten (.+) ein apothekenpflichtiges Medikament verschreibt$")
  public void whenIssuePharmacyOnlyPrescriptionToActor(String patientName) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .randomPrescription());
  }

  /**
   * TMD-1598 Variante Direktzuweisung
   *
   * @param docName ist der Name des ausstellenden Arztes
   * @param patientName ist der Name des empfangenden Patienten
   * @param medications ist die Liste der Medikationen die der Arzt dem Patienten ausstellt
   */
  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) folgende(?:s)? Medikament(?:e)? verschreibt und der Apotheke (.+) direkt zuweist:$")
  public void whenIssueDirectAssignmentPrescriptionToActor(
      String docName, String patientName, String pharmacyName, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
                .to(thePharmacy)
                .from(medications.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (?:dem|der) Versicherten (.+) folgende(?:s)? Medikament(?:e)? verschreibt und der Apotheke (.+) direkt zuweist:$")
  public void whenIssueDirectAssignmentPrescriptionToActor(
      String patientName, String pharmacyName, DataTable medications) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
                .to(thePharmacy)
                .from(medications.asMaps()));
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) ein Medikament verschreibt und der Apotheke (.+) direkt zuweist$")
  public void whenIssueSingleRandomDirectAssignmentPrescriptionToActor(
      String docName, String patientName, String pharmacyName) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
                .to(thePharmacy)
                .randomPrescription());
  }

  @Wenn(
      "^(?:der Arzt|die Ärztin) (?:dem|der) Versicherten (.+) ein Medikament verschreibt und der Apotheke (.+) direkt zuweist$")
  public void whenIssueSingleRandomDirectAssignmentPrescriptionToActor(
      String patientName, String pharmacyName) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forPatient(thePatient)
                .as(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
                .to(thePharmacy)
                .randomPrescription());
  }

  @Dann(
      "^darf (?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) das folgende E-Rezept nicht verschreiben und der Apotheke (.+) direkt zuweisen:$")
  public void thenIssueDirectAssignmentPrescriptionToActorNotAllowed(
      String docName, String patientName, String pharmacyName, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);
    val thePharmacy = OnStage.theActorCalled(pharmacyName);

    then(theDoctor)
        .attemptsTo(
            Negate.the(
                    IssuePrescription.forPatient(thePatient)
                        .as(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
                        .to(thePharmacy)
                        .from(medications.asMaps()))
                .with(UnexpectedResponseResourceError.class));
  }

  /**
   * Ausstellen eines E-Rezeptes an einen Versicherten mittels dessen KVNR. Das E-Rezept kann über
   * die DataTable parametrisiert werden. Dabei sind grundsätzliche sämtliche Spalten optional und
   * werden automatisch zum zufälligen Werten aufgefüllt, wenn diese nicht angegeben werden.
   *
   * <p>Die nachfolgende Tabelle beschreibt die möglichen Parameter die in der DataTable genutzt
   * werden können:
   *
   * <table>
   *     <tr>
   *         <th>Parameter</th>
   *         <th>Typ</th>
   *         <th>Beschreibung</th>
   *         <th>Beispiel</th>
   *     </tr>
   *     <tr>
   *         <td>KBV_Statuskennzeichen</td>
   *         <td>Freitext*</td>
   *         <td>Der Code des <a href="https://applications.kbv.de/S_KBV_STATUSKENNZEICHEN_V1.01.xhtml">KBV Statuskennzeichens</a></td>
   *         <td>00</td>
   *     </tr>
   *     <tr>
   *         <td>PZN</td>
   *         <td>Freitext*</td>
   *         <td>Die <a href="https://de.wikipedia.org/wiki/Pharmazentralnummer">Pharmazentralnummer</a> ist ein in Deutschland bundeseinheitlicher Identifikationsschlüssel</td>
   *         <td>04100218</td>
   *     </tr>
   *     <tr>
   *         <td>Name</td>
   *         <td>Freitext</td>
   *         <td>Der Name des Medikamentes</td>
   *         <td>IBUFLAM akut</td>
   *     </tr>
   *     <tr>
   *         <td>Substitution</td>
   *         <td>Boolean</td>
   *         <td>Entspricht dem <a href="https://www.kbv.de/html/2945.php">aut idem</a> auf einem gewöhnlichen Rezept</td>
   *         <td>true</td>
   *     </tr>
   *     <tr>
   *         <td>Verordnungskategorie</td>
   *         <td>Freitext*</td>
   *         <td>Die Kategorie der Verordnung z.B. 01 für BtM</td>
   *         <td>00</td>
   *     </tr>
   *     <tr>
   *         <td>Impfung</td>
   *         <td>Boolean</td>
   *         <td>Gibt an, ob es sich bei diesem E-Rezept um eine Impfung handelt</td>
   *         <td>false</td>
   *     </tr>
   *     <tr>
   *         <td>Normgröße</td>
   *         <td>Freitext*</td>
   *         <td>Gibt die Normgröße der Verpackung an</td>
   *         <td>N1</td>
   *     </tr>
   *     <tr>
   *         <td>Darreichungsform</td>
   *         <td>Freitext*</td>
   *         <td>Die Darreichungsform als Code aus der <a href="https://applications.kbv.de/S_KBV_DARREICHUNGSFORM_V1.08.xhtml">Schlüsseltabelle der KBV</a></td>
   *         <td>TAB</td>
   *     </tr>
   *     <tr>
   *         <td>Darreichungsmenge</td>
   *         <td>Integer</td>
   *         <td>Darreichungsmenge</td>
   *         <td>1</td>
   *     </tr>
   *     <tr>
   *         <td>Dosierung</td>
   *         <td>Freitext</td>
   *         <td>Anweisung zur Dosierung des Medikamentes</td>
   *         <td>1-0-0-0</td>
   *     </tr>
   *     <tr>
   *         <td>Menge</td>
   *         <td>Integer</td>
   *         <td>Die Menge an Medikamenten die ausgegeben werden soll</td>
   *         <td>4</td>
   *     </tr>
   *     <tr>
   *         <td>MVO</td>
   *         <td>Boolean</td>
   *         <td>Gibt an, ob es sich um eine Mehrfachverordnung handelt</td>
   *         <td>false</td>
   *     </tr>
   *     <tr>
   *         <td>Denominator</td>
   *         <td>Integer</td>
   *         <td>Der Denominator der MVO (wird nur ausgelesen, wenn MVO=true)</td>
   *         <td>4</td>
   *     </tr>
   *     <tr>
   *         <td>Numerator</td>
   *         <td>Integer</td>
   *         <td>Der Numerator der MVO (wird nur ausgelesen, wenn MVO=true)</td>
   *         <td>1</td>
   *     </tr>
   *     <tr>
   *         <td>Gueltigkeitsstart</td>
   *         <td>Integer</td>
   *         <td>Ab wann (in Tagen gerechnet ab dem jetzigen Datum) ist diese MVO Verordnung gültig (wird nur ausgelesen, wenn MVO=true).
   *         Wird <i>leer</i> angegeben, dann wird dieses Feld nicht angegeben</td>
   *         <td>leer</td>
   *     </tr>
   *     <tr>
   *         <td>Gueltigkeitsende</td>
   *         <td>Integer</td>
   *         <td>Wie lange (in Tagen gerechnet ab dem jetzigen Datum) ist diese MVO Verordnung gültig (wird nur ausgelesen, wenn MVO=true).
   *         Wird <i>leer</i> angegeben, dann wird dieses Feld nicht angegeben</td>
   *         <td>leer</td>
   *     </tr>
   *     <tr>
   *         <td>Notdiensgebühr</td>
   *         <td>Boolean</td>
   *         <td>Gibt an, ob für das Medikament eine Notdiensgebühr anfällt</td>
   *         <td>false</td>
   *     </tr>
   *     <tr>
   *         <td>Zahlungsstatus</td>
   *         <td>Integer [0..2]</td>
   *         <td>Zahlungsstatus</td>
   *         <td>0</td>
   *     </tr>
   * </table>
   *
   * <b>*Note:</b> Wird als Freitext übergeben, hat allerdings eine Struktur die eingehalten werden
   * muss!
   *
   * @param docName ist der Name des verschreibenden Arztes
   * @param kvnr ist die Krankenversichertennummer des Versicherten für den das E-Rezept ausgestellt
   *     wird
   * @param medications ist die DataTable für die Parametrisierung des E-Rezeptes
   */
  @Wenn(
      "^(?:der Arzt|die Ärztin) (.*) folgende(?:s)? E-Rezept(?:e)? an die KVNR (\\w+) verschreibt:$")
  public void whenIssueMultiplePrescriptionsToKvnr(
      String docName, String kvnr, DataTable medications) {
    log.trace(format("Doctor {0} will issue Prescription to Patient with KVNR {1}", docName, kvnr));
    val theDoctor = OnStage.theActorCalled(docName);

    when(theDoctor).attemptsTo(IssuePrescription.forKvnr(kvnr).from(medications.asMaps()));
    log.trace(format("Doctor {0} issued Prescription", docName));
  }

  @Wenn("^(?:der Arzt|die Ärztin) (.+) ein E-Rezept an die KVNR (\\w+) verschreibt$")
  public void whenIssueSingleRandomPrescriptionsToKvnr(String docName, String kvnr) {
    val theDoctor = OnStage.theActorCalled(docName);

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forKvnr(kvnr)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .randomPrescription());
  }

  /**
   * Dies ist eine temporäre Vorgabe, gemäß A_22222 bei der sonstige Kostenträger vom E-Rezept
   * ausgeschlossen sind
   *
   * @param docName ist der Name des verschreibenden Arztes
   * @param medications ist die Liste der Informationen zu den einzelnen Verschreibungen
   */
  @Dann(
      "^darf (?:der Arzt|die Ärztin) (.+) (?:diesem|dieser) Versicherten das folgende E-Rezept nicht ausstellen:$")
  public void thenIsNotAllowedToPrescribe(String docName, DataTable medications) {
    val thePatient = OnStage.theActorInTheSpotlight();
    val theDoctor = OnStage.theActorCalled(docName);

    then(theDoctor)
        .attemptsTo(
            Negate.the(IssuePrescription.forPatient(thePatient).from(medications.asMaps()))
                .with(UnexpectedResponseResourceError.class));
  }

  @Dann(
      "^darf (?:der Arzt|die Ärztin) (.+) (?:der|dem) Versicherten (.+) kein E-Rezept ausstellen$")
  public void thenIsNotAllowedToPrescribe(String docName, String patientName) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theDoctor = OnStage.theActorCalled(docName);

    then(theDoctor)
        .attemptsTo(
            Negate.the(IssuePrescription.forPatient(thePatient).randomPrescription())
                .with(UnexpectedResponseResourceError.class));
  }

  /**
   * Dies ist eine temporäre Vorgabe gemäß A_22222, bei der sonstige Kostenträger vom E-Rezept
   * ausgeschlossen sind
   *
   * @param docName ist der Name des verschreibenden Arztes
   * @param patientName ist der Name des Patienten, an den die Verschreibung ausgestellt wird
   * @param medications ist die Liste der Informationen zu den einzelnen Verschreibungen
   */
  @Dann(
      "^darf (?:der Arzt|die Ärztin) (.+) (?:dem|der) Versicherten (.+) das folgende E-Rezept nicht ausstellen:$")
  public void thenIsNotAllowedToPrescribe(
      String docName, String patientName, DataTable medications) {
    val thePatient = OnStage.theActorCalled(patientName);
    val theDoctor = OnStage.theActorCalled(docName);

    then(theDoctor)
        .attemptsTo(
            Negate.the(IssuePrescription.forPatient(thePatient).from(medications.asMaps()))
                .with(UnexpectedResponseResourceError.class));
  }

  /**
   * Löschen eines Rezeptes durch den verschreibenden Arzt
   *
   * @param docName ist der Name des verschreibenden Arztes
   * @param order definiert das Rezept auf dem Stack des Arztes
   */
  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept löscht$")
  public void whenDocAbortsIssuedPrescription(String docName, String order) {
    val theDoctor = OnStage.theActorCalled(docName);
    when(theDoctor).attemptsTo(AbortPrescription.asDoctor().fromStack(order));
  }

  @Wenn("^(?:der Arzt|die Ärztin) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept löscht$")
  public void whenDocAbortsIssuedPrescription(String order) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    when(theDoctor).attemptsTo(AbortPrescription.asDoctor().fromStack(order));
  }

  /**
   * Negierung Löschen eines Rezept durch den verschreibenen Arzt
   *
   * @param docName ist der Name des verschreibenden Arztes
   */
  @Dann(
      "^darf (?:der Arzt|die Ärztin) (.+) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept nicht löschen$")
  public void thenIsNotAllowedToAbortPrescription(String docName, String order) {
    val theDoctor = OnStage.theActorCalled(docName);
    then(theDoctor)
        .attemptsTo(
            Negate.the(AbortPrescription.asDoctor().fromStack(order))
                .with(
                    UnexpectedResponseResourceError
                        .class)); // this won't fit, need to expect something from Ensure!
  }

  @Dann(
      "^darf (?:der Arzt|die Ärztin) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept nicht löschen$")
  public void thenIsNotAllowedToAbortPrescription(String order) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    then(theDoctor)
        .attemptsTo(
            Negate.the(AbortPrescription.asDoctor().fromStack(order))
                .with(
                    UnexpectedResponseResourceError
                        .class)); // this won't fit, need to expect something from Ensure!
  }

  @Dann(
      "^kann (?:der Arzt|die Ärztin) (.+) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept nicht löschen, weil (?:er|sie) nicht das Recht dazu hat$")
  public void thenHasNoRightToAbortPrescription(String docName, String order) {
    val theDoctor = OnStage.theActorCalled(docName);
    then(theDoctor)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asDoctor().fromStack(order))
                .isEqualTo(403));
  }

  @Dann(
      "^kann (?:der Arzt|die Ärztin) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept nicht löschen, weil (?:er|sie) nicht das Recht dazu hat$")
  public void thenHasNoRightToAbortPrescription(String order) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    then(theDoctor)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asDoctor().fromStack(order))
                .isEqualTo(403));
  }

  @Dann(
      "^kann (?:der Arzt|die Ärztin) (.+) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept nicht löschen, weil es einen Konflikt gibt$")
  public void thenConflictToAbortPrescription(String docName, String order) {
    val theDoctor = OnStage.theActorCalled(docName);
    then(theDoctor)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asDoctor().fromStack(order))
                .isEqualTo(409));
  }

  @Dann(
      "^kann (?:der Arzt|die Ärztin) das (letzte|erste) von (?:ihm|ihr) eingestellte E-Rezept nicht löschen, weil es einen Konflikt gibt$")
  public void thenConflictToAbortPrescription(String order) {
    val theDoctor = OnStage.theActorInTheSpotlight();
    then(theDoctor)
        .attemptsTo(
            CheckTheReturnCode.of(ResponseOfAbortOperation.asDoctor().fromStack(order))
                .isEqualTo(409));
  }

  /**
   * TMD-1626 Teststep um den Fehlerfall "Privatrezept an GKV-Versicherten abbilden zu können
   *
   * @param docName ist der Name des verschreibenden Arztes
   * @param insurantName ist der Name eines Versicherten
   */
  @Wenn(
      "^(?:der Arzt|die Ärztin) (.+) (?:dem|der) GKV-Versicherten (.+) fälschlicherweise ein PKV-Rezept verschreibt$")
  public void derArztADerGKVVersichertenAFaeschlicherweiseEinPKVRezeptVerschreibt(
      String docName, String insurantName) {
    throw new PendingStepException("Not yet implemented");
  }

  @Dann(
      "^kann (?:der Arzt|die Ärztin) (.+) (?:der|dem) Versicherten (.+) kein E-Rezept verschreiben, weil die PZN eine falsche Länge hat$")
  public void thenPznIsNotAllowed(String docName, String patientName, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);
    val thePatient = OnStage.theActorCalled(patientName);

    then(theDoctor)
        .attemptsTo(
            Negate.the(IssuePrescription.forPatient(thePatient).from(medications.asMaps()))
                .with(UnexpectedResponseResourceError.class));
  }
}
