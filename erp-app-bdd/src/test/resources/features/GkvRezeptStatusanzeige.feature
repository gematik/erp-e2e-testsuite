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

@PRODUKT:eRp_FdV
@iosNightly
@Versicherung:GKV
@Funktionalität:RezeptDarstellung
Funktionalität: Darstellung von GKV E-Rezepten
  Bei der Darstellung wird geprüft, ob sowohl Status- und Gültigkeitsanzeige als auch alle notwendigen Informationen
  zu einem GKV E-Rezept dem Versicherten korrekt dargestellt werden


  Grundlage: Rezept wird erzeugt, geladen und ist auf dem Mainscreen
    Angenommen die GKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet
    Und die Ärztin Adelheid Ulmenwald hat Zugriff auf seinen HBA und auf die SMC-B der Praxis

  @TCID:ERP_FDV_GKV_LÖSCHEN_01
  @Funktionalität:Löschen
  Szenario: Ausgestelltes GKV E-Rezept löschen ohne Einlösen
  Die GKV Versicherte löscht das E-Rezept ohne diese in einer Apotheke einzulösen

    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice ein apothekenpflichtiges Medikament verschreibt
    Und die Versicherte Alice ihr letztes E-Rezept in der App löscht
    Dann wird das letzte gelöschte E-Rezept der Versicherten in der App nicht mehr angezeigt

  @TCID:ERP_FDV_GKV_REZEPTINFORMATION_01
  @Funktionalität:RezeptInformationen
  Szenario: E-Rezept verschreiben und dem GKV Versicherten Anzeigen
  Ausgestellte E-Rezepte sind am Ausstellungstag für genau 27 Tage gültig

    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice ein apothekenpflichtiges Medikament verschreibt
    Dann wird der Versicherten Alice das letzte E-Rezept noch für 27 Tage einlösbar angezeigt

  @TCID:ERP_FDV_GKV_MEHRFACHVERORDNUNG_01
  @Funktionalität:MVO
  Szenario: Mehrfachverordnung für GKV-Versicherte
  Zwei E-Rezepte für GKV-Versicherte als Mehrfachverordnung

    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice folgende apothekenpflichtige Medikamente verschreibt:
      | MVO  | Numerator | Denominator | Gueltigkeitsstart | Gueltigkeitsende |
      | true | 1         | 2           | 0                 | 90               |
      | true | 2         | 2           | 90                | leer             |
    Dann wird der Versicherten Alice das letzte E-Rezept in der App angezeigt
    Und der Versicherten wird das erste E-Rezept in der App angezeigt
