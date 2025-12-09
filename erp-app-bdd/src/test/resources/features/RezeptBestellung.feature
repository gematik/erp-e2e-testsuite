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
# *******
# For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
# *******
#
# language: de

@PRODUKT:eRp_FdV
@iosNightly
@Funktionalität:RezeptBestellung
Funktionalität: E-Rezepte per Nachricht bestellen

  Grundlage: Die Versicherte bestellt ein E-Rezept per Nachricht bei einer Apotheke
    Angenommen die Ärztin Adelheid Ulmenwald hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und die Apotheke Adelheid Ulmendorfer hat Zugriff auf ihre SMC-B
    Und die GKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet

  @TCID:ERP_FDV_BESTELLEN_01
  Szenario: Bestellung eines E-Rezeptes für ein verschriebenes PZN-Medikament

    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice ein apothekenpflichtiges Medikament verschreibt
    Und der Versicherten Alice das letzte E-Rezept in der App angezeigt wird
    #TODO Verifizieren, dass das letzte E-Rezept den Status: "Einlösbar" hat
    Und die Versicherte Alice ihr letztes E-Rezept in der App der Apotheke Adelheid Ulmendorfer per Nachricht zuweist
    Und die Apotheke Adelheid Ulmendorfer die letzte Zuweisung per Nachricht von Alice akzeptiert
    Und die Apotheke Adelheid Ulmendorfer für das letzte akzeptierte E-Rezept von Alice die Dispensierinformationen zeitnah bereitstellt
    Dann kann die Apotheke Adelheid Ulmendorfer für das letzte dispensierte E-Rezept den Workflow abschliessen
    Und hat die Versicherte Alice das letzte E-Rezept elektronisch erhalten
    Und hat die Apotheke Adelheid Ulmendorfer genau 1 Quittung für Alice vorliegen

  @TCID:ERP_FDV_BESTELLEN_02
  Szenario: Bestellung eines E-Rezeptes für ein verschriebenes Wirkstoff-Medikament

    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice folgende apothekenpflichtige Wirkstoff Verordnung verschreibt:
      | Name                |
      | Diazetam-Trichlorid |
    Und die Versicherte Alice ihr letztes E-Rezept in der App der Apotheke Adelheid Ulmendorfer per Nachricht zuweist
    Und die Apotheke Adelheid Ulmendorfer die letzte Zuweisung per Nachricht von Alice akzeptiert
    Und der Versicherten Alice das letzte E-Rezept in der App angezeigt wird
    #TODO Verifizieren, dass das letzte E-Rezept den Status: "Wird bearbeitet" hat
    Und die Apotheke Adelheid Ulmendorfer für das letzte akzeptierte E-Rezept von Alice die Dispensierinformationen zeitnah bereitstellt
    Dann kann die Apotheke Adelheid Ulmendorfer für das letzte dispensierte E-Rezept den Workflow abschliessen
    Und hat die Versicherte Alice das letzte E-Rezept elektronisch erhalten
    Und hat die Apotheke Adelheid Ulmendorfer genau 1 Quittung für Alice vorliegen

  @TCID:ERP_FDV_BESTELLEN_03
  Szenario: Bestellung eines E-Rezeptes für ein verschriebenes Rezeptur-Medikament

    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice folgende apothekenpflichtige Rezeptur Verordnung verschreibt:
      | Name                              |
      | 40mg Hexafluorid & 200g Zahnpasta |
    Und die Versicherte Alice ihr letztes E-Rezept in der App der Apotheke Adelheid Ulmendorfer per Nachricht zuweist
    Und die Apotheke Adelheid Ulmendorfer die letzte Zuweisung per Nachricht von Alice akzeptiert
    Und die Apotheke Adelheid Ulmendorfer für das letzte akzeptierte E-Rezept von Alice die Dispensierinformationen zeitnah bereitstellt
    Und der Versicherten Alice das letzte E-Rezept in der App angezeigt wird
    #TODO Verifizieren, dass das letzte E-Rezept den Status: "Ware steht bereit" hat
    Dann kann die Apotheke Adelheid Ulmendorfer für das letzte dispensierte E-Rezept den Workflow abschliessen
    Und hat die Versicherte Alice das letzte E-Rezept elektronisch erhalten
    Und hat die Apotheke Adelheid Ulmendorfer genau 1 Quittung für Alice vorliegen

  @TCID:ERP_FDV_BESTELLEN_04
  Szenario: Bestellung eines E-Rezeptes für ein verschriebenes Freitext-Medikament

    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice folgende apothekenpflichtige Freitext Verordnung verschreibt:
      | Name                  | Freitext |
      | formloses Medikament  | Patientin benötigt dringend Behandlung von bronchialem Atemwegsinfekt. |
    Und die Versicherte Alice ihr letztes E-Rezept in der App der Apotheke Adelheid Ulmendorfer per Nachricht zuweist
    Und die Apotheke Adelheid Ulmendorfer die letzte Zuweisung per Nachricht von Alice akzeptiert
    Und die Apotheke Adelheid Ulmendorfer für das letzte akzeptierte E-Rezept von Alice die Dispensierinformationen zeitnah bereitstellt
    Dann kann die Apotheke Adelheid Ulmendorfer für das letzte dispensierte E-Rezept den Workflow abschliessen
    Und hat die Versicherte Alice das letzte E-Rezept elektronisch erhalten
    Und hat die Apotheke Adelheid Ulmendorfer genau 1 Quittung für Alice vorliegen

  @TCID:ERP_FDV_BESTELLEN_05
  Szenario: Bestellung eines E-Rezeptes für das mehrere Medikamente dispensiert werden (Mehrfachdispensierung)
  In diesem Szenario verschreibt die Ärztin eine große Packung (N3) mit 50 Stk Inhalt.
  Die Apotheke dispensiert hingegen eine kleine Packung (N1) mit 10 Stk und zwei mittlere Packungen (N2) mit 20 Stk Inhalt.
  Die Dispensierung der drei Packungen erzeugt nur eine Quittung bei der Apotheke.

    Wenn die Ärztin Adelheid Ulmenwald folgendes E-Rezept an die Versicherte Alice verschreibt:
      | Name         | PZN      | Kategorie | Substitution | Normgröße | Darreichungsform | Dosierung | Menge |
      | IBUFLAM akut | 11648419 | 00        | true         | N3        | FTA              | 1-0-0-1   | 50    |
    Und die Versicherte Alice ihr letztes E-Rezept in der App der Apotheke Adelheid Ulmendorfer per Nachricht zuweist
    Und die Apotheke Adelheid Ulmendorfer die letzte Zuweisung per Nachricht von Alice akzeptiert
    Und die Apotheke Adelheid Ulmendorfer das letzte akzeptierte E-Rezept mit den folgenden Medikamenten korrekt an Alice dispensiert:
      | Name         | PZN      | Kategorie | Normgröße | Menge | Einheit | Darreichungsform |
      | IBUFLAM akut | 04100230 | 00        | N1        | 10    | Stk     | FTA              |
      | IBUFLAM akut | 04100218 | 00        | N2        | 20    | Stk     | FTA              |
      | IBUFLAM akut | 04100218 | 00        | N2        | 20    | Stk     | FTA              |
    Dann hat die Versicherte Alice das letzte E-Rezept elektronisch erhalten
    Und hat die Apotheke Adelheid Ulmendorfer genau 1 Quittung für Alice vorliegen

  @TCID:ERP_FDV_BESTELLEN_06
  Szenario: Bestellung eines E-Rezeptes für eine verschriebene Mehrfachverordnung

    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice folgendes apothekenpflichtiges Medikament verschreibt:
      | Name             | MVO  | Denominator | Numerator | Gueltigkeitsstart | Gueltigkeitsende |
      | Nachtstahl FORTE | true | 4           | 1         | 0                 | leer             |
    Und die Versicherte Alice ihr letztes E-Rezept in der App der Apotheke Adelheid Ulmendorfer per Nachricht zuweist
    Und die Apotheke Adelheid Ulmendorfer die letzte Zuweisung per Nachricht von Alice akzeptiert
    Und die Apotheke Adelheid Ulmendorfer für das letzte akzeptierte E-Rezept von Alice die Dispensierinformationen zeitnah bereitstellt
    Dann kann die Apotheke Adelheid Ulmendorfer für das letzte dispensierte E-Rezept den Workflow abschliessen
    Und hat die Versicherte Alice das letzte E-Rezept elektronisch erhalten
    Und hat die Apotheke Adelheid Ulmendorfer genau 1 Quittung für Alice vorliegen