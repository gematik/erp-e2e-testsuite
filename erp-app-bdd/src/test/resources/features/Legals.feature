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
@Rechtliches
Funktionalität: Einstellungen
  Alle Komponenten der Einstellung werden angezeigt und sind sichtbar.

  Grundlage: Rezept wird erzeugt, geladen und ist auf dem Mainscreen
    Angenommen die GKV Versicherte Alice öffnet das Onboarding

  @TCID:ERP_FDV_GKV_RECHTLICHES_01
  Szenario:  Verifikation Datenschutzerklärung und Nutzungsbedingungen während des Onboarding
    Wenn die Versicherte das Impressum während des Onboarding überprüfen möchte
    Dann sind die Nutzungsbedingungen und den Datenschutz sichtbar


  @TCID:ERP_FDV_GKV_RECHTLICHES_02
  Szenario: Verifikation von Einstellungen - Rechtliches
    Wenn die Versicherte das Impressum nach dem Onboarding überprüfen möchte
    Dann sind die Nutzungsbedingungen, der Datenschutz, das Impressum und die Lizenzen sichtbar



