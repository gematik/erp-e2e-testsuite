#
# Copyright 2026 gematik GmbH
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

@PRODUKT:eRp_FD
Funktionalität: Apothekenpflichtige Verordnung für Versicherte mit dgMP


  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B


  @TCID:ERP_EE_EMP_ID_01
  @TESTFALL:positiv
  @Hauptdarsteller:Arzt
  Szenario: Einstellen eines Apothekenpflichtigen E-Rezeptes mit Verweis auf auf einen Eintrag im Medikationsplan
  Die Apotheke akzeptiert das zugewiesene Rezept.

    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgende apothekenpflichtiges Medikament verschreibt:
      | Name               | PZN      | Normgröße | Menge | Einheit | Darreichungsform | Dosierung          | emp-identifier  (will be red automatically) |
      | RAMIPRIL-1A Pharma | 00766759 | N1        | 100   | Stk     | TAB              | morgens 1 Tablette | session:MedicationPlanIdentifier  (Dummy)   |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestelltes E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren


  @TCID:ERP_EE_D_DGMP_02
  @TESTFALL:positiv
  @Hauptdarsteller:Arzt
  Szenario: Einstellen eines Apothekenpflichtigen E-Rezeptes mit mit menschenlesbaren Dosierungsanweisungen
  Die Apotheke akzeptiert das zugewiesene Rezept.

    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgende apothekenpflichtiges Medikament verschreibt:
      | status  | PZN      | renderedDosageInstruction       | frequency | period | periodUnit | timeOfDay | value | unit  |
      | unknown | 00766713 | täglich: 08:00 Uhr — je 1 Stück | 1         | 1      | d          | 08:00:00  | 1     | Stück |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestelltes E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren


  @TCID:ERP_EE_D_DGMP_03
  @TESTFALL:positiv
  @Hauptdarsteller:Arzt
  Szenario: Einstellen eines Apothekenpflichtigen E-Rezeptes mit mit menschenlesbaren Dosierungsanweisungen
  Die Apotheke akzeptiert das zugewiesene Rezept.

    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgende apothekenpflichtiges Medikament verschreibt:
      | status  | PZN      | renderedDosageInstruction                     | frequency | period | periodUnit | dayOfWeek | when     | value | unit  |
      | unknown | 00766713 | montags 1-0-1-0 Stück; freitags 1-0-1-0 Stück | 4         | 1      | wk         | mon fri   | MORN EVE | 1     | Stück |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestelltes E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren

  @TCID:ERP_EE_D_DGMP_04
  @TESTFALL:positiv
  @Hauptdarsteller:Apotheke
  Szenario: Einstellen eines apothekenpflichtigen E-Rezeptes mit mit menschenlesbaren Dosierungsanweisungen
  Die Apotheke akzeptiert das zugewiesene Rezept.

    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgende apothekenpflichtiges Medikament verschreibt:
      | status  | PZN      | renderedDosageInstruction                     | frequency | period | periodUnit | dayOfWeek | when     | value | unit  |
      | unknown | 00766713 | montags 1-0-1-0 Stück; freitags 1-0-1-0 Stück | 4         | 1      | wk         | mon fri   | MORN EVE | 1     | Stück |

    Und die Versicherte Sina Hüllmann ihr letztes ausgestelltes E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Wenn die Apotheke das letzte akzeptierte E-Rezept mit den folgenden Medikamenten korrekt an Sina Hüllmann dispensiert:
      | Name                                  | PZN      | frequency | period | periodUnit | dayOfWeek | when     | value | unit  |
      | RAMIPRIL-1A Pharma 5 mg Tabletten neu | 00766759 | 4         | 1      | wk         | mon fri   | MORN EVE | 1     | Stück |

