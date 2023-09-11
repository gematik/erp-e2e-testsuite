#
# Copyright (c) 2023 gematik GmbH
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

@PRODUKT:eRp_FD
@Demo
Funktionalität: Hello World Demo Feature

  Grundlage:
    Angenommen der Arzt Bernd Claudius hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und die Ärztin Gündüla Gunther hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und die Apotheke NordApotheke hat Zugriff auf ihre SMC-B
    Und der GKV Versicherte Fridolin Straßer hat Zugriff auf seine eGK
    Und die PKV Versicherte Hanna Bäcker hat Zugriff auf ihre eGK


  @TCID:ERP_EE_GKV_DEMO_01
  @TESTFALL:positiv
  @Versicherung:GKV
  @Funktionalität:mehrereMedicationDispense
  @Hauptdarsteller:Apotheke
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
    Und die Apotheke das letzte akzeptierte E-Rezept mit den folgenden Medikamenten korrekt an Fridolin Straßer dispensiert:
      | Name         | PZN      | Nomgröße | Menge | Einheit | Darreichungsform |
      | IBUFLAM akut | 04100230 | N1       | 10    | Stk     | FTA              |
      | IBUFLAM akut | 04100218 | N2       | 20    | Stk     | FTA              |
      | IBUFLAM akut | 04100218 | N2       | 20    | Stk     | FTA              |
    Dann hat die Apotheke Am Flughafen genau 1 Quittung für Fridolin Straßer vorliegen

  @TCID:ERP_EE_GKV_DEMO_02
  @TESTFALL:positiv
  @Versicherung:GKV
  @Hauptdarsteller:Arztpraxis
  Szenario: Verschreibung eines E-Rezeptes nur über KVNR
    Wenn der Arzt Bernd Claudius folgendes E-Rezept an die KVNR X123456789 verschreibt:
      | Name          | PZN      | Substitution |
      | Schmerzmittel | 12345678 | false        |
    Dann hat der Versicherte Fridolin Straßer noch kein E-Rezept über DMC erhalten


  @TCID:ERP_EE_GKV_DEMO_03
  @TESTFALL:positiv
  @Hauptdarsteller:Versicherter
  Szenario: E-Rezept an einen Vertreter senden
  Der Versicherte sendet sein E-Rezept an einen Vertreter
    Wenn der Arzt Bernd Claudius ein E-Rezept an den Versicherten Fridolin Straßer verschreibt
    Und der Versicherte Fridolin Straßer sein letztes E-Rezept per Nachricht an die Vertreterin Hanna Bäcker schickt
    Dann hat die Vertreterin Hanna Bäcker die Nachricht mit dem Rezept des Versicherten Fridolin Straßer empfangen
