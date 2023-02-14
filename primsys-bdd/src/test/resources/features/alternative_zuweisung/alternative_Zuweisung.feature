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

@Funktionalität=AlternZuweisung
@Impl=done
@MainActor=Versicherter
Funktionalität: Alternative Zuweisung
  Der Versicherte soll ohne Anmeldung am Fachdienst sein per DMC in das FdV eingescanntes Rezept an die gewünschte Apotheke schicken können.
  Dabei wird vom E-Rezept-FdV eine Nachricht Referenz auf das einzulösende E-Rezept (Task-ID), die Zugriffsberechtigung (AccessCode) und Kontaktinformationen des Versicherten gebildet.
  Diese verschlüsselt das E-Rezept-FdV hybrid mit dem Verschlüsselungszertifikat (C.HCI.ENC) der SMC-B der Apotheke und versendet den erhaltenen CMS-Datensatz über den Dienstleister an die für die Apotheke beim ApoVZD ermittelte URL.
  Die Apotheke entschlüsselt die Nachricht und löst anschließend das Rezept ein

  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und die Apotheke Am Flughafen verbindet sich mit seinem Apothekendienstleister

  @TCID=ERP_EE_altZuweisung_01
  @Path=happy
  Szenariogrundriss:  E-Rezept per alternative Zuweisung einlösen
  Der Arzt verschreibt der GKV-Versicherten ein E-Rezept für ein apothekenpflichtiges Rezept.
  Der Versicherte weist das E-Rezept seiner Apotheke über den Apothekendienstleister zu.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |

    Und die Versicherte Sina Hüllmann für das letzte E-Rezept die alternative Zuweisung an die Apotheke Am Flughafen mit der Option <Belieferungsoption> auslöst
    Und die Apotheke Am Flughafen eine Nachricht mit einer alternativen Zuweisung vom Dienstleister empfängt und entschlüsselt
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Sina Hüllmann dispensiert
    Dann hat die Apotheke Am Flughafen genau 1 Quittung vorliegen
    Und hat der Versicherte Sina Hüllmann genau 1 Medikament erhalten

    Beispiele:
      | Belieferungsoption |
      | Reservierung       |
      | Bote               |
      | Versand            |
