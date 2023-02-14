#
# ${GEMATIK_COPYRIGHT_STATEMENT}
#
# language: de


@Versicherung=All
@Impl=open
Funktionalität: Alle löschbaren Rezepte einer Versicherten löschen
  Über das FdV kann der Versicherte alle seine Rezepte löschen.
  Nur die Rezepte, die von der Apotheke schon reserviert (Accept), aber noch nicht eingelöst (Close) wurden, sind nicht durch den Versicherten löschbar.

  @TCID=ERP_EE_01
    @Path=happy
    @MainActor=Versicherter
  Szenariogrundriss: Der Versicherte löscht alle seine löschbaren Rezepte unabhängig vom Status.


   Angenommen die <Versicherungsart> Versicherte <Akteur> hat Zugriff auf ihre eGK

    Wenn die Versicherte <Akteur> alle löschbaren E-Rezepte löscht
    Dann werden der Versicherten <Akteur> keine löschbaren Rezepte mehr im FDV angezeigt



    Beispiele:
      | Versicherungsart | Akteur          |
      | GKV              |Sine Hüllmann    |
      | GKV              |Aenna Gondern    |
      | GKV              |Fridolin Straßer |
      | PKV              |Günther Angermänn|
      | PKV              |Hanna Bäcker     |
