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
@Workflow:160
@Versicherung:GKV
@AFO-ID:A_18827
@AFO-ID:A_18502
@AFO-ID:A_18503
Funktionalität: Apothekenpflichtige Medikamente für GKV-Versicherte
  Die Erezept-Anwendungsfälle für apothekenpflichtige Medikamente für GKV-Versicherte (Workflow 160)

  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und die Apotheke Stadtapotheke hat Zugriff auf ihre SMC-B

  @TCID:ERP_EE_WF160_01
  @TESTFALL:positiv
  @AFO-ID:A_18506
  @AFO-ID:A_18822
  @AFO-ID:A_19019
  Szenario: E-Rezept verschreiben und dem Versicherten Anzeigen
  Der Arzt verschreibt der GKV-Versicherten ein E-Rezept für ein apothekenpflichtiges Rezept. Dieses Rezept soll im FdV angezeigt werden.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Dann wird der Versicherten Sina Hüllmann das neue E-Rezept angezeigt

  @TCID:ERP_EE_WF160_02
  @TESTFALL:positiv
  @AFO-ID:A_18511
  @AFO-ID:A_18514
  @AFO-ID:A_19233-04
  Szenario: E-Rezept verschreiben, via DMC zuweisen und erfolgreich dispensieren
  Die Ärztin verschreibt der GKV-Versicherten ein E-Rezept für ein apothekenpflichtiges Rezept.
  Dieses Rezept wird in der Apotheke über einen QR-Code eingelöst. Die Apotheke hat daraufhin eine Quittung für das dispensierte Medikament.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Sina Hüllmann dispensiert
    Dann hat die Apotheke Am Flughafen genau 1 Quittung vorliegen
    Dann kann die Apotheke Am Flughafen die Signatur der letzten Quittung erfolgreich mit dem Konnektor validieren
    Und hat der Versicherte Sina Hüllmann genau 1 Medikament erhalten


  @TCID:ERP_EE_WF160_03
  @TESTFALL:positiv
  @AFO-ID:A_19248-01
  @Hauptdarsteller:Versicherter
  Szenario: Dispensierinformationen zum E-Rezept als Versicherter einsehen

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Sina Hüllmann dispensiert
    Dann kann die Versicherte Sina Hüllmann 1 Dispensierinformation für ihr letztes E-Rezept abrufen


  @TCID:ERP_EE_WF160_04
  @TESTFALL:positiv
  @AFO-ID:A_19117
  @AFO-ID:A_19226
  @Hauptdarsteller:Apotheke
  Szenario: Quittung erneut abfragen
  Die Apotheke kann nach erfolgreicher Dispensierung die Quittung erneut abrufen.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Sina Hüllmann dispensiert
    Dann hat die Apotheke Am Flughafen genau 1 Quittung vorliegen
    Dann kann die Apotheke Am Flughafen die letzte Quittung erneut abrufen
    Dann hat die Apotheke Am Flughafen genau 2 Quittungen vorliegen
    Dann kann die Apotheke Am Flughafen die Signatur der letzten Quittung erfolgreich mit dem Konnektor validieren

  @TCID:ERP_EE_WF160_05
  @TESTFALL:positiv
  @AFO-ID:A_18505
  @AFO-ID:A_19149
  @Hauptdarsteller:Arztpraxis
  Szenario: E-Rezept durch Verordnenden löschen
  Der Arzt verschreibt der GKV-Versicherten ein E-Rezept für ein apothekenpflichtiges Rezept. Dieses Rezept kann er löschen,
  solange es nicht von einer Apotheke akzeptiert wurde.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Ärztin Dr. Schraßer das letzte von ihr eingestellte E-Rezept löscht
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Dann kann die Apotheke Am Flughafen das letzte zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es nicht mehr existiert


  @TCID:ERP_EE_WF160_06
  @TESTFALL:positiv
  @AFO-ID:A_18507
  @Hauptdarsteller:Versicherter
  Szenario: E-Rezept durch Versicherten löschen
  Der Arzt verschreibt der GKV-Versicherten ein E-Rezept für ein apothekenpflichtiges Rezept.
  Dieses Rezept kann die Versicherte löschen, solange es nicht von einer Apotheke akzeptiert wurde.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes E-Rezept löscht
    Und die Versicherte Sina Hüllmann ihr letztes gelöschte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Dann kann die Apotheke Am Flughafen das letzte zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es nicht mehr existiert

  @TCID:ERP_EE_WF160_07
  @TESTFALL:negativ
  @AFO-ID:A_19120-03
  @Hauptdarsteller:Arztpraxis
  Szenario: Löschen des E-Rezepts nicht möglich, weil durch Apotheke reserviert
  Der Arzt verschreibt der GKV-Versicherten ein E-Rezept für ein apothekenpflichtiges Rezept. Dieses Rezept kann er nicht mehr löschen,
  sobald es von einer Apotheke akzeptiert wurde.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Ärztin Dr. Schraßer das letzte von ihr eingestellte E-Rezept nicht löschen, weil sie nicht das Recht dazu hat
    Dann wird der Versicherten Sina Hüllmann das neue E-Rezept angezeigt
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren

  @TCID:ERP_EE_WF160_08
  @TESTFALL:negativ
  @AFO-ID:A_18512
  @AFO-ID:A_19172
  @Hauptdarsteller:Apotheke
  Szenario: Einlösen des E-Rezepts nicht möglich, weil durch Apotheke zurückgegeben
  Die Apotheke gibt das zugewiesene Rezept zurück. Anschließend kann sie es nicht einlösen.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte Rezept zurückweist
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept nicht dispensieren, weil sie nicht das Recht dazu hat


  @TCID:ERP_EE_WF160_09
  @TESTFALL:negativ
  @AFO-ID:A_18513
  @AFO-ID:A_19121
  @Hauptdarsteller:Apotheke
  Szenario: Einlösen des E-Rezepts nicht möglich, weil durch Apotheke gelöscht
  Die Apotheke löscht das zugewiesene Rezept. Anschließend kann sie es nicht einlösen.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept löscht
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept nicht dispensieren, weil es nicht mehr existiert


  @TCID:ERP_EE_WF160_10
  @TESTFALL:negativ
  @AFO-ID:A_19168
  @Hauptdarsteller:Apotheke
  Szenario: Zweimaliges Akzeptieren des E-Rezepts durch gleiche Apotheke nicht möglich
  Die Apotheke akzeptiert das zugewiesene Rezept. Anschließend kann sie es nicht noch mal akzeptieren.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen das letzte zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es einen Konflikt gibt
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren


  @TCID:ERP_EE_WF160_11
  @TESTFALL:negativ
  @AFO-ID:A_19168
  @Hauptdarsteller:Apotheke
  Szenario: E-Rezept kann nicht durch eine zweite Apotheke reserviert werden
  Die Apotheke akzeptiert das Rezept. Anschließend versucht eine zweite Apotheke das Rezept einzulösen. Das wird durch den Fachdienst verhindert.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Stadtapotheke via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Stadtapotheke das letzte zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es einen Konflikt gibt
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren


  @TCID:ERP_EE_WF160_12
  @TESTFALL:negativ
  @AFO-ID:A_19121
  @Hauptdarsteller:Apotheke
  Szenario: Einlösen des E-Rezepts nicht möglich, weil durch Versicherten gelöscht
  Der Versicherte löscht das Rezept nach der Zuweisung an eine Apotheke. Die Apotheke kann es deshalb nicht akzeptieren.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Versicherte Sina Hüllmann ihr letztes E-Rezept löscht
    Dann kann die Apotheke Am Flughafen das letzte zugewiesene E-Rezept nicht beim Fachdienst akzeptieren, weil es nicht mehr existiert


  @TCID:ERP_EE_WF160_13
  @TESTFALL:negativ
  @AFO-ID:A_19146
  @Hauptdarsteller:Apotheke
  Szenario: Löschen des E-Rezepts durch Apotheke nicht möglich, weil nicht akzeptiert

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Dann kann die Apotheke Am Flughafen das letzte zugewiesene E-Rezept ohne zu akzeptieren nicht löschen

  @TCID:ERP_EE_WF160_14
  @TESTFALL:negativ
  @AFO-ID:A_19145
  @Hauptdarsteller:Versicherter
  Szenario: Löschen von akzeptierten E-Rezepten durch den Versicherten nicht möglich
  Der Versicherte versucht das Rezept zu löschen, nachdem es schon von einer Apotheke akzeptiert wurde. Das wird durch den Fachdienst verhindert.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Versicherte Sina Hüllmann ihr letztes E-Rezept nicht löschen, weil sie nicht das Recht dazu hat
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren

  @TCID:ERP_EE_WF160_15
  @TESTFALL:negativ
  @Hauptdarsteller:Versicherter
  Szenario: Löschen von eingelösten E-Rezepten durch den Versicherten möglich

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Sina Hüllmann dispensiert
    Und die Versicherte Sina Hüllmann ihr letztes E-Rezept löscht
    Und hat der Versicherte Sina Hüllmann genau 1 Medikamente erhalten
    Dann wird der Versicherten Sina Hüllmann ihr letztes gelöschte E-Rezept nicht mehr angezeigt

  @TCID:ERP_EE_WF160_16
  @TESTFALL:negativ
  @AFO-ID:A_19232
  @Hauptdarsteller:Apotheke
  Szenario: Erneutes Dispensieren durch die gleiche Apotheke nicht möglich
  Die Apotheke kann nach erfolgreicher Dispensierung den Task nicht noch einmal mit einem /Task/<id>/$close-Operation beenden,
  weil der Task im Status "completed" ist.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Sina Hüllmann dispensiert
    Dann kann die Apotheke Am Flughafen das letzte dispensierte E-Rezept nicht erneut dispensieren

  @TCID:ERP_EE_WF160_17
  @TESTFALL:negativ
  @AFO-ID:A_19231
  @Hauptdarsteller:Apotheke
  Szenario: Dispensieren mit falschem Secret nicht möglich
  Der Fachdienst muss beim Dispensieren das mitgelieferte Secret prüfen und bei ungültigem Secret die HTTP-POST-Operation
  über /Task/<id>/$close ablehnen.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept nicht mit dem falschen Secret fgdkjfgd dispensieren
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren

  @TCID:ERP_EE_WF160_18
  @TESTFALL:negativ
  @AFO-ID:A_19224
  @Hauptdarsteller:Apotheke
  Szenario: Löschen mit falschem Secret nicht möglich
  Der Fachdienst muss beim Löschen das mitgelieferte Secret prüfen und bei ungültigem Secret die HTTP-POST-Operation
  über /Task/<id>/$abort ablehnen.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept nicht mit dem falschen Secret fgdkjfgd löschen
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren

  @TCID:ERP_EE_WF160_19
  @TESTFALL:negativ
  @AFO-ID:A_19171
  @Hauptdarsteller:Apotheke
  Szenario: Zurückgeben mit falschem Secret nicht möglich
  Der Fachdienst muss beim Zurückgeben das mitgelieferte Secret prüfen und bei ungültigem Secret die HTTP-POST-Operation
  über /Task/<id>/$reject ablehnen.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept nicht mit dem falschen Secret fgdkjfgd zurückgeben
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren

  @TCID:ERP_EE_WF160_20
  @TESTFALL:negativ
  @AFO-ID:A_19405-01
  @Hauptdarsteller:Apotheke
  Szenario: Apotheke darf Dispensierinformationen nicht abrufen

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Sina Hüllmann dispensiert
    Dann darf die Apotheke Am Flughafen die Dispensierinformationen für das letzte dispensierte E-Rezept nicht abrufen

  @TCID:ERP_EE_WF160_21
  @TESTFALL:negativ
  @AFO-ID:A_19018
  @Hauptdarsteller:Apotheke
  Szenario: Eine Apotheke darf kein E-Rezept einstellen
  Der Fachdienst muss das Anlegen eines E-Rezepts ablehnen, wenn der Access-Token nicht eine der folgendes OID aufweist:
  oid_arzt
  oid_zahnarzt
  oid_praxis_arzt
  oid_zahnarztpraxis
  oid_praxis_psychotherapeut
  oid_krankenhaus

  In diesem Testfall wird versucht mit der SMC-B einer Apotheke ein Create-Aufruf zum Fachdienst zu schicken.

    Angenommen die Ärztin Keine Arztpraxis hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Dann darf der Arzt Keine Arztpraxis der Versicherten Sina Hüllmann das folgende E-Rezept nicht ausstellen:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |


  @TCID:ERP_EE_WF160_22
  @TESTFALL:negativ
  @AFO-ID:A_19225-01
  @Hauptdarsteller:Apotheke
  Szenario: Ein Apotheker darf kein E-Rezept signieren
  Der Fachdienst muss die Aktivierung ablehnen, wenn die QES nicht von der Berufsgruppe
  oid_arzt, oid_zahnarzt ode id-baek-at-namingAuthorityÄrzteschaft-Ärztin/Arzt (1.3.6.1.4.1.24796.4.11.1) gemäß [BÄK_G0] erstellt wurde.
  In diesem Testfall wird die QES mit einem HBA Apotheker erstellt.

    Angenommen die Ärztin Kein Arzt hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Dann darf der Arzt Kein Arzt der Versicherten Sina Hüllmann kein E-Rezept ausstellen

  @TCID:ERP_EE_WF160_23
  @TESTFALL:positiv
  @AFO-ID:A_18781
  @AFO-ID:A_19116
  @Hauptdarsteller:Versicherter
  Szenario: Abrufen des E-Rezepts als Vertreter
  Der Versicherte weist das ausgestellte Rezept einem Vertreter zu. Dieser kann es erfolgreich in seinem FdV abrufen.

    Angenommen der GKV Versicherte Günther Angermänn hat Zugriff auf seine eGK
    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Und die Versicherte Sina Hüllmann ihr letztes E-Rezept per Nachricht an den Vertreter Günther Angermänn schickt
    Dann hat der Vertreter Günther Angermänn die Nachricht mit dem Rezept der Versicherten Sina Hüllmann empfangen
    Dann wird der Versicherten Sina Hüllmann das neue E-Rezept angezeigt
    Dann wird dem Vertreter Günther Angermänn das neue E-Rezept angezeigt


  @TCID:ERP_EE_WF160_24
  @TESTFALL:positiv
  @AFO-ID:A_20546-01
  @Hauptdarsteller:Versicherter
  Szenario: Löschen des E-Rezepts als Vertreter nur mit AccessCode möglich
  Der Versicherte weist das ausgestellte Rezept einem Vertreter zu. Dieser kann es ohne AccessCode nicht beim Fachdienst löschen.

    Angenommen der GKV Versicherte Günther Angermänn hat Zugriff auf seine eGK
    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann ein apothekenpflichtiges Medikament verschreibt
    Und die Versicherte Sina Hüllmann ihr letztes E-Rezept per Nachricht an den Vertreter Günther Angermänn schickt
    Und der Vertreter Günther Angermänn hat die Nachricht mit dem Rezept der Versicherten Sina Hüllmann empfangen
    Dann kann der Vertreter Günther Angermänn das letzte von Sina Hüllmann zugewiesene E-Rezept ohne AccessCode nicht löschen
    Aber der Vertreter Günther Angermänn kann das letzte von Sina Hüllmann zugewiesene E-Rezept mit AccessCode löschen

  @TCID:ERP_EE_WF160_25
  @TESTFALL:positiv
  @AFO-ID:A_24179
  @Hauptdarsteller:Apotheke
  Szenario: Zweimaliges Akzeptieren des E-Rezepts durch gleiche Apotheke nach Datenverlust möglich
  Die Apotheke akzeptiert das zugewiesene Rezept. Aus technischen unverschuldeten Gründen erhält oder speichert sie das Secret jedoch nicht.
  So kann sie erneut ihr Secret beim FachDienst abrufen.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name             | PZN      |
      | Harnsäure Hemmer | 16588573 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept verliert
    Dann kann die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst erneut abrufen
    Dann kann die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensieren

  @TCID:ERP_EE_WF160_26
  @TESTFALL:negativ
  @AFO-ID:A_24176
  @Hauptdarsteller:Apotheke
  Szenario: Abrufen eines E-Rezeptes ohne vorheriges Akzeptieren
  Die Apotheke versucht ein E-Rezept einzulösen. Sie hat aber vorher vergessen es zu akzeptieren.
  Das wird durch den Fachdienst verhindert. (GetTaskById -> dafür ResponseOfGetTaskById) ohne vorher zu akzeptieren (AcceptTask)

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name             | PZN      |
      | Harnsäure Hemmer | 16588573 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Dann kann die Apotheke Am Flughafen das letzte zugewiesene E-Rezept nicht beim Fachdienst abrufen, weil das E-Rezept noch nicht akzeptiert ist

  @TCID:ERP_EE_WF160_27
  @TESTFALL:negativ
  @AFO-ID:A_24176
  @Hauptdarsteller:Apotheke
  Szenario: Abrufen des E-Rezepts nach Akzeptieren durch Apotheke A durch Apotheke B nicht möglich
  Die Versicherte schickt das Rezept via DMC zu einer Apotheke (A).
  Auf dem Weg dorthin geht sie in eine andere Apotheke (B) und weist dieser Das Rezept via DMC zu.
  Die Apotheke versucht das zugewiesene Rezept abzurufen. Das wird durch den Fachdienst verhindert.

    Wenn die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name             | PZN      |
      | Harnsäure Hemmer | 16588573 |
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Versicherte Sina Hüllmann ihr letztes ausgestellte E-Rezept der Apotheke Stadtapotheke via Data Matrix Code zuweist
    Dann kann die Apotheke Stadtapotheke das letzte zugewiesene E-Rezept nicht beim Fachdienst abrufen, weil das E-Rezept noch nicht akzeptiert ist

