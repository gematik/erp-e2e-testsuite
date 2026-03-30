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
# *******
# For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
# *******
#
# language: de

@PRODUKT:eRp_FD
Funktionalität: Apothekenpflichtige Verordnungstypen für Versicherte und Selbstzahler
  Die Erezept-Anwendungsfälle für apothekenpflichtige Medikamente

  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und die Apotheke Stadtapotheke hat Zugriff auf ihre SMC-B

  @TCID:ERP_EE_REZEPTVARIANTEN_01
  @TESTFALL:positiv
  @AFO-ID:A_24034
  @Hauptdarsteller:Arzt
  Szenariogrundriss: Einstellen eines Apothekenpflichtigen E-Rezeptes mit verschiedenen Verordnungstypen und Versicherungsverhältnissen
  Die Apotheke akzeptiert das zugewiesene Rezept in den Variationen Frteitext-, Wirkstoff-, Rezepturverordnung und Kombipackung.

    Und die <Versicherungsart> Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgende apothekenpflichtige <Verordnungstyp> Verordnung verschreibt:
      | Name   | Freitext   | PZN   | Menge   | Impfung     | WirkstoffMenge      | WirkstoffMengeEinheit | Bezugsmenge           | BezugsmengeEinheit      | Wirkstoffname   |
      | <Name> | <Freitext> | <PZN> | <Menge> | <isVaccine> | <WirkstoffmengeNum> | <WirkstoffEinheitNum> | <WirkstoffmengeDenom> | <WirkstoffeinheitDemon> | <Wirkstoffname> |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestelltes E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren

    Beispiele:
      | Verordnungstyp | Versicherungsart | Name                             | Freitext                                                                                                            | PZN      | Menge | isVaccine | WirkstoffmengeNum | WirkstoffEinheitNum | WirkstoffmengeDenom | WirkstoffeinheitDemon | Wirkstoffname |
      | Freitext       | GKV              | PlaceboCondimenti Luffa-Schwämme | Ein Bisschen Davon und noch mehr davon und das in kreisenden Bewegungen einmassieren, wo ? -> nach eigenem Ermessen |          | 1     | false     |                   |                     |                     |                       |               |
      | Rezeptur       | GKV              | Anti Aging Chreme für die Achsel | quantum satis                                                                                                       | 03438010 | 5     | false     |                   |                     |                     |                       |               |
      | Wirkstoff      | GKV              | Harnsäure Hemmer                 | Ibuprofen und Paracetamol -> beides und viel und so weiter                                                          | 16588573 | 2     | true      |                   |                     |                     |                       |               |
      | Freitext       | PKV              | PlaceboCondimenti Luffa-Schwämme | Ein Bisschen Davon und noch mehr davon und das  ganze gut durchrühren                                               |          | 3     | false     |                   |                     |                     |                       |               |
      | Rezeptur       | PKV              | Anti Aging Chreme für die Achsel | beidseitig auftragen                                                                                                |          | 5     | false     |                   |                     |                     |                       |               |
      | Wirkstoff      | PKV              | Harnsäure Hemmer                 | Ibuprofen und Paracetamol -> beides und viel und so weiter                                                          | 16588573 | 1     | false     |                   |                     |                     |                       |               |


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
      | Rezeptur       | GKV              | Anti Aging Chreme für die Achsel | quantum satis                                                                                                       | 03438010 | 5     |
      | Wirkstoff      | GKV              | Harnsäure Hemmer                 | Ibuprofen und Paracetamol -> beides und viel und so weiter                                                          | 16588573 | 2     |
      | Freitext       | PKV              | PlaceboCondimenti Luffa-Schwämme | Ein Bisschen Davon und noch mehr davon und das  ganze gut durchrühren                                               |          | 3     |
      | Rezeptur       | PKV              | Anti Aging Achsel-Chreme         | beidseitig auftragen                                                                                                |          | 5     |
      | Wirkstoff      | PKV              | Harnsäure Hemmer                 | Ibuprofen und Paracetamol -> beides und viel und so weiter                                                          | 16588573 | 1     |

  @TCID:ERP_EE_REZEPTVARIANTEN_03
  @TESTFALL:positiv
  @AFO-ID:A_24034
  @Hauptdarsteller:Arzt
  Szenariogrundriss: Einstellen eines Apothekenpflichtigen E-Rezeptes mit einer PZN- Kombipackung und Versicherungsverhältnissen
  Die Apotheke akzeptiert das zugewiesene Rezept als Kombipackung und erstellt eine MedicationDispense mit contained Medications für die zwei enthaltenen Produkte.

    Und die <Versicherungsart> Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgende apothekenpflichtiges Medikament verschreibt:
      | Name   | Freitext   | PZN   | Menge   | Impfung     | WirkstoffMenge      | WirkstoffMengeEinheit | Bezugsmenge           | BezugsmengeEinheit      | Wirkstoffname   | Darreichungsform        |
      | <Name> | <Freitext> | <PZN> | <Menge> | <isVaccine> | <WirkstoffmengeNum> | <WirkstoffEinheitNum> | <WirkstoffmengeDenom> | <WirkstoffeinheitDemon> | <Wirkstoffname> | <DarrerichungsformCode> |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestelltes E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept mit den folgenden Medikamenten korrekt an Sina Hüllmann dispensiert:
      | Name                    | PZN      | Kategorie | Normgröße | Menge | Einheit | Darreichungsform | WirkstoffmengeNum | WirkstoffEinheitNum | WirkstoffmengeDenom | WirkstoffeinheitDemon | Wirkstoffname          |
      | Allergodil Augentropfen | 01038246 | 00        | N1        | 1     | Stk     | KPG              | 0.015             | mg                  | 1.5                 | Tropfen               | Azelastin hydrochlorid |
      | Allergodil Nasenspray   | 01038246 | 00        | N1        | 1     | Stk     | KPG              | 0.14              | mg                  | 1.2                 | Sprühstoß             | Azelastin hydrochlorid |

    Beispiele:
      | Versicherungsart | Name                         | Freitext             | PZN      | Menge | isVaccine | WirkstoffmengeNum | WirkstoffEinheitNum | WirkstoffmengeDenom | WirkstoffeinheitDemon | Wirkstoffname          | DarrerichungsformCode |
      | GKV              | Otowaxol Kombipackung, 10 ml | beides und so weiter | 01038246 | 2     | false     | 0.15              | mg                  | 0.5                 | L                     | Azelastin hydrochlorid | KPG                   |


  @TCID:ERP_EE_REZEPTVARIANTEN_04
  @TESTFALL:positiv
  @AFO-ID:A_27827
  @Hauptdarsteller:Arzt
  Szenariogrundriss: Einstellen eines Apothekenpflichtigen T-Rezeptes mit einer PZN-Verordnung
  Die Apotheke akzeptiert das zugewiesene Rezept als  und erstellt eine MedicationDispense mit contained Medications für die zwei enthaltenen Produkte.

    Und die <Versicherungsart> Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgende apothekenpflichtiges Medikament verschreibt:
      | Name   | PZN   | Menge   | Impfung     | Verordnungskategorie   | Reichdauer in Wochen   | WirkstoffMenge      | WirkstoffMengeEinheit | Bezugsmenge           | BezugsmengeEinheit      | Wirkstoffname   | Darreichungsform        | Workflow |
      | <Name> | <PZN> | <Menge> | <isVaccine> | <Verordnungskategorie> | <Reichdauer in Wochen> | <WirkstoffmengeNum> | <WirkstoffEinheitNum> | <WirkstoffmengeDenom> | <WirkstoffeinheitDemon> | <Wirkstoffname> | <DarrerichungsformCode> | <WF>     |


    Und die Versicherte Sina Hüllmann ihr letztes ausgestelltes E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren

    Beispiele:
      | Versicherungsart | Name                                       | PZN      | Menge | isVaccine | Verordnungskategorie | Reichdauer in Wochen | WirkstoffmengeNum | WirkstoffEinheitNum | WirkstoffmengeDenom | WirkstoffeinheitDemon | Wirkstoffname | DarrerichungsformCode | WF  |
      | GKV              | Pomalidomid Accord 1 mg 21 x 1 Hartkapseln | 19201712 | 2     | false     | 02                   | 3                    | 0.15              | mg                  | 0.5                 | L                     | Pomalidomid   | KMR                   | 166 |
