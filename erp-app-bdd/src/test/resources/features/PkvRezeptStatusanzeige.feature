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

@Versicherung:PKV
@Funktionalität:RezeptDarstellung
Funktionalität: Darstellung von PKV E-Rezepten
  Bei der Darstellung wird geprüft, ob sowohl Status- und Gültigkeitsanzeige als auch alle notwendigen Informationen
  zu einem PKV E-Rezept dem Versicherten korrekt dargestellt werden


  Grundlage: Rezept wird erzeugt, geladen und ist auf dem Mainscreen
    Angenommen die PKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet
#    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und der Arzt Bernd Claudius hat Zugriff auf seinen HBA und auf die SMC-B der Praxis

  @iosNightly
  @Funktionalität:Löschen
  Szenario: Ausgestelltes PKV E-Rezept löschen ohne Einlösen
  Die PKV Versicherte löscht das E-Rezept ohne diese in einer Apotheke einzulösen

    Wenn der Arzt Bernd Claudius der Versicherten Alice ein apothekenpflichtiges Medikament verschreibt
    Und die Versicherte Alice ihr letztes E-Rezept in der App löscht
    Dann wird das letzte gelöschte E-Rezept der Versicherten in der App nicht mehr angezeigt

  @iosNightly
  @Funktionalität:RezeptInformationen
  Szenario: E-Rezept verschreiben und dem PKV Versicherten Anzeigen
  Der Arzt verschreibt der PKV-Versicherten ein E-Rezept für ein apothekenpflichtiges Rezept.
  Dieses E-Rezept soll in der App korrekt angezeigt werden.

    Wenn der Arzt Bernd Claudius der Versicherten Alice ein apothekenpflichtiges Medikament verschreibt
    Dann wird der Versicherten Alice das letzte E-Rezept in der App angezeigt

  @iosNightly
  @Funktionalität:MVO
  Szenario: Mehrfachverordnung für PKV-Versicherte
  Zwei E-Rezepte für PKV-Versicherte als Mehrfachverordnung

    Wenn die Ärztin Bernd Claudius der Versicherten Alice folgende apothekenpflichtige Medikamente verschreibt:
      | MVO  | Numerator | Denominator | Gueltigkeitsstart | Gueltigkeitsende |
      | true | 1         | 2           | 0                 | 90               |
      | true | 2         | 2           | 90                | leer             |
    Dann wird der Versicherten Alice das letzte E-Rezept in der App angezeigt
    Und der Versicherten wird das erste E-Rezept in der App angezeigt
