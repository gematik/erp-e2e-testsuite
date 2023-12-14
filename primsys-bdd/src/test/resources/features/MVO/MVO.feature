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
@Funktionalität:MVO
Funktionalität: Mehrfachverordnung von apothekenpflichtigen Arzneimitteln
  Eine Mehrfachverordnung besteht aus mindestens 2 bis maximal 4 Teilverordnungen.
  Jede Teilverordnung einer Mehrfachverordnung ist ein vollständiges E-Rezept mit QES-signierten Verordnungsdatensatz und E-Rezept-Token.

  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Und der PKV Versicherte Günther Angermänn hat Zugriff auf seine digitale Identität
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B


  @TCID:ERP_EE_MVO_01
  @AFO-ID:A_22627
  @TESTFALL:positiv
  @Workflow:160
  @Versicherung:GKV
  @Hauptdarsteller:Arztpraxis
  Szenariogrundriss: Mehrfachverordnung für GKV-Versicherte
  Gutfall: E-Rezepte für GKV-Versicherte als Mehrfachverordnung (WF 160)

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | MVO  | Denominator | Numerator | Gueltigkeitsstart | Gueltigkeitsende |
      | true | <Anzahl>    | <Nummer>  | <Start>           | leer             |
    Dann wird der Versicherten Sina Hüllmann das neue E-Rezept angezeigt

    Beispiele:
      | Anzahl | Nummer | Start |
      | 4      | 1      | 0     |
      | 4      | 2      | 90    |
      | 4      | 3      | 180   |
      | 4      | 4      | 270   |


  @TCID:ERP_EE_MVO_02
  @AFO-ID:A_22627
  @TESTFALL:positiv
  @Workflow:200
  @Versicherung:PKV
  @Hauptdarsteller:Arztpraxis
  Szenariogrundriss: Mehrfachverordnung für PKV-Versicherte
  Gutfall: E-Rezepte für PKV-Versicherte als Mehrfachverordnung erstellen (WF 200)

    Wenn die Ärztin Dr. Schraßer dem Versicherten Günther Angermänn folgendes apothekenpflichtiges Medikament verschreibt:
      | MVO  | Denominator | Numerator | Gueltigkeitsstart | Gueltigkeitsende |
      | true | <Anzahl>    | <Nummer>  | <Start>           | <Ende>           |
    Dann wird dem Versicherten Günther Angermänn das neue E-Rezept angezeigt

    Beispiele:
      | Anzahl | Nummer | Start | Ende |
      | 4      | 1      | 0     | 90   |
      | 4      | 2      | 90    | 180  |
      | 4      | 3      | 180   | 270  |
      | 4      | 4      | 270   | 360  |


  @TCID:ERP_EE_MVO_03
  @AFO-ID:A_22627-01
  @TESTFALL:positiv
  @Workflow:169
  @Versicherung:GKV
  @Hauptdarsteller:Arztpraxis
  Szenariogrundriss: Mehrfachverordnung für GKV-Versicherte als Direktzuweisung
  Gutfall: Direktzuweisung für GKV-Versicherte als Mehrfachverordnung (WF 169)

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist:
      | MVO  | Denominator | Numerator | Gueltigkeitsstart | Gueltigkeitsende |
      | true | <Anzahl>    | <Nummer>  | <Start>           | <Ende>           |
    Dann wird der Versicherten Sina Hüllmann das neue E-Rezept angezeigt

    Beispiele:
      | Anzahl | Nummer | Start | Ende |
      | 4      | 1      | 0     | 90   |
      | 4      | 2      | 90    | 180  |
      | 4      | 3      | 180   | 270  |
      | 4      | 4      | 270   | leer |


  @TCID:ERP_EE_MVO_04
  @AFO-ID:A_22627-01
  @TESTFALL:positiv
  @Workflow:209
  @Versicherung:PKV
  @Hauptdarsteller:Arztpraxis
  Szenariogrundriss: Mehrfachverordnung für PKV-Versicherte als Direktzuweisung
  Gutfall: Direktzuweisung für PKV-Versicherte  als Mehrfachverordnung (WF 209)

    Wenn die Ärztin Dr. Schraßer dem Versicherten Günther Angermänn folgendes Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist:
      | MVO  | Denominator | Numerator | Gueltigkeitsstart | Gueltigkeitsende |
      | true | <Anzahl>    | <Nummer>  | <Start>           | <Ende>           |
    Dann wird dem Versicherten Günther Angermänn das neue E-Rezept angezeigt

    Beispiele:
      | Anzahl | Nummer | Start | Ende |
      | 4      | 1      | 0     | 90   |
      | 4      | 2      | 90    | 180  |
      | 4      | 3      | 180   | 270  |
      | 4      | 4      | 270   | leer |


  @TCID:ERP_EE_MVO_05
  @AFO-ID:A_22632
  @Funktionalität:Entlassrezept
  @TESTFALL:negativ
  @Hauptdarsteller:Arztpraxis
  Szenariogrundriss: Entlassrezept nicht als Mehrfachverordnung
    Dann darf die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann das folgende E-Rezept nicht ausstellen:
      | KBV_Statuskennzeichen | MVO  | Denominator | Numerator | Gueltigkeitsstart | Gueltigkeitsende |
      | <Kennzeichen>         | true | 4           | 1         | 0                 | 90               |

    Beispiele:
      | Kennzeichen |
      | 04          |
      | 14          |


  @TCID:ERP_EE_MVO_06
  @TESTFALL:positiv
  @Hauptdarsteller:Apotheke
  Szenario: Erstes Teilrezept einer Mehrfachverordnung einlösen

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | MVO  | Denominator | Numerator | Gueltigkeitsstart | Gueltigkeitsende |
      | true | 4           | 1         | 0                 | 90               |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Sina Hüllmann dispensiert
    Dann hat die Apotheke Am Flughafen genau 1 Quittung vorliegen
    Und hat der Versicherte Sina Hüllmann genau 1 Medikament erhalten


  @TCID:ERP_EE_MVO_07
  @AFO-ID:A_22635
  @TESTFALL:negativ
  @Hauptdarsteller:Apotheke
  Szenariogrundriss: Teilrezept nicht einlösbar, wenn Start des Gütligkeitszeitraums noch nicht erreicht

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | MVO  | Denominator | Numerator | Gueltigkeitsstart | Gueltigkeitsende |
      | true | 4           | <Nummer>  | 90                | 180              |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Dann wird der Versicherten Sina Hüllmann das neue E-Rezept angezeigt
    Dann kann die Apotheke Am Flughafen das letzte zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es noch nicht gültig ist

    Beispiele:
      | Nummer |
      | 1      |
      | 2      |
      | 3      |
      | 4      |

  @TCID:ERP_EE_MVO_08
  @AFO-ID:A_22704
  @AFO-ID:A_22628
  @AFO-ID:A_22629
  @AFO-ID:A_22630
  @AFO-ID:A_22632
  @AFO-ID:A_22634
  @TESTFALL:negativ
  @Hauptdarsteller:Fachdienst
  Szenariogrundriss: Unzulässige Denominatoren und Numeratoren
  1. Denominator muss größer 1 sein (A_22629)
  2. Denominator darf nicht größer 4 sein (A_22628)
  3. Numerator darf nicht größer 4 sein (A_22628)
  4. Numerator darf nicht größer als Denominator sein (22630)
  5. Numerator muss größer 0 sein sein (A_22704)
  6. Denominator muss größer 0 sein sein (A_22629)
  7. Beginn der Einlösefrist muss gesetzt sein (A_22634)
  8. Beginn des Gültigkeitszeitraums muss vor Ende des Zeitraums liegen


    Dann darf die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann das folgende E-Rezept nicht ausstellen:
      | MVO   | Denominator | Numerator | Gueltigkeitsstart | Gueltigkeitsende |
      | <MVO> | <Anzahl>    | <Nummer>  | <Start>           | <Ende>           |


    Beispiele:
      | MVO  | Anzahl | Nummer | Start | Ende |
      | true | 1      | 1      | 0     | 90   |
      | true | 5      | 1      | 0     | 90   |
      | true | 4      | 5      | 180   | 270  |
      | true | 3      | 4      | 270   | 360  |
      | true | 4      | 0      | 0     | 90   |
      | true | 0      | 1      | 0     | 90   |
      | true | 4      | 1      | leer  | 1    |
      | true | 4      | 1      | 180   | 90   |


  @TCID:ERP_EE_MVO_09
  @AFO-ID:A_22627
  @TESTFALL:positiv
  @Workflow:160
  @Versicherung:GKV
  @Hauptdarsteller:Arztpraxis
  Szenario: Zusammenhängende Mehrfachverordnung für GKV-Versicherte
  Alle vier Mehrfachverordnungen werden zusammenhängend (über eine gemeinsame ID verknüpft) auf einmal ausgestellt

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | MVO  | MVO-ID   | Denominator | Numerator | Gueltigkeitsstart | Gueltigkeitsende |
      | true | 13061707 | 4           | 1         | 0                 | leer             |
      | true |          | 4           | 2         | 90                | leer             |
      | true |          | 4           | 3         | 180               | leer             |
      | true |          | 4           | 4         | 270               | leer             |
    Dann wird der Versicherten Sina Hüllmann das neue E-Rezept angezeigt
