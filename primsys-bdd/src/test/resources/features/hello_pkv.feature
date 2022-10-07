#
# Copyright (c) 2022 gematik GmbH
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

@Demo
@Impl=done
@Versicherung=PKV
Funktionalität: Demo Feature für PKV Tests

  Grundlage:
    Angenommen der Arzt Bernd Claudius hat Zugriff auf seinen HBA und auf die SMC-B der Praxis
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und der PKV Versicherte Fridolin Straßer hat Zugriff auf seine eGK
    Und der Versicherte hat seine Einwilligung zum Speichern der Abrechnungsinformationen widerrufen
    Und die PKV Versicherte Hanna Bäcker hat Zugriff auf ihre eGK
    Und die Versicherte hat ihre Einwilligung zum Speichern der Abrechnungsinformationen erteilt

  @Path=happy
  Szenario: Einwilligung zur Speicherung der Abrechnungsinformationen erteilen
  Fridolin erteilt seine Einwilligung zur Speicherung der Abrechnungsinformationen und Hanna hat die Einwilligung bereits aus
  der Vorbedingung
    Wenn der Versicherte Fridolin Straßer seine Einwilligung zur Speicherung der Abrechnungsinformationen erteilt
    Dann hat der Versicherte eine Einwilligung zur Speicherung der Abrechnungsinformationen beim E-Rezept Fachdienst
    Und die Versicherte Hanna Bäcker hat eine Einwilligung zur Speicherung der Abrechnungsinformationen beim E-Rezept Fachdienst

  @Path=happy
  Szenario: Abrechnungsinformation für ein dispensiertes Medikament abrufen
  Die Apotheke dispensiert ein Medikament an einen PKV-Versicherten, der seine Einwilligung erteilt hat
    Wenn der Arzt Bernd Claudius folgendes E-Rezept an den Versicherten Fridolin Straßer verschreibt:
      | Name          | PZN      | Substitution |
      | Schmerzmittel | 12345678 | false        |
    Und der Versicherte Fridolin Straßer sein letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Fridolin Straßer dispensiert
    Dann hat der Versicherte Fridolin Straßer genau 1 Medikament erhalten
    Und der Versicherte hat eine Abrechnungsinformation für das letzte dispensierte Medikament beim Fachdienst vorliegen
