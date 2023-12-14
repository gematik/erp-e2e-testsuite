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
@Funktionalität:mehrereMedicationDispense
@Versicherung:GKV
Funktionalität: Mehrere Medication Dispenses
  Der E-Rezept-Fachdienst MUSS beim Beenden eines Tasks mittels /Task/<id>/$close auch die Übergabe
  mehrerer MedicationDispense-Objekte in einem validen Standard-FHIR-Bundle im http-Body des Requests ermöglichen
  und die zweite, dritte usw. MedicationDispense für den Abruf unter einer einzelnen ID
  (z.B. MedicationDispense/<prescriptionID> +"suffix") durch den Versicherten speichern.
  Dies erlaubt die Abgabe von mehreren Packungen kleineren Inhalts um die verschriebene Menge des Medikament zu erreichen.


  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B

  @TCID:ERP_EE_MEHRERE_MEDICATION_DISPENSES_01
  @TESTFALL:positiv
  @AFO-ID:A_22069
  @AFO-ID:A_22070
  @Hauptdarsteller:Apotheke
  Szenario: Dispensierung des E-Rezeptes mit unterschiedlichen Normgrößen
  Die Apotheke dispensiert das akzeptierte E-Rezept über mehrere Packungen unterschiedlicher Größe.
  In diesem Szenario verschreibt der Arzt eine große Packung (N3) mit 50 Stk Inhalt.
  Die Apotheke dispensiert hingegen eine kleine (N1) Packung mit 10 Stk und zwei mittlere (N2) mit 20 Stk Inhalt.
  Die Dispensierung der drei Packungen erzeugt nur eine Quittung.

    Wenn die Ärztin Dr. Schraßer folgendes E-Rezept an die Versicherte Sina Hüllmann verschreibt:
      | Name         | PZN      | Substitution | Normgröße | Darreichungsform | Dosierung | Menge |
      | IBUFLAM akut | 11648419 | true         | N3        | FTA              | 1-0-0-1   | 50    |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke das letzte akzeptierte E-Rezept mit den folgenden Medikamenten korrekt an Sina Hüllmann dispensiert:
      | Name         | PZN      | Normgröße | Menge | Einheit | Darreichungsform |
      | IBUFLAM akut | 04100230 | N1        | 10    | Stk     | FTA              |
      | IBUFLAM akut | 04100218 | N2        | 20    | Stk     | FTA              |
      | IBUFLAM akut | 04100218 | N2        | 20    | Stk     | FTA              |
    Dann hat die Apotheke Am Flughafen genau 1 Quittung für Sina Hüllmann vorliegen
    Dann kann die Versicherte Sina Hüllmann 3 Dispensierinformationen für ihr letztes E-Rezept abrufen







