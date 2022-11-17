#
# Copyright (c) 2022 gematik GmbH
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

@Demo
@Impl=done
Funktionalität: Hello World Demo Feature

  Grundlage:
    Angenommen der Arzt Bernd Claudius hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und die Ärztin Gündüla Gunther hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und die Apotheke NordApotheke hat Zugriff auf ihre SMC-B
    Und der GKV Versicherte Fridolin Straßer hat Zugriff auf seine eGK
    Und die PKV Versicherte Hanna Bäcker hat Zugriff auf ihre eGK

  @Path=happy
  @Versicherung=GKV
  Szenario: Quittung für ein dispensiertes PKV Rezept abrufen
  Der Arzt Claudius verschreibt Fridolin ein E-Rezept, welches dieser bei der Apotheke "Am Flughafen" einlöst.
  Die Apotheke "Am Flughafen" hat daraufhin eine Quittung für das dispensierte Medikament.

    Wenn der Arzt Bernd Claudius folgendes E-Rezept an den Versicherten Fridolin Straßer verschreibt:
      | Name          | PZN      | Substitution | Verordnungskategorie | Normgröße | Darreichungsform | Dosierung | Menge | Notdienstgebühr | Zahlungsstatus |
      | Schmerzmittel | 12345678 | false        | 00                   | N1        | TAB              | 1-0-0-1   | 3     | false           | 0              |
    Und der Versicherte Fridolin Straßer sein letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke das letzte akzeptierte E-Rezept korrekt dispensiert
    Dann hat die Apotheke mindestens eine Quittung vorliegen
    Und die Apotheke hat maximal 2 Quittungen vorliegen
    Und die Apotheke hat mindestens 1 Quittung für Fridolin Straßer vorliegen

  @Path=happy
  @Versicherung=GKV
  @Funktionalität=mehrereMedicationDispense
  Szenario: Dispensierung des E-Rezeptes mit mehreren Medikamenten
  Die Apotheke dispensiert das akzeptierte E-Rezept über mehrere Ersatzpräparate.
  In diesem Szenario verschreibt der Arzt eine große Packung (N3) mit 50 Stk Inhalt.
  Die Apotheke dispensiert hingegen eine kleine (N1) Packung mit 10 Stk und zwei mittlere (N2) mit 20 Stk Inhalt.
  Die Dispensierung der drei Packungen erzeugt nur eine Quittung.

    Wenn der Arzt Bernd Claudius folgendes E-Rezept an den Versicherten Fridolin Straßer verschreibt:
      | Name         | PZN      | Substitution | Normgröße | Darreichungsform | Dosierung | Menge |
      | IBUFLAM akut | 11648419 | true         | N3        | FTA              | 1-0-0-1   | 50    |
    Und der Versicherte Fridolin Straßer sein letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke das letzte akzeptierte E-Rezept mit den folgenden Medikamenten korrekt dispensiert:
      | Name         | PZN      | Nomgröße | Menge | Einheit | Darreichungsform |
      | IBUFLAM akut | 04100230 | N1       | 10    | Stk     | FTA              |
      | IBUFLAM akut | 04100218 | N2       | 20    | Stk     | FTA              |
      | IBUFLAM akut | 04100218 | N2       | 20    | Stk     | FTA              |
    Dann hat die Apotheke genau 1 Quittung für Fridolin Straßer vorliegen

  @Path=happy
  @Versicherung=GKV
  Szenario: Verschreibung eines E-Rezeptes nur über KVNR
    Wenn der Arzt Bernd Claudius folgendes E-Rezept an die KVNR X123456789 verschreibt:
      | Name          | PZN      | Substitution |
      | Schmerzmittel | 12345678 | false        |
    Dann hat der Versicherte Fridolin Straßer noch kein E-Rezept über DMC erhalten

  @Path=bad
  @Versicherung=GKV
  Szenario: Dispensierung eines nicht akzeptierten E-Rezeptes
  Die Apotheke Am Flughafen versucht fälschlicherweise eine E-Rezept zu dispensieren.
  Das E-Rezept wurde der Apotheke zwar physisch bereits zugewiesen aber diese hat das E-Rezept beim Fachdienst
  noch nicht akzeptiert.

    Wenn der Arzt Bernd Claudius ein E-Rezept an den Versicherten Fridolin Straßer verschreibt
    Und der Versicherte Fridolin Straßer sein letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Dann kann die Apotheke noch kein E-Rezept dispensieren

  @Path=bad
  @Versicherung=GKV
  Szenario: Dispensierung eines E-Rezeptes an einen anderen Versicherten
  Die Apotheke Am Flughafen versucht ein E-Rezept an einen Versicherten mit einer anderen KVNR zu dispensieren.

    Wenn der Arzt Bernd Claudius ein E-Rezept an den Versicherten Fridolin Straßer verschreibt
    Und der Versicherte Fridolin Straßer sein letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke kann das letzte akzeptierte E-Rezept nicht an den Versicherten mit KVNR X123456789 dispensieren
    Und die Apotheke kann das letzte akzeptierte E-Rezept korrekt dispensieren

  @Path=bad
  @Versicherung=GKV
  Szenario: Mehrfache Dispensierung eines einzelnen E-Rezeptes
  Die Apotheke Am Flughafen versucht ein E-Rezept mehrfach falsch zu dispensieren.

    Wenn der Arzt Bernd Claudius ein E-Rezept an den Versicherten Fridolin Straßer verschreibt
    Und der Versicherte Fridolin Straßer sein letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke das letzte akzeptierte E-Rezept nicht mit dem falschen Secret abc123 dispensieren
    Und die Apotheke kann das letzte akzeptierte E-Rezept nicht an den Versicherten mit KVNR X123456789 dispensieren
    Aber die Apotheke kann das letzte akzeptierte E-Rezept korrekt dispensieren

  @Path=happy
  @Versicherung=All
  Szenario: E-Rezept an einen Vertreter senden
  Der Versicherte sendet sein E-Rezept an einen Vertreter
    Wenn der Arzt Bernd Claudius ein E-Rezept an den Versicherten Fridolin Straßer verschreibt
    Und der Versicherte Fridolin Straßer sein letztes E-Rezept per Nachricht an die Vertreterin Hanna Bäcker schickt
    Dann hat die Vertreterin Hanna Bäcker die Nachricht mit dem Rezept des Versicherten Fridolin Straßer empfangen


#  Szenario: Verbinde PSP-Client mit PSP-Server
#    Angenommen die Apotheke Am Flughafen verbindet sich mit seinem Apothekendienstleister
#    Wenn die Apotheke Am Flughafen eine Nachricht mit einer alternativen Zuweisung vom Dienstleister empfängt und entschlüsselt
