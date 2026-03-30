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
@Funktionalität:EURezepte
Funktionalität: Einlösen von EU Rezepten

  Grundlage: Die Versicherte bestellt ein E-Rezept per Nachricht bei einer Apotheke
    Angenommen die Ärztin Adelheid Ulmenwald hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und die Apotheke Adelheid Ulmendorfer hat Zugriff auf ihre SMC-B
    Und die GKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet

  @TCID:ERP_FDV_EU_EINLÖSEN_01
  Szenario: Bestellung eines E-Rezeptes für ein verschriebenes PZN-Medikament im EU Ausland
    Und die Versicherte Alice hat das EU Feature aktiviert
    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice ein apothekenpflichtiges Medikament verschreibt
    Dann kann die Versicherte Alice das letzte E-Rezept in Malta einlösen