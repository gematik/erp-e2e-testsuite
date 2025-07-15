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

@PRODUKT:eRp_FdV
@iosNightly
@Funktionalität:RezeptBestellung
Funktionalität: E-Rezepte per Nachricht bestellen

  Grundlage: Die Versicherte bestellt ein E-Rezept per Nachricht bei einer Apotheke
    Angenommen die Ärztin Adelheid Ulmenwald hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und die Apotheke Adelheid Ulmendorfer hat Zugriff auf ihre SMC-B

  @TCID:ERP_FDV_BESTELLEN_01
  Szenario: Zuweisung per Nachricht
  Der Versicherte weist das gerade erhaltene E-Rezept per Nachricht einer Apotheke zu.
  Diese kann das Rezept erfolgreich akzeptieren und einlösen.

    Angenommen die GKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet
    Und die Ärztin Adelheid Ulmenwald hat der Versicherten Alice ein apothekenpflichtiges Medikament verschrieben
    Wenn die Versicherte Alice ihr letztes E-Rezept in der App der Apotheke Adelheid Ulmendorfer per Nachricht zuweist
    Und die Apotheke Adelheid Ulmendorfer die letzte Zuweisung per Nachricht von Alice akzeptiert
    Und die Apotheke Adelheid Ulmendorfer das letzte akzeptierte E-Rezept korrekt an Alice dispensiert
    Dann hat die Versicherte Alice das letzte E-Rezept elektronisch erhalten
    Und die Apotheke Adelheid Ulmendorfer hat genau 1 Quittung vorliegen