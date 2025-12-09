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
@Funktionalität:Dispensierung
Funktionalität: E-Rezepte dispensieren
  Dispensierte E-Rezepte im Rezeptarchiv ablegen

  Grundlage: Rezept wird erzeugt, geladen und ist auf dem Mainscreen
    Angenommen die Ärztin Adelheid Ulmenwald hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B

  @TCID:ERP_FDV_GKV_STANDARDDISPENSIERUNG_01
  @Funktionalität:Standarddispensierung
  Szenario: Dispensierung des E-Rezeptes mit dem verschriebenen Medikament
    Angenommen die GKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet
    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice ein apothekenpflichtiges Medikament verschreibt
    Wenn die Versicherte Alice ihr letztes ausgestelltes E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Alice dispensiert
    Dann hat die Apotheke Am Flughafen genau 1 Quittung für Alice vorliegen
    Dann hat die Versicherte Alice das letzte E-Rezept elektronisch erhalten

  @TCID:ERP_FDV_GKV_MEHRFACHDISPENSIERUNG_01
  @Funktionalität:Mehrfachdispensierung
  Szenario: Dispensierung des E-Rezeptes mit mehreren Medikamenten
  Die Apotheke dispensiert das akzeptierte E-Rezept über mehrere Ersatzpräparate.
  In diesem Szenario verschreibt der Arzt eine große Packung (N3) mit 50 Stk Inhalt.
  Die Apotheke dispensiert hingegen eine kleine (N1) Packung mit 10 Stk und zwei mittlere (N2) mit 20 Stk Inhalt.
  Die Dispensierung der drei Packungen erzeugt nur eine Quittung.

    Angenommen die GKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet
    Wenn die Ärztin Adelheid Ulmenwald folgendes E-Rezept an die Versicherte Alice verschreibt:
      | Name         | PZN      | Kategorie | Substitution | Normgröße | Darreichungsform | Dosierung | Menge |
      | IBUFLAM akut | 11648419 | 00        | true         | N3        | FTA              | 1-0-0-1   | 50    |
    Wenn die Versicherte Alice ihr letztes ausgestelltes E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke das letzte akzeptierte E-Rezept mit den folgenden Medikamenten korrekt an Alice dispensiert:
      | Name         | PZN      | Kategorie | Normgröße | Menge | Einheit | Darreichungsform |
      | IBUFLAM akut | 04100230 | 00        | N1        | 10    | Stk     | FTA              |
      | IBUFLAM akut | 04100218 | 00        | N2        | 20    | Stk     | FTA              |
      | IBUFLAM akut | 04100218 | 00        | N2        | 20    | Stk     | FTA              |
    Dann hat die Apotheke Am Flughafen genau 1 Quittung für Alice vorliegen
    Dann hat die Versicherte Alice das letzte E-Rezept elektronisch erhalten