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
@Funktionalität:Direktzuweisung
@Workflow:169
@Versicherung:GKV
@AFO-ID:A_18827
@AF-ID:AF_10111
@AF-ID:AF_10112
@AFO-ID:A_18502
@AFO-ID:A_18503
Funktionalität: Direktzuweisung für GKV-Rezepte
  Die E-Rezept-Anwendungsfälle für Direktzuweisung für GKV-Versicherte (Workflow 169)

  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B


  @TCID:ERP_EE_WF169_01
  @TESTFALL:positiv
  @AF-ID:AF_10114
  @AFO-ID:A_18822
  @AFO-ID:A_21360
  @Hauptdarsteller:Versicherter
  Szenario: Anzeigen von direkt zugewiesenen E-Rezepten
  Der Arzt verschreibt der GKV-Versicherten ein Medikament als Direktzuweisung.
  Dieses E-Rezept soll im FdV angezeigt werden.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist:
      | Name        |
      | Zytostatika |
    Dann wird der Versicherten Sina Hüllmann das neue E-Rezept ohne AccessCode angezeigt


  @TCID:ERP_EE_WF169_02
  @TESTFALL:positiv
  @Hauptdarsteller:Arztpraxis
  Szenario: Direktzuweisung an eine Apotheke durch den Arzt
  Der Arzt verschreibt der GKV-Versicherten ein Medikament als Direktzuweisung und übermittelt es der Apotheke.
  Die Apotheke kann es erfolgreich dispensieren.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist:
      | Name        |
      | Zytostatika |
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensiert
    Dann hat die Apotheke genau eine Quittung vorliegen


  @TCID:ERP_EE_WF169_03
  @TESTFALL:negativ
  @AFO-ID:A_22102
  @Hauptdarsteller:Versicherter
  Szenario: Löschen nicht möglich bis nach der Dispensierung
  Der Versicherte kann E-Rezepte mit Direktzuweisung nicht löschen bis diese durch
  eine Apotheke dispensiert wurden

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist:
      | Name        |
      | Zytostatika |
    Dann wird der Versicherten Sina Hüllmann das neue E-Rezept angezeigt
    Und die Versicherte kann ihr letztes E-Rezept nicht löschen, weil sie nicht das Recht dazu hat


  @TCID:ERP_EE_WF169_04
  @TESTFALL:negativ
  @AFO-ID:A_22102
  @Hauptdarsteller:Versicherter
  Szenario: Löschen nicht möglich während der Dispensierung
  Der Versicherte kann das E-Rezept nicht löschen während die Apotheke das Rezept dispensiert

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist:
      | Name        |
      | Zytostatika |
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann wird der Versicherten Sina Hüllmann das neue E-Rezept angezeigt
    Und die Versicherte kann ihr letztes E-Rezept nicht löschen, weil sie nicht das Recht dazu hat


  @TCID:ERP_EE_WF169_05
  @TESTFALL:positiv
  @AFO-ID:A_22102
  @Hauptdarsteller:Versicherter
  Szenario: Löschen möglich erst nach der Dispensierung
  Der Versicherte kann das E-Rezept erst löschen nachdem die Apotheke das Rezept dispensiert hat

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist:
      | Name        |
      | Zytostatika |
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensiert
    Und die Versicherte Sina Hüllmann ihr letztes E-Rezept löscht
    Dann wird das letzte gelöschte E-Rezept der Versicherten nicht mehr angezeigt
