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

package de.gematik.test.erezept.app.parsers;

import static de.gematik.test.erezept.app.parsers.MedicationParser.compareMedicationNames;
import static de.gematik.test.erezept.app.parsers.MedicationParser.getMedicationName;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@Slf4j
class MedicationParserTest extends ErpFhirParsingTest {
  @ParameterizedTest
  @CsvSource(
      value = {
        "NEUPRO 8MG/24H PFT 7"
            + " ST:fhir/valid/kbv/1.1.0/bundle/1f339db0-9e55-4946-9dfa-f1b30953be9b.xml",
        "CERAZETTE 75 Mikrogramm Filmtabletten"
            + " (6X28St):fhir/valid/kbv/1.1.0/bundle/3a1c45f8-d959-43f0-8ac4-9959be746188.xml",
        "Novaminsulfon 500 mg Lichtenstein 100 ml Tropf."
            + " N3:fhir/valid/kbv/1.1.0/bundle/5a3458b0-8364-4682-96e2-b262b2ab16eb.xml",
        "Fucidine® 20mg/g Creme 15g"
            + " N1:fhir/valid/kbv/1.1.0/bundle/5f66314e-459a-41e9-a3d7-65c935a8be2c.xml",
        "Olanzapin Heumann 20mg 70 Schmelztbl."
            + " N3:fhir/valid/kbv/1.1.0/bundle/6d5162b2-abc9-4c0c-9c46-4deb3b64edd7.xml",
        "Nerisona, Asche"
            + " Basis:fhir/valid/kbv/1.1.0/bundle/6f193f8b-da7b-4a77-9105-44050d8839c5.xml",
        "NEUPRO 8MG/24H PFT 7"
            + " ST:fhir/valid/kbv/1.1.0/bundle/6f404894-137e-44e6-abe9-ea2ddc286317.xml",
        "Euthyrox 50 TAB 50"
            + " ST:fhir/valid/kbv/1.1.0/bundle/7d1f08b4-0224-4088-b144-841a75011d18.xml",
        "Remicade 100MG Pulver PIK"
            + " 4ST:fhir/valid/kbv/1.1.0/bundle/7f12495e-709a-47cb-b589-995356926876.xml",
        "Viani 50µg/250µg 1 Diskus 60 ED"
            + " N1:fhir/valid/kbv/1.1.0/bundle/9ad99a8f-6bce-4ab8-ada1-bb3063566c25.xml",
        "Ramipril:fhir/valid/kbv/1.1.0/bundle/9c85a2a5-92ee-4a57-83cb-ba90a0df2a21.xml",
        "Doxycyclin AL 200 T, 10 Tabletten"
            + " N1:fhir/valid/kbv/1.1.0/bundle/14f3cff7-f921-429e-98ca-c65dcb367ba9.xml",
        "L-Thyroxin Henning 75 100 Tbl."
            + " N3:fhir/valid/kbv/1.1.0/bundle/15da065c-5b75-4acf-a2ba-1355de821d6e.xml",
        "Aciclovir 800 - 1 A Pharma® 35 Tbl."
            + " N1:fhir/valid/kbv/1.1.0/bundle/34abcf97-6693-422e-b6f0-0ba9b232843e.xml",
        "clinda-saar 600 mg, 30"
            + " Tabl.:fhir/valid/kbv/1.1.0/bundle/40FC73DB-9343-4FE9-842C-DDF57957B40D.xml",
        "Priorix docpharm:fhir/valid/kbv/1.1.0/bundle/60e48db8-1164-4535-8600-ba83785ac58a.xml",
        "Pramipexol Hexal 1,05 MG RET"
            + " 30ST:fhir/valid/kbv/1.1.0/bundle/91c262e7-390e-4136-8243-7a68a4418d34.xml",
        "Gabapentin,"
            + " Gabapentin:fhir/valid/kbv/1.1.0/bundle/218b581d-ccbe-480e-b8d7-f5f9b925e8c4.xml",
        "Beloc-Zok® mite 47,5 mg, 30 Retardtabletten"
            + " N1:fhir/valid/kbv/1.1.0/bundle/328ad940-3fff-11ed-b878-0242ac120002.xml",
        "ASPIRIN 500MG UEBERZ TABL, 20"
            + " St:fhir/valid/kbv/1.1.0/bundle/414ca393-dde3-4082-9a3b-3752e629e4aa.xml",
        "Prospan® Hustensaft 100ml"
            + " N1:fhir/valid/kbv/1.1.0/bundle/0428d416-149e-48a4-977c-394887b3d85c.xml",
        "Erythromycin, Oleum Rosae, Ungt. Emulsificans"
            + " aquos.:fhir/valid/kbv/1.1.0/bundle/465f1638-1fbe-462e-a68a-e25b7b507c3e.xml",
        "INFECTOCORTIKRUPP® Zäpfchen 100 mg 3 Supp."
            + " N1:fhir/valid/kbv/1.1.0/bundle/690a7f01-058e-492a-b1dc-d6d8c8a30a59.xml",
        "Somatropin:fhir/valid/kbv/1.1.0/bundle/726e6cdd-c93d-418c-aeeb-1cfe60228916.xml",
        "GONAL-f 150 I.E./0,25ml"
            + " Injektionslösung:fhir/valid/kbv/1.1.0/bundle/753fff3b-5373-4f8d-aa22-852792e799d8.xml",
        "Roxythromycin 300 Heumann FTA 7"
            + " ST:fhir/valid/kbv/1.1.0/bundle/846d07e6-4776-4ae1-8749-0b9334314b97.xml",
        "FSME-IMMUN 0,5 ml"
            + " Erwachsene:fhir/valid/kbv/1.1.0/bundle/914b7619-621e-4270-a2b5-1cdf95c87d14.xml",
        "Metformin 850mg Tabletten"
            + " N3:fhir/valid/kbv/1.1.0/bundle/4863d1fb-dc26-4680-bb35-018610d1749d.xml",
        "L Thyrox JOD Hexal 100/100 TAB 50"
            + " ST:fhir/valid/kbv/1.1.0/bundle/5702a4b8-39a3-4183-9c38-99b749881781.xml",
        "L-Thyroxin Henning 75 100 Tbl."
            + " N3:fhir/valid/kbv/1.1.0/bundle/7031f7f3-cf51-4e77-82d7-b9bdb0a5959f.xml",
        "Salicylsäure, 2-propanol 70"
            + " %:fhir/valid/kbv/1.1.0/bundle/9581ce65-b118-4751-9073-19c091b341e0.xml",
        "2-propanol 70 %, Salicylsäure"
            + ":fhir/valid/kbv/1.1.0/bundle/9581ce65-b118-4751-9073-19c091b341e0.xml",
        "Januvia® 50 mg 28 Filmtabletten"
            + " N1:fhir/valid/kbv/1.1.0/bundle/44420ed9-7388-4be5-acc5-9c124fad9f34.xml",
        "Vardenafil:fhir/valid/kbv/1.1.0/bundle/9356774b-81cb-482b-b000-819685088248.xml",
        "Neupro 4MG/24H PFT"
            + " 7ST:fhir/valid/kbv/1.1.0/bundle/50926152-1499-4084-b784-b696d468946b.xml",
        "Aluminiumchlorid-Hexahydrat, Hydroxyethylcellulose 250, Gereinigtes"
            + " Wasser:fhir/valid/kbv/1.1.0/bundle/a409358a-da34-11eb-8d19-0242ac130003.xml",
        "Ramipril 10mg TAB  Ratiopharm 50ST"
            + " TAB:fhir/valid/kbv/1.1.0/bundle/a6949071-45a9-436c-9370-839de9fcf84d.xml",
        "Abasaglar 100 E/ML KWIKPEN PEN"
            + " 10x3ml:fhir/valid/kbv/1.1.0/bundle/acf39375-3a2b-4017-995d-531705fc129d.xml",
        "Venlafaxin - 1 A Pharma® 75mg 100 Tabl."
            + " N3:fhir/valid/kbv/1.1.0/bundle/aea2f4c5-675a-4d76-ab9b-7994c80b64ec.xml",
        "Sprycel 100MG FTA 30"
            + " ST:fhir/valid/kbv/1.1.0/bundle/b942313c-50b2-4fd8-b328-4fefc927223e.xml",
        "Arcoxia 90 mg 50 FTA:fhir/valid/kbv/1.1.0/bundle/baac1cdd-1313-468f-9c9e-bde74acb308e.xml",
        "L-Thyroxin Henning 75 100 Tbl."
            + " N3:fhir/valid/kbv/1.1.0/bundle/c44ddc5b-21f5-4ce3-995d-ab5ca2633154.xml",
        "Sprycel 100MG FTA 30"
            + " ST:fhir/valid/kbv/1.1.0/bundle/c48d0fb5-9d3a-4872-9879-cdd61c39fc1d.xml",
        "Simvastatin:fhir/valid/kbv/1.1.0/bundle/cd6d3a17-8105-4f1e-86ff-48cd5cebf245.xml",
        "Diclofenac-Natrium:fhir/valid/kbv/1.1.0/bundle/cf0d4980-af01-44f4-a38c-d6bf14b82e07.xml",
        "ABILIFY 5MG TAB"
            + " Beragena:fhir/valid/kbv/1.1.0/bundle/d38c02fa-bbe3-444d-acfe-60698041bd1f.xml",
        "TD- Impfstoff:fhir/valid/kbv/1.1.0/bundle/d390d0d3-f3ac-486b-8ecf-99d281e6682c.xml",
        "Simvastatin:fhir/valid/kbv/1.1.0/bundle/d9118e05-6e13-4df6-af50-552401cf9ba2.xml",
        "Etoposid, NaCl 0,9 %:fhir/valid/kbv/1.1.0/bundle/dae573db-54e3-4cb8-880d-0a46bea8aea1.xml",
        "Hydrocortison, Mannit,"
            + " Siliciumdioxid:fhir/valid/kbv/1.1.0/bundle/ddc83056-5dab-4411-ab13-48abfaf9c47d.xml",
        "Nerisona, Asche"
            + " Basis:fhir/valid/kbv/1.1.0/bundle/deda8f97-38b6-4598-b805-3ef4926fc09d.xml",
        "Sortis 10MG FTA 50ST:fhir/valid/kbv/1.1.0/bundle/e9de4852-9336-4fad-9378-3c904b54ced8.xml",
        "Ibuprofen:fhir/valid/kbv/1.1.0/bundle/ec6059d9-72a0-4ba3-8535-6ffcc0c4279b.xml",
        "Losartan STADA® 100mg 98 Filmtbl."
            + " N3:fhir/valid/kbv/1.1.0/bundle/ee9d3b92-7667-4dfe-8331-f768b2eaca3c.xml",
        "L-Thyroxin Henning 75 100 Tbl."
            + " N3:fhir/valid/kbv/1.1.0/bundle/f70585e0-82f9-4d3d-b248-94504ccf6a66.xml",
        "Neupro 4MG/24H PFT"
            + " 7ST:fhir/valid/kbv/1.1.0/bundle/fe9200db-d503-4a1b-bd13-96ab375f9bab.xml",
      },
      delimiter = ':')
  void shouldHaveTheSameMedicationNames(String expectedName, String path) {
    val pznKbvBundle = getDecodedFromPath(KbvErpBundle.class, path);
    val medication = pznKbvBundle.getMedication();

    val medicationName = getMedicationName(medication);

    log.info("Expected medication name: {}", expectedName);
    log.info("Actual medication name: {}", medicationName);
    assertTrue(compareMedicationNames(expectedName, medicationName));
  }

  @ParameterizedTest
  @CsvSource(
      value = {
        "NEUPRO 8MG/24H PFT 7:fhir/valid/kbv/1.1.0/bundle/1f339db0-9e55-4946-9dfa-f1b30953be9b.xml"
      },
      delimiter = ':')
  void shouldNotHaveTheSameMedicationNames(String name, String path) {
    val pznKbvBundle = getDecodedFromPath(KbvErpBundle.class, path);
    val medication = pznKbvBundle.getMedication();

    val medicationName = getMedicationName(medication);

    assertFalse(compareMedicationNames(name, medicationName));
  }
}
