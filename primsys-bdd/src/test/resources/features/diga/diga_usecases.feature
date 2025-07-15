#
# Copyright 2025 gematik GmbH
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
@Funktionalität:DiGA
@Workflow:162
@Versicherung:GKV
@Hauptdarsteller:Krankenkasse
Funktionalität: DiGA Anwendungsfälle

  Grundlage:
    Angenommen der Kostenträger AOK Bremen hat Zugriff auf seine SMC-B KTR
    Und die GKV Versicherte Hanna Bäcker hat Zugriff auf ihre eGK

  @TCID:ERP_EE_WF162_03
  Szenariogrundriss: Ein Psychotherapeut verschreibt der Versicherten Hanna Bäcker ein EVGDA E-Rezept.
    Der Kostenträger "AOK Bremen" akzeptiert und generiert den Freischaltcode und die Abgabeinformationen.
    Angenommen der Arzt <VerordnendeName> hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und der <VerordnendeRolle> <VerordnendeName> der Versicherten Hanna Bäcker ein EVGDA E-Rezept verschreibt
    Wenn die GKV Versicherte Hanna Bäcker ihr letztes ausgestelltes EVGDA E-Rezept ihrem Kostenträger AOK Bremen zuweist
    Dann kann der Kostenträger AOK Bremen das letzte EVGDA E-Rezept der Versicherten Hanna Bäcker akzeptieren
    Und der Kostenträger AOK Bremen kann für die letzte EVGDA der Versicherten Hanna Bäcker Abgabeinformationen mit Freischaltcode bereitstellen
    Und die GKV Versicherte Hanna Bäcker kann für die letzte EVGDA den Freischaltcode abrufen
    Und die GKV Versicherte Hanna Bäcker kann für die letzte EVDGA Informationen im Protokoll einsehen
    Und die GKV Versicherte Hanna Bäcker kann das letzte EVDGA mit dem FdV löschen

    Beispiele:
      | VerordnendeRolle                | VerordnendeName    |
      | Psychotherapeut                 | Finn Stauffenberg  |
      | Psychologischer Psychotherapeut | Julian Böcher       |
      | Kinderpsychotherapeut           | Peter Kleinschmidt |
      | Arzt                            | Adelheid Ulmenwald |
      | Zahnarzt                        | Gündüla Gunther    |

  @TCID:ERP_EE_WF162_04
  Szenario: Abgabe ohne Freischaltcode, wenn kein Leistungsanspruch vorliegt
    Angenommen der Psychotherapeut Finn Stauffenberg hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und der Psychotherapeut Finn Stauffenberg der Versicherten Hanna Bäcker ein EVGDA E-Rezept verschreibt
    Wenn die GKV Versicherte Hanna Bäcker ihr letztes ausgestelltes EVGDA E-Rezept ihrem Kostenträger AOK Bremen zuweist
    Dann kann der Kostenträger AOK Bremen das letzte EVGDA E-Rezept der Versicherten Hanna Bäcker akzeptieren
    Und der Kostenträger AOK Bremen kann für die letzte EVGDA der Versicherten Hanna Bäcker Abgabeinformationen ohne Freischaltcode bereitstellen
    Und die GKV Versicherte Hanna Bäcker kann für die letzte EVGDA keinen Freischaltcode abrufen

  @TCID:ERP_EE_WF162_05
  Szenario: Abgabe einer EVGDA als Apotheke
    Angenommen die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und der Psychotherapeut Finn Stauffenberg hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und der Psychotherapeut Finn Stauffenberg der Versicherten Hanna Bäcker ein EVGDA E-Rezept verschreibt
    Wenn die GKV Versicherte Hanna Bäcker ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Dann kann die Apotheke Am Flughafen das letzte EVGDA E-Rezept nicht akzeptieren

  @TCID:ERP_EE_WF162_06
  Szenario: keine zeitnahe Dispensierung durch den Kostenträger
    Angenommen der Psychotherapeut Finn Stauffenberg hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und der Psychotherapeut Finn Stauffenberg der Versicherten Hanna Bäcker ein EVGDA E-Rezept verschreibt
    Wenn die GKV Versicherte Hanna Bäcker ihr letztes ausgestelltes EVGDA E-Rezept ihrem Kostenträger AOK Bremen zuweist
    Dann kann der Kostenträger AOK Bremen das letzte EVGDA E-Rezept der Versicherten Hanna Bäcker akzeptieren
    Und der Kostenträger AOK Bremen kann für die letzte akzeptierte EVGDA keine zeitnahe Dispensierung durchführen

  @TCID:ERP_EE_WF162_07
  Szenario: Kostenträger darf EVGDA nicht löschen
    Angenommen der Psychotherapeut Finn Stauffenberg hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und der Psychotherapeut Finn Stauffenberg der Versicherten Hanna Bäcker ein EVGDA E-Rezept verschreibt
    Wenn die GKV Versicherte Hanna Bäcker ihr letztes ausgestelltes EVGDA E-Rezept ihrem Kostenträger AOK Bremen zuweist
    Und der Kostenträger AOK Bremen das letzte EVGDA der Versicherten Hanna Bäcker akzeptiert
    Dann kann der Kostenträger AOK Bremen das letzte akzeptierte EVGDA nicht löschen

  @TCID:ERP_EE_WF162_08
  Szenario: Kostenträger darf EVGDA zurückweisen
    Angenommen der Psychotherapeut Adelheid Ulmenwald hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und der Psychotherapeut Adelheid Ulmenwald der Versicherten Hanna Bäcker ein EVGDA E-Rezept verschreibt
    Wenn die GKV Versicherte Hanna Bäcker ihr letztes ausgestelltes EVGDA E-Rezept ihrem Kostenträger AOK Bremen zuweist
    Und der Kostenträger AOK Bremen das letzte EVGDA der Versicherten Hanna Bäcker akzeptiert
    Dann kann der Kostenträger AOK Bremen das letzte akzeptierte EVGDA zurückweisen
    Und die GKV Versicherte Hanna Bäcker kann für die letzte EVGDA keinen Freischaltcode abrufen


  @TCID:ERP_EE_WF162_09
  Szenario: Kostenträger darf kein Workflow 160 abrufen/akzeptieren
    Angenommen der Arzt Adelheid Ulmenwald hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und der Arzt Adelheid Ulmenwald der Versicherten Hanna Bäcker ein apothekenpflichtiges Medikament verschreibt
    Wenn die GKV Versicherte Hanna Bäcker ihr letztes ausgestelltes E-Rezept ihrem Kostenträger AOK Bremen zuweist
    Dann kann der Kostenträger AOK Bremen das letzte E-Rezept der Versicherten Hanna Bäcker nicht akzeptieren

  @TCID:ERP_EE_WF162_10
  Szenario: Subscription für den Kostenträger
    Angenommen der Psychotherapeut Finn Stauffenberg hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und der Kostenträger AOK Bremen sich beim E-Rezept Fachdienst registriert
    Und der Psychotherapeut Finn Stauffenberg der Versicherten Hanna Bäcker ein EVGDA E-Rezept verschreibt
    Wenn die GKV Versicherte Hanna Bäcker ihr letztes ausgestelltes EVGDA E-Rezept ihrem Kostenträger AOK Bremen zuweist
    Dann wird der Kostenträger AOK Bremen über die neue Zuweisungen informiert

  @TCID:ERP_EE_WF162_11
  Szenario: Kostenträger erklärt die Ablehnung der DiGA mit Hilfe des Fachdienstes
    Angenommen der Psychotherapeut Adelheid Ulmenwald hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und der Psychotherapeut Adelheid Ulmenwald der Versicherten Hanna Bäcker ein EVGDA E-Rezept verschreibt
    Wenn die GKV Versicherte Hanna Bäcker ihr letztes ausgestelltes EVGDA E-Rezept ihrem Kostenträger AOK Bremen zuweist
    Und der Kostenträger AOK Bremen das letzte EVGDA der Versicherten Hanna Bäcker akzeptiert
    Und der Kostenträger AOK Bremen kann für die letzte EVGDA der Versicherten Hanna Bäcker Abgabeinformationen ohne Freischaltcode bereitstellen
    Dann kann der Kostenträger AOK Bremen für das letzte akzeptierte EVGDA E-Rezept der Versicherten Hanna Bäcker eine Begründung senden
    Und die GKV Versicherte Hanna Bäcker kann für die letzte EVGDA Verordnung Informationen vom Kostenträger AOK Bremen im FdV einsehen


