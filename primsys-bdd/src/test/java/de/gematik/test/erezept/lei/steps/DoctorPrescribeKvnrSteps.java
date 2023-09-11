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

import de.gematik.test.erezept.screenplay.task.IssuePrescription;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.de.Angenommen;
import io.cucumber.java.de.Wenn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.actors.OnStage;

import static java.text.MessageFormat.format;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static net.serenitybdd.screenplay.GivenWhenThen.when;

@Slf4j
public class DoctorPrescribeKvnrSteps {

  @Angenommen(
      "^(?:der Arzt|die Ärztin) (.+) verschreibt folgende(?:s)? E-Rezept(?:e)? an die KVNR (\\w+):$")
  public void givenIssueMultiplePrescriptionsToKvnr(
      String docName, String kvnr, DataTable medications) {
    val theDoctor = OnStage.theActorCalled(docName);

    givenThat(theDoctor)
        .attemptsTo(
            IssuePrescription.forKvnr(kvnr)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .from(medications.asMaps()));
  }

  /**
   * Ausstellen eines E-Rezeptes an einen Versicherten mittels dessen KVNR. Das E-Rezept kann über
   * die DataTable parametrisiert werden. Dabei sind grundsätzliche sämtliche Spalten optional und
   * werden automatisch zum zufälligen Werten aufgefüllt, wenn diese nicht angegeben werden.
   *
   * <p>Die nachfolgende Tabelle beschreibt die möglichen Parameter, die in der DataTable genutzt
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

    when(theDoctor)
        .attemptsTo(
            IssuePrescription.forKvnr(kvnr)
                .as(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .from(medications.asMaps()));
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
}
