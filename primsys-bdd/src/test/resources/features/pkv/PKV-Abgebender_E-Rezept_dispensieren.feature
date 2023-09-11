#
# Copyright (c) 2023 gematik GmbH
# 
# Licensed under the Apache License, Version 2.0 (the License);
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an 'AS IS' BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# language: de

@PRODUKT:eRp_FD
@Workflow:200
@Versicherung:PKV
@AFO-ID:A_18514
Funktionalität: PKV-Rezepte dispensieren
  Ein Apotheker dispensiert ein E-Rezept für einen PKV-Versicherten

  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und der PKV Versicherte Günther Angermänn hat Zugriff auf seine eGK
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B

  @TCID:ERP_EE_WF200_01
  @TESTFALL:positiv
  @Hauptdarsteller:Apotheke
  Szenario: PKV E-Rezept dispensieren
  Die Ärztin Dr. Straßer verschreibt dem PKV-Versicherten Günther Angermänn ein E-Rezept, welches dieser bei der
  Apotheke "Am Flughafen" über einen QR-Code einlöst. Die Apotheke hat daraufhin eine Quittung für das dispensierte Medikament.

    Wenn die Ärztin Dr. Schraßer dem Versicherten Günther Angermänn folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und der Versicherte Günther Angermänn sein letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Günther Angermänn dispensiert
    Dann hat die Apotheke Am Flughafen genau 1 Quittung vorliegen
    Dann kann die Apotheke Am Flughafen die Signatur der letzten Quittung erfolgreich mit dem Konnektor validieren
    Dann hat die Versicherte Günther Angermänn genau 1 Medikament erhalten
