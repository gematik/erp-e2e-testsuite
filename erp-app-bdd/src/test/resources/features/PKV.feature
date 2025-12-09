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
#
# *******
# For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
# *******
#
# language: de


@PRODUKT:eRp_FdV
@Funktionalität:PKV
Funktionalität: PKV Tests in der App

  Grundlage:
    Angenommen die Ärztin Adelheid Ulmenwald hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die Apotheke Adelheid Ulmendorfer hat Zugriff auf ihre SMC-B
    Und die PKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet
    Und die Versicherte Alice hat ihre Einwilligung zum Speichern der PKV-Abrechnungsinformationen erteilt


  @TCID:ERP_FDV_PKV_Order_01
  @Versicherung:PKV
  Szenario: Bestellen eines PKV-Rezepts

  @TCID:ERP_FDV_PKV_DIREKTZUWEISUNG_02
  @Versicherung:PKV
  Szenario: Anzeigen von direkt zugewiesenen E-Rezepten für PKV-Versicherte
  Der Arzt verschreibt der PKV-Versicherten ein Medikament als Direktzuweisung.
  Dieses E-Rezept soll in der App angezeigt werden.

    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice ein Medikament verschreibt und der Apotheke Adelheid Ulmendorfer direkt zuweist
    Und der Versicherten Alice das letzte E-Rezept in der App angezeigt wird
    Und die Apotheke Adelheid Ulmendorfer das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    #TODO Add: Und das letzte E-Rezept den Status: "Wird für sie eingelöst" hat
    Und die Apotheke Adelheid Ulmendorfer für das letzte akzeptierte E-Rezept von Alice die Dispensierinformationen zeitnah bereitstellt
    #TODO Add: Und das letzte E-Rezept den Status: "Wird für sie eingelöst" hat
    Dann kann die Apotheke Adelheid Ulmendorfer für das letzte dispensierte E-Rezept den Workflow abschliessen
    Dann hat die Versicherte Alice das letzte E-Rezept elektronisch erhalten
    Dann hat die Apotheke genau eine Quittung vorliegen

  @TCID:ERP_FDV_PKV_DIREKTZUWEISUNG_Löschen_03
  @Funktionalität:Löschen
  @Versicherung:PKV
  Szenario: Anzeigen und löschen von direkt zugewiesenen E-Rezepten für PKV-Versicherte
  Der Arzt verschreibt der PKV-Versicherten ein Medikament als Direktzuweisung.
  Dieses E-Rezept soll in der App angezeigt werden.
  Die PKV Versicherte soll das E-Rezept nicht löschen können.
    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice ein Medikament verschreibt und der Apotheke Adelheid Ulmendorfer direkt zuweist
    Und der Versicherten Alice das letzte E-Rezept in der App angezeigt wird
    Dann kann die Versicherte Alice ihr letztes E-Rezept in der App nicht löschen
    Und der Versicherten wird das letzte E-Rezept in der App angezeigt

  @TCID:ERP_FDV_PKV_Apothekenrechnung_04
  @Versicherung:PKV
  Szenario: Anzeigen einer Apothekenrechnung

    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | Workflow |
      | Schmerzmittel | 200      |
    Und die Versicherte Alice ihr letztes ausgestelltes E-Rezept der Apotheke Adelheid Ulmendorfer via Data Matrix Code zuweist
    Und die Apotheke Adelheid Ulmendorfer das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Adelheid Ulmendorfer das letzte akzeptierte E-Rezept korrekt an Alice dispensiert
    Und die Apotheke Adelheid Ulmendorfer für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    # Dann kann die PKV Versicherte Alice die Abrechnungsinformationen für das letzte dispensierte E-Rezept in der App einsehen

  @TCID:ERP_FDV_PKV_Apothekenrechnung_Löschen_05
  @Versicherung:PKV
  Szenario: Löschen einer Apothekenrechnung

    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | Workflow |
      | Schmerzmittel | 200      |
    Und die Versicherte Alice ihr letztes ausgestelltes E-Rezept der Apotheke Adelheid Ulmendorfer via Data Matrix Code zuweist
    Und die Apotheke Adelheid Ulmendorfer das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Adelheid Ulmendorfer das letzte akzeptierte E-Rezept korrekt an Alice dispensiert
    Und die Apotheke Adelheid Ulmendorfer für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    # Dann kann die PKV Versicherte Alice die Abrechnungsinformationen für das letzte dispensierte E-Rezept in der App einsehen
    # Und die PKV Versicherte Alice kann die Abrechnungsinformationen für das letzte dispensierte E-Rezept in der App löschen

  @TCID:ERP_FDV_PKV_Einwilligung_Entziehen_06
  @Versicherung:PKV
  Szenario: Entziehen der Einwilligung, Abrechnungsinformationen zu speichern

  # Wenn die PKV Versicherte Alice die Einwillung zum Speichern von Abrechnungsinformationen in der App entzieht
  # add new step here
  Dann hat die Versicherte Alice ihre Einwilligung zum Speichern der PKV-Abrechnungsinformationen nicht erteilt
  # this step needs to get new cucumber step code
  # Und die Versicherte Alice hat keine Abrechnungsinformationen in der App zur Verfügung (alternativ vorher ein chargeitem
  # erstellen und dann gucken dass es nicht mehr existiert)

  # add test for giving consent?