#
# Copyright 2024 gematik GmbH
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

Funktionalität: Apothekenpflichtige Verordnungstypen für Versicherte und Selbstzahler
  Die Erezept-Anwendungsfälle für apothekenpflichtige Medikamente

  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und die Apotheke Stadtapotheke hat Zugriff auf ihre SMC-B

    @TCID:ERP_EE_REZEPTVARIANTEN_01
    @TESTFALL:positiv
    @AFO-ID:A_24034
    @Hauptdarsteller:Arzt
  Szenariogrundriss: Einstellen eines Apothekenpflichtigen E-Rezeptes mit verschiedenen Verordnungstypen und Versicherungsverhältnissen
  Die Apotheke akzeptiert das zugewiesene Rezept in den Variationen Frteitext-, Wirkstoff- und Rezepturverordnung.

    Und die <Versicherungsart> Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgende apothekenpflichtige <Verordnungstyp> Verordnung verschreibt:
      | Name   | Freitext   | PZN   | Menge   | Impfung     |
      | <Name> | <Freitext> | <PZN> | <Menge> | <isVaccine> |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren

    Beispiele:
      | Verordnungstyp | Versicherungsart | Name                             | Freitext                                                                                                            | PZN      | Menge | isVaccine |
      | Freitext       | GKV              | PlaceboCondimenti Luffa-Schwämme | Ein Bisschen Davon und noch mehr davon und das in kreisenden Bewegungen einmassieren, wo ? -> nach eigenem Ermessen |          | 1     | false     |
      | Rezeptur       | GKV              | Anti Aging Chreme für die Achsel | quantum satis                                                                                                       |          | 5     | true      |
      | Wirkstoff      | GKV              | Harnsäure Hemmer                 | Ibuprofen und Paracetamol -> beides und viel und so weiter                                                          | 16588573 | 2     | true      |
      | Freitext       | PKV              | PlaceboCondimenti Luffa-Schwämme | Ein Bisschen Davon und noch mehr davon und das  ganze gut durchrühren                                               |          | 3     | false     |
      | Rezeptur       | PKV              | Anti Aging Chreme für die Achsel | beidseitig auftragen                                                                                                |          | 5     | false     |
      | Wirkstoff      | PKV              | Harnsäure Hemmer                 | Ibuprofen und Paracetamol -> beides und viel und so weiter                                                          | 16588573 | 1     | false     |

  @TCID:ERP_EE_REZEPTVARIANTEN_02
  @TESTFALL:positiv
  @AFO-ID:A_24034
  @Hauptdarsteller:Arzt
  Szenariogrundriss: Einstellen des E-Rezeptes Direktzuweisung mit verschiedenen Verordnungstypen und Versicherungsverhältnissen
  Die Apotheke akzeptiert das zugewiesene Rezept in den Variationen Frteitext-, Wirkstoff- und Rezepturverordnung.

    Und die <Versicherungsart> Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgende apothekenpflichtige <Verordnungstyp> Verordnung verschreibt und der Apotheke Am Flughafen direkt zuweist:
      | Name   | Freitext   | PZN   | Menge   |
      | <Name> | <Freitext> | <PZN> | <Menge> |
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren

    Beispiele:
      | Verordnungstyp | Versicherungsart | Name                             | Freitext                                                                                                            | PZN      | Menge |
      | Freitext       | GKV              | PlaceboCondimenti Luffa-Schwämme | Ein Bisschen Davon und noch mehr davon und das in kreisenden Bewegungen einmassieren, wo ? -> nach eigenem Ermessen |          | 1     |
      | Rezeptur       | GKV              | Anti Aging Chreme für die Achsel | quantum satis                                                                                                       |          | 5     |
      | Wirkstoff      | GKV              | Harnsäure Hemmer                 | Ibuprofen und Paracetamol -> beides und viel und so weiter                                                          | 16588573 | 2     |
      | Freitext       | PKV              | PlaceboCondimenti Luffa-Schwämme | Ein Bisschen Davon und noch mehr davon und das  ganze gut durchrühren                                               |          | 3     |
      | Rezeptur       | PKV              | Anti Aging Achsel-Chreme         | beidseitig auftragen                                                                                                |          | 5     |
      | Wirkstoff      | PKV              | Harnsäure Hemmer                 | Ibuprofen und Paracetamol -> beides und viel und so weiter                                                          | 16588573 | 1     |
