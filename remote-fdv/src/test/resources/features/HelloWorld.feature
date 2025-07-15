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
# *******
# For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
# *******
#
# language: de

@Funktionalität:Dispensierung
Funktionalität: E-Rezepte dispensieren
  Dispensierte E-Rezepte im Rezeptarchiv ablegen

  Grundlage: Rezept wird erzeugt, geladen und ist auf dem Mainscreen
    #Angenommen die Ärztin Adelheid Ulmenwald hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    #Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B

  @TCID:ERP_FDV_GKV_STANDARDDISPENSIERUNG_01
  @Funktionalität:Standarddispensierung
  Szenario: Dispensierung des E-Rezeptes mit dem verschriebenen Medikament
    Angenommen die GKV Versicherte Alice hat die Remote-FdV auf ihrem Smartphone eingerichtet
    #Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice ein apothekenpflichtiges Medikament verschreibt
    #Wenn die Versicherte Alice ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    #Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    #Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Alice dispensiert
    #Dann hat die Apotheke Am Flughafen genau 1 Quittung für Alice vorliegen
    Dann hat die Versicherte Alice das letzte E-Rezept elektronisch erhalten

  @TCID:ERP_FDV_GKV_MEHRFACHDISPENSIERUNG_01
  @Funktionalität:Mehrfachdispensierung
  Szenario: Dispensierung des E-Rezeptes mit mehreren Medikamenten
  Die Apotheke dispensiert das akzeptierte E-Rezept über mehrere Ersatzpräparate.
  In diesem Szenario verschreibt der Arzt eine große Packung (N3) mit 50 Stk Inhalt.
  Die Apotheke dispensiert hingegen eine kleine (N1) Packung mit 10 Stk und zwei mittlere (N2) mit 20 Stk Inhalt.
  Die Dispensierung der drei Packungen erzeugt nur eine Quittung.

    Angenommen die GKV Versicherte Alice hat die Remote-FdV auf ihrem Smartphone eingerichtet
    #Wenn die Ärztin Adelheid Ulmenwald folgendes E-Rezept an die Versicherte Alice verschreibt:
      # | Name         | PZN      | Kategorie | Substitution | Normgröße | Darreichungsform | Dosierung | Menge |
      # | IBUFLAM akut | 11648419 | 00        | true         | N3        | FTA              | 1-0-0-1   | 50    |
    #Wenn die Versicherte Alice ihr letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    #Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    #Und die Apotheke das letzte akzeptierte E-Rezept mit den folgenden Medikamenten korrekt an Alice dispensiert:
     # | Name         | PZN      | Kategorie | Normgröße | Menge | Einheit | Darreichungsform |
     # | IBUFLAM akut | 04100230 | 00        | N1        | 10    | Stk     | FTA              |
     # | IBUFLAM akut | 04100218 | 00        | N2        | 20    | Stk     | FTA              |
     # | IBUFLAM akut | 04100218 | 00        | N2        | 20    | Stk     | FTA              |
    #Dann hat die Apotheke Am Flughafen genau 1 Quittung für Alice vorliegen
    Dann hat die Versicherte Alice das letzte E-Rezept elektronisch erhalten

  @TCID:ERP_FDV_GKV_DIREKTZUWEISUNG_01
  @Versicherung:GKV
  Szenario: Anzeigen von direkt zugewiesenen E-Rezepten für GKV-Versicherte
  Der Arzt verschreibt der GKV-Versicherten ein Medikament als Direktzuweisung.
  Dieses E-Rezept soll im FdV angezeigt werden.

    Angenommen die GKV Versicherte Alice hat die Remote-FdV auf ihrem Smartphone eingerichtet
   # Wenn die Ärztin Dr. Schraßer der Versicherten Alice ein Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist
    Und der Versicherten Alice das letzte E-Rezept in der App angezeigt wird
   # Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    #Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensiert
    #Dann hat die Apotheke genau eine Quittung vorliegen

  @TCID:ERP_FDV_GKV_DIREKTZUWEISUNG_02
  @Versicherung:PKV
  Szenario: Anzeigen von direkt zugewiesenen E-Rezepten für PKV-Versicherte
  Der Arzt verschreibt der PKV-Versicherten ein Medikament als Direktzuweisung.
  Dieses E-Rezept soll im FdV angezeigt werden.

    Angenommen die PKV Versicherte Alice hat die Remote-FdV auf ihrem Smartphone eingerichtet
    #Wenn die Ärztin Dr. Schraßer der Versicherten Alice ein Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist
    Und der Versicherten Alice das letzte E-Rezept in der App angezeigt wird
    #Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    #Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt dispensiert
    #Dann hat die Apotheke genau eine Quittung vorliegen

  @TCID:ERP_FDV_GKV_DIREKTZUWEISUNG_03
  @Funktionalität:Löschen
  @Versicherung:GKV
  Szenario: Anzeigen und  löschen von direkt zugewiesenen E-Rezepten für GKV-Versicherte
  Der Arzt verschreibt der GKV-Versicherten ein Medikament als Direktzuweisung.
  Dieses E-Rezept soll im FdV angezeigt werden.
  Die GKV Versicherte soll das E-Rezept nicht löschen können.
    Angenommen die GKV Versicherte Alice hat die Remote-FdV auf ihrem Smartphone eingerichtet
    #Wenn die Ärztin Dr. Schraßer der Versicherten Alice ein Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist
    #Und die Versicherte Alice ihre E-Rezepte abruft TODO needs to be refactored!
    Dann kann die Versicherte Alice ihr letztes E-Rezept in der App nicht löschen
    Und der Versicherten wird das letzte E-Rezept in der App angezeigt

  @TCID:ERP_FDV_GKV_DIREKTZUWEISUNG_04
  @Funktionalität:Löschen
  @Versicherung:PKV
  Szenario: Anzeigen und  löschen von direkt zugewiesenen E-Rezepten für PKV-Versicherte
  Der Arzt verschreibt der PKV-Versicherten ein Medikament als Direktzuweisung.
  Dieses E-Rezept soll im FdV angezeigt werden.
  Die PKV Versicherte soll das E-Rezept nicht löschen können.
    Angenommen die PKV Versicherte Alice hat die Remote-FdV auf ihrem Smartphone eingerichtet
    #Wenn die Ärztin Dr. Schraßer der Versicherten Alice ein Medikament verschreibt und der Apotheke Am Flughafen direkt zuweist
    #Und die Versicherte Alice ihre E-Rezepte abruft TODO needs to be refactored!
    Dann kann die Versicherte Alice ihr letztes E-Rezept in der App nicht löschen
    Und der Versicherten wird das letzte E-Rezept in der App angezeigt

  @TCID:ERP_FDV_GKV_LÖSCHEN_01
  @Funktionalität:Löschen
  Szenario: Ausgestelltes GKV E-Rezept löschen ohne Einlösen
  Die GKV Versicherte löscht das E-Rezept ohne diese in einer Apotheke einzulösen
    Angenommen die GKV Versicherte Alice hat die Remote-FdV auf ihrem Smartphone eingerichtet
    #Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice ein apothekenpflichtiges Medikament verschreibt
    ##Dann wird der Versicherten Alice das letzte E-Rezept noch für 27 Tage einlösbar angezeigt
    Und die Versicherte Alice ihr letztes E-Rezept in der App löscht
    Dann wird das letzte gelöschte E-Rezept der Versicherten in der App nicht mehr angezeigt

  @TCID:ERP_FDV_GKV_MEHRFACHVERORDNUNG_01
  @Funktionalität:MVO
  @Funktionalität:Löschen
  Szenario: Mehrfachverordnung für GKV-Versicherte
  Zwei E-Rezepte für GKV-Versicherte als Mehrfachverordnung

    Angenommen die GKV Versicherte Alice hat die Remote-FdV auf ihrem Smartphone eingerichtet
    #Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice folgende apothekenpflichtige Medikamente verschreibt:
    #  | MVO  | Numerator | Denominator | Gueltigkeitsstart | Gueltigkeitsende |
    #  | true | 1         | 2           | 0                 | 90               |
    #  | true | 2         | 2           | 90                | leer             |
    Dann wird der Versicherten Alice das letzte E-Rezept in der App angezeigt
    Und der Versicherten wird das erste E-Rezept in der App angezeigt
    Und die Versicherte ihr erstes E-Rezept in der App löscht
    Dann wird das erste gelöschte E-Rezept der Versicherten in der App nicht mehr angezeigt

  @TCID:ERP_FDV_BESTELLEN_01
  Szenario: Zuweisung per Nachricht
  Der Versicherte weist das gerade erhaltene E-Rezept per Nachricht einer Apotheke zu.
  Diese kann das Rezept erfolgreich akzeptieren und einlösen.

    Angenommen die GKV Versicherte Alice hat die Remote-FdV auf ihrem Smartphone eingerichtet
    #Und die Ärztin Adelheid Ulmenwald hat der Versicherten Alice ein apothekenpflichtiges Medikament verschrieben
    Wenn die Versicherte Alice ihr letztes E-Rezept in der App der Apotheke Am Flughafen per Nachricht zuweist
    #Und die Apotheke Am Flughafen die letzte Zuweisung per Nachricht von Alice akzeptiert
    #Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Alice dispensiert
    Dann hat die Versicherte Alice das letzte E-Rezept elektronisch erhalten
    #Und die Apotheke Am Flughafen hat genau 1 Quittung vorliegen