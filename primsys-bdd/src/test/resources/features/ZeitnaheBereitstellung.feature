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
@Workflow:160
@Funktionalität:ZeitnaheBereitstellung
@Versicherung:GKV
Funktionalität: Zeitnahe Bereitstellung von Dispensierinformationen für Apothekenpflichtige Medikamente für GKV-Versicherte

  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die GKV Versicherte Sina Hüllmann hat Zugriff auf ihre eGK
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und die Ärztin Dr. Schraßer der Versicherten Sina Hüllmann folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |
    Dann wird der Versicherten Sina Hüllmann das neue E-Rezept angezeigt
    Und die Versicherte Sina Hüllmann ihr letztes ausgestelltes E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist

  @TCID:ERP_EE_ZBD_01
  @TESTFALL:positiv
  Szenario: Zeitnahe Bereitstellung von Dispensierinformationen für das FdV
    Wenn die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen für das letzte akzeptierte E-Rezept von Sina Hüllmann die Dispensierinformationen zeitnah bereitstellt
    Dann kann die Versicherte Sina Hüllmann 1 Dispensierinformationen für ihr letztes E-Rezept abrufen
    Und die Versicherte Sina Hüllmann kann im FdV einsehen, dass ihr letztes E-Rezept von einer Apotheke abgegeben wurde
    Und die Versicherte Sina Hüllmann kann im Protokoll für ihr letztes E-Rezept einsehen, dass es von der Apotheke Am Flughafen abgegeben wurde
    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept den Workflow abschliessen
    Dann hat die Apotheke Am Flughafen genau 1 Quittung vorliegen

  @TCID:ERP_EE_ZBD_02
  @TESTFALL:positiv
  Szenario: Mehrfache zeitnahe Bereitstellung von Dispensierinformationen für das FdV
    Wenn die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen für das letzte akzeptierte E-Rezept von Sina Hüllmann die Dispensierinformationen mehrfach zeitnah bereitstellt
    Dann kann die Versicherte Sina Hüllmann genau die letzte bereitgestellte Dispensierinformation der Apotheke Am Flughafen für ihr letztes E-Rezept abrufen
    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept den Workflow abschliessen

  @TCID:ERP_EE_ZBD_03
  @TESTFALL:positiv
  Szenario: Änderungen von Dispensierinformationen nach Abschluss des Workflows nicht möglich
    Wenn die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen für das letzte akzeptierte E-Rezept von Sina Hüllmann die Dispensierinformationen zeitnah bereitstellt
    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept den Workflow abschliessen
    Dann kann die Apotheke Am Flughafen die Dispensierinformationen für das letzte E-Rezept von Sina Hüllmann nicht mehr ändern

  @TCID:ERP_EE_ZBD_04
  @TESTFALL:positiv
  Szenario: Bereitgestellte Dispensierinformationen beim Abschluss des Workflows überschreiben vorherige bereitgestellte Dispensierinformationen
    Wenn die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen für das letzte akzeptierte E-Rezept von Sina Hüllmann die Dispensierinformationen zeitnah bereitstellt
    Dann kann die Apotheke Am Flughafen das letzte E-Rezept für Sina Hüllmann beim Abschluss des Workflows die Dispensierinformationen ändern in:
      | Name         | PZN      | Normgröße | Menge | Einheit | Darreichungsform |
      | IBUFLAM akut | 04100230 | N1        | 10    | Stk     | FTA              |
    Dann kann die Versicherte Sina Hüllmann genau die letzte bereitgestellte Dispensierinformation der Apotheke Am Flughafen für ihr letztes E-Rezept abrufen

  @TCID:ERP_EE_ZBD_05
  @TESTFALL:positiv
  Szenario: Zeitnahe Bereitstellung von Dispensierinformationen erst nach akzeptieren des E-Rezepts möglich
    Wenn die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst nicht akzeptiert
    Dann kann die Apotheke Am Flughafen für das letzte E-Rezept von Sina Hüllmann keine Dispensierinformationen zeitnah bereitstellen


  @TCID:ERP_EE_ZBD_06
  @TESTFALL:positiv
  Szenario: Zeitnahe Bereitstellung von Dispensierinformationen werden zurückgezogen, wenn das E-Rezept wieder freigegeben wurde
    Wenn die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen für das letzte akzeptierte E-Rezept von Sina Hüllmann die Dispensierinformationen zeitnah bereitstellt
    Und die Apotheke Am Flughafen das letzte akzeptierte Rezept zurückweist
    Dann kann die Versicherte Sina Hüllmann 0 Dispensierinformationen für ihr letztes E-Rezept abrufen

  @TCID:ERP_EE_ZBD_07
  @TESTFALL:negativ
  Szenario: Zeitnahe Bereitstellung von Dispensierinformationen werden Vergessen einzustellen, sodass der Patient keine informationen zur verfügung hat
    Wenn die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept den Workflow nicht abschliessen




