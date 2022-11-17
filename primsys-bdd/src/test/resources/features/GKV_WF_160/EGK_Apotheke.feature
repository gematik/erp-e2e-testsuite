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

@Funktionalität=EGKinApotheke
@Workflow=160
@Versicherung=GKV
@Impl=open
Funktionalität: eGK in der Apotheke
  Der Versicherte soll durch die Vorlage seiner EGK eine Apotheke dazu berechtigen,
  seine einlösbaren E-Rezepte aus dem E-Rezept-Fachdienst abrufen zu können. Aus technischer Sicht ist es dabei
  unerheblich, ob es sich um einen GKV- oder PKV-Versicherten handelt.

  Grundlage: Die Ärztin Dr. Straßer verschreibt der Versicherten Aenna Gondern ein E-Rezept als Grundlage für
  die Testszenarien
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und die GKV Versicherte Aenna Gondern hat Zugriff auf ihre eGK
    Und die Ärztin Dr. Schraßer dem Versicherten Aenna Gondern folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      |
      | Schmerzmittel | 12345678 |

  @TCID=ERP_EE_WF160_EGK_APOTHEKE_01
  @Path=happy
  @Afo=A_21558-1
  @Afo=A_22431
  Szenario: E-Rezept mit eGK und gültigem Prüfungsnachweis in der Apotheke einlösen
  Ein GKV Versicherter will seine E-Rezepte mit seiner EGK in einer
  öffentlichen Apotheke einlösen können

    Angenommen die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Wenn die Apotheke Am Flughafen die E-Rezepte mit der eGK von Aenna Gondern abruft
    Und die Apotheke Am Flughafen das letzte abgerufene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Aenna Gondern dispensiert
    Dann hat die Apotheke Am Flughafen genau 1 Quittung vorliegen

  @TCID=ERP_EE_WF160_EGK_APOTHEKE_02
  @Path=happy
  @Afo=A_22431
  Szenario: Eine Apotheke darf über die eGK nur E-Rezepte einsehen die noch nicht eingelöst sind
  Bei diesem Szenario werden der GKV Versicherten Aenna Gondern zwei E-Rezepte verschrieben.
  Das erste E-Rezepte wird über die Apotheke Am Flughafen mit der eGK eingelöst. Das zweite E-Rezept soll über die
  Apotheke NordApotheke abgerufen werden. Aenna Gondern will, dass die Apotheke NordApotheke beim Einlösen
  über die eGK nur Zugriff auf das noch nicht eingelöste E-Rezept bekommt.
    Angenommen die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und die Apotheke NordApotheke hat Zugriff auf ihre SMC-B
    Wenn die Apotheke Am Flughafen die E-Rezepte mit der eGK von Aenna Gondern abruft
    Und die Apotheke Am Flughafen das letzte abgerufene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke NordApotheke die E-Rezepte mit der eGK von Aenna Gondern abruft
    Dann kann die Apotheke NordApotheke das letzte E-Rezept nicht abrufen, weil die Apotheke Am Flughafen dieses bereits akzeptiert hat

  @TCID=ERP_EE_WF160_EGK_APOTHEKE_03
  @Path=happy
  @Afo=A_19284
  @Impl=open
  Szenario: Protokollierung von Einlösungen über eGK in der Apotheke
  Die GKV Versicherte Aenna Gondern will, dass das Einlösen über die eGK protokolliert
  und im FdV angezeigt wird.

    Angenommen die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Wenn die Apotheke Am Flughafen die E-Rezepte mit der eGK von Aenna Gondern abruft
    Und die Apotheke Am Flughafen das letzte abgerufene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Aenna Gondern dispensiert
    Dann hat die Versicherte Aenna Gondern für das letzte dispensiert E-Rezept im Zugriffsprotokoll des FdV einen Protokolleintrag


  @TCID=ERP_EE_WF160_EGK_APOTHEKE_04
  @Path=bad
  @Afo=A_21558
  Szenario: Einlösen von E-Rezepten über die eGK in Krankenhaus-Apotheke nicht möglich
  Die GKV Versicherte Aenna Gondern kann seine E-Rezept nicht in einer Krankenhaus-Apotheke
  mit seiner EGK einlösen, da die Krankenhaus-Apotheken nur mit der eGK keine E-Rezepte abrufen dürfen.

    Angenommen die Apotheke Am Waldesrand hat Zugriff auf ihre SMC-B
    Wenn die Krankenhaus-Apotheke Am Waldesrand die E-Rezepte mit der eGK von Aenna Gondern abruft
    Dann kann die Apotheke Am Waldesrand die E-Rezepte von Aenna Gondern nicht abrufen, weil Krankenhaus-Apotheken nicht berechtigt sind

  @TCID=ERP_EE_WF160_EGK_APOTHEKE_04
  @Path=bad
  @Afo=A_22432
  Szenario: Das Abrufen von E-Rezepten ohne das zuvor ein aktueller Prüfungsnachweis abgerufen wurde, muss vom E-Rezept Fachdienst untersagt werden.

    Angenommen die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Wenn die Apotheke Am Flughafen für die eGK von Aenna Gondern keinen Prüfungsnachweis abruft
    Dann kann die Apotheke Am Flughafen die E-Rezepte von Aenna Gondern nicht abrufen, weil der Prüfungsnachweis nicht abgerufen wurde


  @TCID=ERP_EE_WF160_EGK_APOTHEKE_06
  @Path=bad
  @Afo=A_22432
  Szenario: E-Rezepte mit eGK in der Apotheke nur mit zeitlich gültigem Prüfungsnachweis
  Die Apotheke Am Flughafen bekommt nur mit einem aktuellen, innerhalb der letzten 30 Minuten erstellten Prüfungsnachweis
  Zugriff auf die E-Rezepte des Versicherten Günther Angermänn.

    Angenommen die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Wenn die Apotheke Am Flughafen für die eGK von Aenna Gondern einen alten Prüfungsnachweis verwendet
    Dann kann die Apotheke Am Flughafen die E-Rezepte von Aenna Gondern nicht abrufen, weil der Prüfungsnachweis zeitlich ungültig ist
