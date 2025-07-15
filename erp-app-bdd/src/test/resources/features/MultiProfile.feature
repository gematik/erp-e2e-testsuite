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
Funktionalität: Mehrbenutzer mit App-Profilen

  @TCID:ERP_FDV_GKV_MULTIPROFIL_01
  @Funktionalität:MultiProfile
  Szenario: Zwei Benutzer teilen sich ein Gerät
    Angenommen die GKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet
    Und der GKV Versicherte Bob legt sich ein Profil in der E-Rezept App von Alice an
    Und die Ärztin Adelheid Ulmenwald hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice ein apothekenpflichtiges Medikament verschreibt
    Und die Ärztin Adelheid Ulmenwald dem Versicherten Bob ein apothekenpflichtiges Medikament verschreibt
    Dann wird der Versicherten Alice das letzte E-Rezept in der App angezeigt
    Dann wird dem Versicherten Bob das letzte E-Rezept in der App angezeigt