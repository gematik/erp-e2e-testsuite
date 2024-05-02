#
# Copyright 2023 gematik GmbH
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# language: de

@PRODUKT:eRp_FD
@Funktionalität:EGKinApotheke
@Workflow:160
@Versicherung:GKV
Funktionalität: eGK in der Apotheke mit VSDM++
  Der Versicherte soll durch die Vorlage seiner EGK eine Apotheke dazu berechtigen,
  seine einlösbaren E-Rezepte aus dem E-Rezept-Fachdienst mit Hilfe des VSDM Prüfungsnachweis abrufen zu können.
  Aus technischer Sicht ist es dabei unerheblich, ob es sich um einen GKV- oder PKV-Versicherten handelt.


  @TCID:ERP_EE_EGK_APOTHEKE_VSDM_01
  @TESTFALL:positiv
  @AFO-ID:A_23450
  @AFO-ID:A_23454
  @Hauptdarsteller:Apotheke
  Szenariogrundriss: In der Apotheke ein E-Rezept mit eGK abrufen und einlösen

    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die GKV Versicherte <Patient> hat eine eGK für die Abholung in der Apotheke
    Und die Ärztin Dr. Schraßer dem Versicherten <Patient> folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Angenommen die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Wenn die Apotheke Am Flughafen die E-Rezepte mit der eGK von <Patient> abruft
    Und die Apotheke Am Flughafen das letzte abgerufene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an <Patient> dispensiert
    Dann hat die Apotheke Am Flughafen genau 1 Quittung vorliegen

    Beispiele:

      | Patient                      |
      | Sofia 'Kubus' Herrmann       |
      | Fabian 'GKVI' Krause         |
      | Juliane 'TK' Sommer          |
      | Gerda 'kkh' Galle            |
      | Thomas 'Bitmarck' Gottschalk |
      | Martha 'ITS Care' Koch       |
      | Maria 'Mobil ISC' Neumann    |
      | Julian 'Arge AOK' Nagelsmann |
      | Lucius 'ITSC' Fox            |



