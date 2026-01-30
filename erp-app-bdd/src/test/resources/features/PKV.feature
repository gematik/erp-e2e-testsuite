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
@iosNightly
@Versicherung:PKV
@Funktionalität:Kostenbeleg
Funktionalität: Kostenbeleg und Einwilligung

  Grundlage:
    Angenommen die Ärztin Adelheid Ulmenwald hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die Apotheke Adelheid Ulmendorfer hat Zugriff auf ihre SMC-B
    Und die PKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet

  @TCID:ERP_FDV_PKV_04
  Szenario: Kostenbelege und Einwilligung verwalten
    Und die Versicherte Alice kann Kostenbelege verwalten
    Wenn die Versicherte Alice ihre Einwilligung zum Speichern der PKV-Abrechnungsinformationen über die App erteilt
    Und die Ärztin Adelheid Ulmenwald der Versicherten Alice folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | Workflow |
      | Schmerzmittel | 200      |
    Und die Versicherte Alice ihr letztes E-Rezept in der App der Apotheke Adelheid Ulmendorfer per Nachricht zuweist
    Und die Apotheke Adelheid Ulmendorfer die letzte Zuweisung per Nachricht von Alice akzeptiert
    Und die Apotheke Adelheid Ulmendorfer das letzte akzeptierte E-Rezept korrekt an Alice dispensiert
    Und die Apotheke Adelheid Ulmendorfer für das letzte dispensierte E-Rezept die PKV-Abrechnungsinformationen bereitstellt
    Dann kann die Versicherte Alice den Kostenbeleg für das letzte E-Rezept in der App einsehen
    Und kann die Versicherte Alice den Kostenbeleg für das letzte E-Rezept in der App löschen
    Und kann die Versicherte Alice ihre Einwilligung zum Speichern der PKV-Abrechnungsinformationen über die App widerrufen