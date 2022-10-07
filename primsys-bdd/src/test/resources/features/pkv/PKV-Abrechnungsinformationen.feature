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

@Workflow=200
@Versicherung=PKV
@Anwendungsfall=A_18822
@Anwendungsfall=A_18827
@Impl=open
Funktionalität: Abrechnungsinformationen für PKV-Rezepte
  Umfasst Testfälle zu den Anwendungsfällen:
  - Abrechnungssinformationen durch den Abgebenden bereitstellen
    - Abrechnungsinformationen  durch den Abgebenden abrufen
    - PKV-Abgabedatensatz ändern
    - Einwilligung durch den Versicherten erteilen
    - Einwilligung durch den Versicherten widerrufen
    - Einwilligung durch den Versicherten einsehen
    - Abrufen der Abrechnungsinformationen durch den Versicherten
    - Markieren der Abrechnungsinformation durch den Versicherten
    - Löschen der Abrechnungsinformation durch den Versicherten
    - Berechtigen der Apotheke zum Ändern des PKV-Abgabedatensatzes



  Grundlage:
    Angenommen die Ärztin Dr. Schraßer hat Zugriff auf ihren HBA und auf die SMC-B der Praxis
    Und der PKV Versicherte Günther Angermänn hat Zugriff auf seine digitale Identität
    Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B
    Und der Versicherte Günther Angermänn hat seine Einwilligung zum Speichern der Abrechnungsinformationen erteilt
    Und die Ärztin Dr. Schraßer dem Versicherten Günther Angermänn folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      | Workflow |
      | Schmerzmittel | 123456789| 200      |
    Und der Versicherte Günther Angermänn sein letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Günther Angermänn dispensiert

  @TCID=ERP_EE_WF200_01
  @Path=happy
  @Afo=A_22118
  @Anwendungsfall=AF_10082
  @Anwendungsfall=AF_10087
  Szenario: Bereitstellen der Abrechnungsinformationen durch die Apotheke

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept einen PKV-Abrechnungsdatensatz mit der SMC-B signiert und beim Fachdienst hinterlegt
    Dann kann der Versicherte Günther Angermänn für das letzte E-Rezept die Abrechnungsinformationen im FdV abrufen



  @TCID=ERP_EE_WF200_02
  @Path=bad
  @Afo=A_22133
  Szenario: Keine Abrechnungsinformationen bereitstellen, wenn keine Einwilligung erteilt
   Falls für den Versicherten keine Einwilligung beim Fachdienst zur Speicherung der Abrechnungsinformationen vorliegt, muss der Fachdienst das Hochladen des ChargeItems mit Fehlercode 403 ablehnen

    Wenn der Versicherte Günther Angermänn seine Einwilligung zur Speicherung der Abrechnungsinformationen widerruft
    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept keinen PKV-Abrechnungsdatensatz beim Fachdienst hinterlegen, weil keine Einwilligung vorliegt


  @TCID=ERP_EE_WF200_03
  @Path=happy
  @Afo=A_22161
  @Anwendungsfall=AF_10084
  @Anwendungsfall=AF_10086
  Szenario: Einwilligung durch Versicherten erteilen
  Der Versicherte kann im FdV die Einwilligung zum Speichern der Abrechnungsinformationen beim Fachdienst erteilen. Diese wird ihm im FdV angezeigt

    Wenn der Versicherte Günther Angermänn seine Einwilligung zur Speicherung der Abrechnungsinformationen erteilt
    Dann kann der Versicherte Günther Angermänn seine Einwilligung im FdV abrufen

  @TCID=ERP_EE_WF200_04
  @Path=happy
  @Afo=A_22157
  @Anwendungsfall=AF_10085
  Szenario: Abrechnungsinformationen löschen, wenn die Einwilligung vom Versicherten widerrufen wird
  Falls der Versicherte nach dem Speichern von Abrechnungsinformationen seine Einwilligung widerruft, werden die vorhandenen Abrechnungsinformationen beim Fachdiesnst gelöscht.
  Der E-Rezept-Fachdienst MUSS beim Aufruf der HTTP-Operation DELETE auf den Endpunkt /Consent mit ?category=CHARGCONS alle dem Versicherten zugeordneten ChargeItem-Ressourcen (ChargeItem.subject.identifier)
  anhand der KVNR des Versicherten im ACCESS_TOKEN im "Authorization"-Header des HTTP-Requests identifizieren und löschen.

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept einen PKV-Abrechnungsdatensatz mit der SMC-B signiert und beim Fachdienst hinterlegt
    Wenn der Versicherte Günther Angermänn für das letzte E-Rezept die Abrechnungsinformationen im FdV abruft
    Wenn der Versicherte Günther Angermänn seine Einwilligung zur Speicherung der Abrechnungsinformationen widerruft
    Dann kann der Versicherte Günther Angermänn keine Abrechnungsinformationen im FdV abrufen

  @TCID=ERP_EE_WF200_05
  @Path=happy
  @Afo=A_22113
  @Afo=A_22114
  @Anwendungsfall=AF_10090
  Szenario: Löschen der Abrechnungsinformationen durch den Versicherten
  Der Versicherte kann über das FdV die Abrechnungsinformationen zu einem Rezept löschen. Anschließend können sie nicht mehr angezeigt werden (FC 404)

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept einen PKV-Abrechnungsdatensatz mit der SMC-B signiert und beim Fachdienst hinterlegt
    Wenn der Versicherte Günther Angermänn für das letzte E-Rezept die Abrechnungsinformationen im FdV löscht
    Dann kann der Versicherte Günther Angermänn für das letzte E-Rezept keine Abrechnungsinformationen im FdV abrufen, weil sie nicht gefunden werden

  @TCID=ERP_EE_WF200_06
  @Path=happy
  @Anwendungsfall=AF_10083
  @Anwendungsfall=AF_10081
  Szenario: Ändern des PKV-Abgabedatensatzes auf Wunsch des Versicherten, Übertragung des AccessCodes per DMC

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept einen PKV-Abrechnungsdatensatz mit der SMC-B signiert und beim Fachdienst hinterlegt
    Wenn der Versicherte Günther Angermänn für das letzte E-Rezept die Abrechnungsinformationen im FdV abruft
    Und der Versicherte  Günther Angermänn die Apotheke Am Flughafen via Data Matrix Code zum Ändern der Abrechnungsinformationen berechtigt
    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept die Abrechnungsinformationen vom Fachdienst abrufen
    Dann kann die Apotheke Am Flughafen die Abrechnungsinformationen für das letzte dispensierte E-Rezept ändern
    Dann kann der Versicherte Günther Angermänn für das letzte E-Rezept geänderte Abrechnungsinformationen im FdV abrufen


  @TCID=ERP_EE_WF200_07
  @Path=happy
  @Anwendungsfall=AF_10083
  Szenario: Ändern des PKV-Abgabedatensatzes auf Wunsch des Versicherten, Übertragung des AccessCodes per Nachricht

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept einen PKV-Abrechnungsdatensatz mit der SMC-B signiert und beim Fachdienst hinterlegt
    Und der Versicherte Günther Angermänn für das letzte E-Rezept die Abrechnungsinformationen im FdV abruft
    Und der Versicherte  Günther Angermänn die Apotheke Am Flughafen per Nachricht zum Ändern der Abrechnungsinformationen berechtigt
    Und die Apotheke Am Flughafen die letzte Nachricht des Versicherten Günther Angermänn mit dem Änderungswunsch empfängt und beantwortet
    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept die Abrechnungsinformationen vom Fachdienst abrufen
    Dann kann die Apotheke Am Flughafen die Abrechnungsinformationen für das letzte dispensierte E-Rezept ändern
    Dann hat der Versicherte Günther Angermänn eine Antwort auf seinen Änderungswunsch von der Apotheke Am Flughafen erhalten
    Dann kann der Versicherte Günther Angermänn für das letzte E-Rezept geänderte Abrechnungsinformationen im FdV abrufen


  @TCID=ERP_EE_WF200_08
  @Path=bad
  @Afo=A_22616
  Szenario: Ändern des PKV-Abgabedatensatzes nur einmal möglich mit gleichem AccessCode

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept einen PKV-Abrechnungsdatensatz mit der SMC-B signiert und beim Fachdienst hinterlegt
    Wenn der Versicherte Günther Angermänn für das letzte E-Rezept die Abrechnungsinformationen im FdV abruft
    Und der Versicherte  Günther Angermänn die Apotheke Am Flughafen via Data Matrix Code zum Ändern der Abrechnungsinformationen berechtigt
    Dann kann die Apotheke Am Flughafen die Abrechnungsinformationen für das letzte dispensierte E-Rezept ändern
    Dann kann die Apotheke Am Flughafen die Abrechnungsinformationen für das letzte dispensierte E-Rezept nicht ändern, weil sie kein Recht dazu hat

  @TCID=ERP_EE_WF200_09
  @Path=bad
  @Afo=A_22215
  Szenario: Ändern des PKV-Abgabedatensatzes nicht möglich, weil Einwilligung widerrufen

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept einen PKV-Abrechnungsdatensatz mit der SMC-B signiert und beim Fachdienst hinterlegt
    Wenn der Versicherte Günther Angermänn für das letzte E-Rezept die Abrechnungsinformationen im FdV abruft
    Und der Versicherte  Günther Angermänn die Apotheke Am Flughafen via Data Matrix Code zum Ändern der Abrechnungsinformationen berechtigt
    Und der Versicherte Günther Angermänn seine Einwilligung zur Speicherung der Abrechnungsinformationen widerruft
    Dann kann die Apotheke Am Flughafen die Abrechnungsinformationen für das letzte dispensierte E-Rezept nicht ändern, weil sie kein Recht dazu hat
    Dann kann der Versicherte Günther Angermänn keine Abrechnungsinformationen im FdV abrufen

  @TCID=ERP_EE_WF200_10
  @Path=happy
  @Anwendungsfall=AF_10089
  Szenariogrundriss: Markieren des PKV-Abgabedatensatzes durch Versicherten

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept einen PKV-Abrechnungsdatensatz mit der SMC-B signiert und beim Fachdienst hinterlegt
    Wenn der Versicherte Günther Angermänn die Markierungen für Abrechnungsinformationen des letzten E-Rezept setzt:
      | Versicherung  | Beihilfe  | Finanzamt  |
      | <Versicherung>| <Beihilfe>| <Finanzamt>|

    Dann hat der Versicherte Günther Angermänn die Abrechnungsinformationen des letzten E-Rezepts mit folgenden Markierungen:
      | Versicherung  | Beihilfe  | Finanzamt  |
      | <Versicherung>| <Beihilfe>| <Finanzamt>|

    Beispiele:
      | Versicherung | Beihilfe | Finanzamt |
      | true         | true     | false     |
      | true         | false    | true      |
      | true         | false    | false     |
      | false        | true     | true      |
      | false        | true     | false     |
      | false        | false    | true      |
      | false        | false    | false     |


  @TCID=ERP_EE_WF200_11
  @Path=happy
  @Anwendungsfall=AF_10089
  Szenariogrundriss: Löschen von Markierungen des PKV-Abgabedatensatzes durch Versicherten

    Wenn die Apotheke Am Flughafen für das letzte dispensierte E-Rezept einen PKV-Abrechnungsdatensatz mit der SMC-B signiert und beim Fachdienst hinterlegt


    Wenn der Versicherte Günther Angermänn die Markierungen für Abrechnungsinformationen des letzten E-Rezept setzt:
      | Versicherung  | Beihilfe  | Finanzamt  |
      | <Versicherung>| <Beihilfe>| <Finanzamt>|

    Dann hat der Versicherte Günther Angermänn die Abrechnungsinformationen des letzten E-Rezepts mit folgenden Markierungen:
      | Versicherung  | Beihilfe  | Finanzamt  |
      | <Versicherung>| <Beihilfe>| <Finanzamt>|

    Beispiele:
      | Versicherung | Beihilfe | Finanzamt |
      | true         | true     | true      |
      | true         | true     | false     |
      | true         | false    | true      |
      | true         | false    | false     |
      | false        | true     | true      |
      | false        | true     | false     |
      | false        | false    | true      |
      | false        | false    | false     |


  @TCID=ERP_EE_WF200_12
  @Path=happy
  Szenario: Bereitstellen der Abrechnungsinformationen durch die Apotheke nach Änderung der Medikation
  Der PKV-Abgabedatensatz muss mit dem HBA signiert werden, wenn die Medikation gegenüber dem ursprünglichem Rezept verändert wurde

    Und der Apotheker Finn-Louis Nullmayr hat Zugriff auf seinen HBA

    Wenn der Apotheker Finn-Louis Nullmayr in der Apotheke Am Flughafen für das letzte dispensierte E-Rezept einen PKV-Abrechnungsdatensatz mit dem HBA signiert und beim Fachdienst hinterlegt
    Dann kann der Versicherte Günther Angermänn für das letzte E-Rezept die Abrechnungsinformationen im FdV abrufen

  @TCID=ERP_EE_WF200_13
  @Path=bad
  @Afo=A_22131
  Szenario: Bereitstellen der Abrechnungsinformationen nicht möglich, weil E-Rezept gelöscht
    Wenn die Versicherte Sina Hüllmann ihr letztes E-Rezept löscht
    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept keinen PKV-Abrechnungsdatensatz beim Fachdienst hinterlegen, weil der Task nicht mehr existiert

  @TCID=ERP_EE_WF200_14
  @Path=bad
  @Afo=A_22731
  Szenariogrundriss: Keine Abrechnungsinformationen für GKV Workflows
  Keine Abrechnungsinformationen bereitstellen bei Workflow 169 und 160

    Wenn die Ärztin Dr. Schraßer dem Versicherten Günther Angermänn folgendes apothekenpflichtiges Medikament verschreibt:
      | Name          | PZN      | Workflow |
      | Schmerzmittel | 123456789| <WF>    |
    Und der Versicherte Günther Angermänn sein letztes ausgestellte E-Rezept der Apotheke Am Flughafen via Data Matrix Code zuweist
    Und die Apotheke Am Flughafen das letzte zugewiesene E-Rezept beim Fachdienst akzeptiert
    Und die Apotheke Am Flughafen das letzte akzeptierte E-Rezept korrekt an Günther Angermänn dispensiert

    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept keinen PKV-Abrechnungsdatensatz beim Fachdienst hinterlegen, weil es kein PKV-Rezept ist

    Beispiele:
    |WF |
    |160|
    |169|

  @TCID=ERP_EE_WF200_13
  @Afo=A_22132
  @Path=bad
  Szenario: Keine Abrechnungsinformationen bereitstellen bei ungültigem Secret

    Dann kann die Apotheke Am Flughafen für das letzte dispensierte E-Rezept keinen PKV-Abrechnungsdatensatz mit dem falschen Secret fgdkjfgd hinterlegen
