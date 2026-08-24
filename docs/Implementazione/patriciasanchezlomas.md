La mia parte nell'implementazione consiste essenzialmente nella logica relativa alle mappe e alla libreria delle carte, nonché nella parte dell'interfaccia grafica relativa ad alcuni eventi.

## CardLibrary 

Ho creato un ampio elenco di carte base secondo la definizione di `Card` e prendendo come riferimento le carte del gioco originale. Ho anche aggiunto alcuni semplici metodi per ottenere diversi tipi di carte (ad esempio: carte poco comuni) a seconda delle esigenze della logica di gioco.

## Maps

L'avanzamento nel gioco avviene attraverso una serie di eventi e combattimenti. Per la strategia di gioco è importante poter scegliere percorsi diversi a seconda degli eventi preferiti dal giocatore. A tal fine ho definito una struttura dati `Path`, che funziona in modo simile alle `Sequences`, ma dispone di un'opzione in più per gestire le diramazioni lungo il percorso.

```scala
enum Path[E]:
  case Node(event: E, next: Path[E])
  case Fork(left: Path[E], right: Path[E])
  case End()
```

Questa struttura offre alcuni vantaggi:
* Navigazione intuitiva: andare avanti significa scegliere `next`, oppure `left` o `right` come nuovo percorso.
* Posizione attuale autonoma: non è necessario memorizzare un altro valore che indichi quale sia la posizione attuale.
- Eliminazione dei percorsi: vengono ignorati gli eventi passati e i percorsi non scelti.

Tuttavia, quando il percorso è lungo e presenta alcune diramazioni, risulta molto complicato da leggere e scrivere; per questo ho introdotto una nuova struttura `M̀apScript`, il cui unico scopo è consentire la creazione di mappe più complesse in modo molto più intuitivo.
Si basa sulla collezione `List` e contiene una serie di livelli `MapLevel` (che possiamo intendere come altezze o passaggi), ognuno dei quali contiene una diramazione `MapBranch`. Ogni diramazione contiene uno (`Node` o `Join`) o due (`Fork`) eventi e un numero intero `offset` che serve a calcolare i collegamenti al momento della creazione del `Path`. Il `MapScript` del gioco viene ricavato da `MapTemplates`.
La differenza in termini di semplicità si può apprezzare prendendo come esempio uno dei test.

```scala
Scenario("The Path must have a four branches that join back") {

  Given("a MapScript with some events")
  val event1 = randomEvent
  val event2 = randomEvent
  val event3 = randomEvent
  val event4 = randomEvent
  val event5 = randomEvent
  val event6 = randomEvent
  val event7 = randomEvent
  val event8 = randomEvent
  val event9 = randomEvent
  val event10 = randomEvent

  val script: MapScript[GameEvent] = MapScript(List(
      MapLevel(List(
          Node(3, event1)
        )
      ),
      MapLevel(List(
          Fork(3, event2, event3)
        )
      ),
      MapLevel(List(
          Fork(1, event4, event5), Fork(5, event6, event7)
        )
      ),
      MapLevel(List(
          Join(1, event8), Join(5, event9)
        )
      ),
      MapLevel(List(
          Join(3, event10)
        )
      )
    )
  )

  When("creating a new Path")
  val gameMap: Path[GameEvent] = Path.fromScript(script)

  Then("The result must have four branches")
  gameMap.shouldBe(Path.Node(event1, Path.Fork(
        Path.Node(event2, Path.Fork(
            Path.Node(event4, Path.Node(event8, Path.Node(event10, Path.End()))),
            Path.Node(event5, Path.Node(event8, Path.Node(event10, Path.End())))
          )
        ),
        Path.Node(event3, Path.Fork(
            Path.Node(event6, Path.Node(event9, Path.Node(event10, Path.End()))),
            Path.Node(event7, Path.Node(event9, Path.Node(event10, Path.End())))
          )
        )
      )
    )
  )
}
```

## View

### Resources 

Ho raccolto e classificato una raccolta che comprende quasi tutte le risorse visive del gioco originale necessarie alle diverse interfacce utente. Per ottenere le immagini in modo semplice, ho creato `ResourceLoader`, che contiene metodi per caricarle in modo conciso a partire dal loro percorso. Le directory sono contenute in `GUIAssets`, in modo da poterle gestire e modificare facilmente.

### CardView

Per separare le informazioni logiche di una carta dalla sua rappresentazione visiva, `CardView` utilizza `CardViewInfo` per ottenere i dati necessari alla stampa, compresa la separazione dei `seals`, al fine di conferire un aspetto speciale alle carte che l'utente ha potenziato. L'obiettivo è che le carte appaiano il più possibile simili a quelle del gioco originale.
Viene inoltre utilizzato `GUIGeometry`, che definisce una serie di proporzioni e posizioni dei diversi componenti dell’interfaccia grafica del gioco; in particolare, `CardGeometry` garantisce un aspetto omogeneo della carta indipendentemente dalle sue dimensioni.

### StatBonus

Questo oggetto consente alla carta di assumere un nuovo aspetto (`CardViewInfo`) quando viene potenziata. Tuttavia, il vero potenziamento avviene in un secondo momento; `StatBonus` si limita a mostrarlo all'utente.

### MapView, StartScreenView, CardSelectionView, FireCampView, MycologystsView e StrangeStonesView

Questi componenti di Scala Swing forniscono una rappresentazione visiva degli eventi di gioco e, al contempo, hanno la funzione di restituire al modello un `GameMessage` contenente l'input dell'utente; pertanto, seguono una struttura comune:

1. Mostra le opzioni all'utente
2. Attende l'input
3. Conferma la selezione e la invia
4. Mostra uno stato finale (non sempre)

