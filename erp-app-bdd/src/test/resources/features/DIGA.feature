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
# *******
# For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
# *******
#
# language: de


@PRODUKT:eRp_FdV
@Funktionalität:Digitale_Gesundheitsanwendungen
Funktionalität: DiGA Anwendungsfälle in der App

  Grundlage:
    Angenommen der Kostenträger AOK Bremen hat Zugriff auf seine SMC-B KTR
    Und die GKV Versicherte Alice hat die E-Rezept App auf ihrem Smartphone eingerichtet
    Und die Ärztin Adelheid Ulmenwald hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    # Und die GKV Versicherte Alice hat Zugriff auf ihre eGK

@TCID:ERP_FDV_GKV_DIGA_HAPPY_01
  Szenario: Empfang, Anfrage und Erhalt einer Digitalen Gesundheitsanwendung
  Wenn die Ärztin Adelheid Ulmenwald der Versicherten Alice ein EVDGA E-Rezept verschreibt
  Und die GKV Versicherte Alice ihr letztes ausgestelltes EVDGA E-Rezept in der App anzeigen kann
  Und die GKV Versicherte Alice ihr letztes ausgestelltes EVDGA E-Rezept per App ihrem Kostenträger AOK Bremen zuweist
  Und der Kostenträger AOK Bremen das letzte EVDGA der Versicherten Alice akzeptiert
  Und der Kostenträger AOK Bremen kann für die letzte EVDGA der Versicherten Alice Abgabeinformationen mit Freischaltcode bereitstellen
  Und die GKV Versicherte Alice kann den Freischaltcode für ihre letzte bereitgestellte EVDGA per App abrufen
  Und die GKV Versicherte Alice kann die letzte bereitgestellte EVDGA in der App löschen
  Dann wird das letzte gelöschte EVDGA E-Rezept der Versicherten in der App nicht mehr angezeigt
