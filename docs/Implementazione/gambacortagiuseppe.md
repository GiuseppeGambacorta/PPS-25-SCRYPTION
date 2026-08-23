

## 1. Implementazione della `Board`

`BoardRow` e `Board` sono implementati come **opaque type** basati internamente su collezioni immutabili (`Vector[Slot]` e `Vector[BoardRow]`). L'adozione dei tipi opachi garantisce il totale incapsulamento della rappresentazione dati: all'esterno del modulo di definizione, `BoardRow` e `Board` sono tipi a sé stanti e possono essere manipolati esclusivamente attraverso i costruttori controllati e gli extension methods esposti, impedendo la creazione di stati non validi o l'accesso diretto alla struttura sottostante.

Uno `Slot` modella lo stato della singola cella come `Option[Card[?]]` (`Some(card)` per una cella occupata, `None` per una cella vuota). La costante `x` è definita come alias di `None` per rappresentare visivamente una cella vuota all'interno delle espressioni del DSL.

### 1.1 Il DSL di costruzione: operatori `|` e `||`

Il modulo fornisce un DSL dichiarativo per istanziare righe e board in modo intuitivo, evitando factory verbose o costruttori annidati. Il DSL sfrutta due extension methods infissi:

- **`|` (pipe singola)**: concatena singoli `Slot` per comporre una `BoardRow`. L'operatore permette sia di iniziare una riga (`slot1 | slot2`) sia di estendere una riga esistente (`riga | nuovoSlot`), con vincoli `require` che validano a runtime il limite esatto delle colonne consentite;
- **`||` (doppia pipe)**: aggrega istanze di `BoardRow` per comporre una `Board`. L'operatore supporta la combinazione di due righe (`riga1 || riga2`) o l'aggiunta di una riga a una griglia (`board || riga3`), verificando la conformità sul numero totale di righe consentite.

L'uso combinato di questi operatori rende la definizione delle board visualmente isomorfa alla griglia di gioco:

```scala
val initialBoard = (Some(squirrel) | x          | x | x) ||
                   (x              | Some(bear) | x | x) ||
                   (x              | x          | x | x)
```

In aggiunta al DSL visivo, è disponibile la factory esplicita per la costruzione rapida di singole righe:

```scala
val row = BoardRow(Some(squirrel), Some(bear), Some(fox), x)
```

Questa astrazione ha semplificato e velocizzato la stesura dei test automatici per le logiche di combattimento e il gameplay loop, permettendo di istanziare scenari di test complessi con una sintassi compatta e priva di boilerplate.

---

## 2. Architettura degli Eventi di Gioco

### 2.1 Il tipo `Event`

Nel sistema, un `Event` è l'astrazione fondamentale che descrive qualsiasi transizione di stato del gioco:

```scala
type Event = (GameState, GameMessagesChannel) => GameState
```

Dal punto di vista concettuale, un `Event` è una funzione che accetta lo stato corrente della partita (`GameState`) e un canale di sincronizzazione thread-safe (`GameMessagesChannel`), restituendo il nuovo stato risultante (`GameState`).

L'adozione di questo type alias offre tre vantaggi architetturali:

- **Firma uniforme**: ogni dinamica di gioco (combattimenti, pesca carte, potenziamenti al falò, sacrifici) rispetta la medesima interfaccia, permettendo al controller di trattare tutte le attività in modo del tutto polimorfo;
- **Separazione tra logica e I/O**: la logica del Model calcola la trasformazione dello stato, usando il canale solo per sincronizzarsi asincronamente con l'utente senza conoscere dettagli grafici;
- **Componibilità e testabilità**: un `Event` può essere eseguito e verificato passando un mock/stub del canale o verificando direttamente la transizione da `GameState` a `GameState`.

### 2.2 Associazione Evento-Vista: `GameEvents`

Per collegare in modo modulare la logica di dominio alla rispettiva interfaccia grafica senza creare dipendenze rigide, il modulo `GameEvents` definisce la tupla `GameEvent`:

```scala
type GameEvent = (Event, GameMessagesChannel => Panel)
```

Un `GameEvent` accoppia:

1. La funzione di dominio (`Event`): calcola la transizione dello stato di gioco;
2. La factory grafica (`GameMessagesChannel => Panel`): riceve l'istanza del canale condiviso, istanzia il rispettivo `ViewModel` e produce il `Panel` Swing per il rendering.

```scala
object GameEvents:
  val getANewCard: GameEvent = (getANewCardEvent, ch => new CardSelectionView(ViewModelDeckEvent(ch)))
  val fight: GameEvent       = (fightEvent, ch => new FightView(ViewModelFight(ch)))
  val getANewItem: GameEvent = (getANewItemEvent, ch => new ItemSelectionView(ViewModelItemEvent(ch)))
  // ... altri eventi di potenziamento (falò, micologi, sacrifici)
```

Questa struttura rende l'architettura altamente estensibile: l'introduzione di una nuova schermata o meccanica richiede esclusivamente l'aggiunta di una nuova coppia (funzione di transizione + factory della vista).

### 2.3 Orchestrazione e Ciclo di Gioco: `GameController`

Il `GameController` è il componente incaricato di coordinare l'esecuzione asincrona della partita, governando il passaggio tra schermate ed eventi.

Le sue responsabilità principali includono:

- **Esecuzione Asincrona su Thread Dedicato**: All'avvio (`startNewGame()`), il controller genera lo stato iniziale (`GameState`) e la mappa (`GameMap`), delegando l'esecuzione del `gameLoop` a un `Future` in background per non bloccare l'Event Dispatch Thread (EDT) di Swing durante le attese sul canale.
- **Iniezione del Canale e Cambio Schermata**: Nel metodo ricorsivo di coda (`@tailrec def gameLoop`), per ciascun nodo della mappa:
    1. Estrae la logica e la factory della vista da `map.currentEvent`;
    2. Crea un'istanza dedicata di `GameMessagesChannel`;
    3. Invia il nuovo pannello UI alla vista tramite la callback `onViewChange(createView(eventCh))`;
    4. Esegue `eventLogic(gameState, eventCh)`, attendendo la risoluzione dell'evento.
- **Avanzamento sulla Mappa**: Una volta ottenuto il `nextState`, se la partita continua il controller crea un nuovo canale per la mappa, monta la `MapView` tramite `ViewModelMap` ed esegue `MapEvent(map, mapCh)` per acquisire la scelta del percorso del giocatore prima di ricominciare il ciclo.

---

## 3. Sistema di Combattimento (`fightEvent`)

Il combattimento è l'evento più articolato tra quelli previsti dal gioco: a differenza degli altri eventi, che modellano una singola interazione puntuale con il giocatore, `fightEvent` gestisce un intero sotto-ciclo di gioco scandito da più **stati** che si alternano fino al raggiungimento di una condizione di vittoria o sconfitta.

### 3.1 Macchina a stati

L'intero combattimento è governato da una macchina a stati finiti (`TurnState`), che rappresenta in ogni momento la fase attiva del turno:

- **draw**: fase di pesca, in cui il giocatore sceglie una carta da aggiungere alla propria mano;
- **playerTurn**: fase in cui il giocatore posiziona carte sulla board o utilizza oggetti;
- **playerFight**: fase in cui viene risolto l'attacco delle creature del giocatore contro quelle del bot;
- **botTurn**: fase in cui l'avversario calcola ed esegue le proprie mosse;
- **botFight**: fase in cui viene risolto l'attacco delle creature del bot contro quelle del giocatore.

Queste fasi si susseguono in un ciclo continuo, alternando le azioni delle due entità finché il bilanciamento della bilancia dei danni (`scalePoints`) non raggiunge una delle due soglie limite: superata in positivo decreta la vittoria del giocatore, superata in negativo la sconfitta.

<pre class="mermaid">
stateDiagram-v2
    [*] --> draw
    draw --> playerTurn : pesca effettuata
    draw --> EndFight : sconfitta (soglia negativa raggiunta)
    playerTurn --> playerFight : fine turno giocatore
    playerFight --> botTurn
    botTurn --> botFight
    botTurn --> EndFight : vittoria (soglia positiva raggiunta)
    botFight --> draw
    EndFight
</pre>

### 3.2 Decomposizione in Handler e Testing Modulare

La logica di transizione della macchina a stati è centralizzata in una funzione ricorsiva di loop che fa pattern matching sullo stato corrente:

```scala
turnState match
  case TurnState.draw =>
    if fightState.scalePoints <= BotWinningPoints then PlayerLost
    else
      val (nextTurn, nextState) = handleDrawPhase(fightState, ch)
      loop(nextTurn, nextState, ch)

  case TurnState.playerTurn =>
    val (nextTurn, nextState) = handlePlayerTurnPhase(fightState, ch)
    loop(nextTurn, nextState, ch)

  case TurnState.playerFight =>
    val (nextTurn, nextState) = handleFightPhase(fightState, isPlayerAttacking = true)
    loop(nextTurn, nextState, ch)

  case TurnState.botTurn =>
    if fightState.scalePoints >= PlayerWinningPoints then PlayerWon
    else
      val bot: BotStrategy = RandomBotStrategy()
      val fightStateAfterBotPlays = bot.playTurn(fightState)
      loop(TurnState.botFight, fightStateAfterBotPlays, ch)

  case TurnState.botFight =>
    val (nextTurn, nextState) = handleFightPhase(fightState, isPlayerAttacking = false)
    loop(nextTurn, nextState, ch)
```

Per mantenere il codice manutenibile e modulare, l'effettiva computazione di ciascun passaggio è delegata a funzioni handler dedicate (`handleDrawPhase`, `handlePlayerTurnPhase`, `handleFightPhase`). Questa scomposizione garantisce:

- **Isolamento delle responsabilità**: ogni handler incapsula esclusivamente le regole relative alla propria fase di gioco, gestendo in autonomia l'eventuale comunicazione asincrona sul canale `ch` o il calcolo deterministico del danno;
- **Testabilità unitaria granulare**: ogni singolo handler è stato testato in completo isolamento rispetto al ciclo principale, verificando puntualmente le variazioni di stato (`fightState`), il calcolo dei danni alle carte, l'applicazione dei sacrifici e le risposte del canale tramite test di unità dedicati prima della loro integrazione nel loop complessivo.

---

## 4. Implementazione di `GameMessagesChannel`

Questa sezione descrive nel dettaglio l'implementazione del canale di comunicazione tra Model e View, a partire dalla gerarchia dei messaggi che vi transitano fino ai meccanismi concreti di sincronizzazione.

### 4.1 La gerarchia dei messaggi: `GameMessage`

Alla base di tutto c'è `GameMessage`, un `sealed trait` che rappresenta il tipo comune a cui appartiene ogni messaggio scambiabile sul canale. Essendo `sealed`, il compilatore conosce l'insieme chiuso dei suoi sottotipi diretti, il che consente pattern matching esaustivi (senza bisogno di un caso di default "a copertura") ovunque un messaggio venga gestito.

Da `GameMessage` derivano tre enum, ciascuno dedicato a un contesto di gioco specifico:

- **`EventMessages`**: messaggi per gli eventi generici a singola interazione (`Cards`, `SingleCard`, `Items`, `SingleItem`, `End`) usati ad esempio per la selezione di carte o oggetti;
- **`FightMessages`**: messaggi dedicati al combattimento (`State`, `DrawFromSquirrel`, `DrawFromDeck`, `CardToPlay`, `CardToPlayWithSacrifices`, `UseItem`, `EndPlayerTurn`, `End`)  coprono sia le azioni che il giocatore può compiere sia la notifica dello stato corrente del turno;
- **`MapMessages`**: messaggi per la navigazione sulla mappa (`left`, `right`, `forward`).

Ogni enum estende `GameMessage`, quindi tutti i suoi casi sono automaticamente utilizzabili ovunque sia richiesto un `GameMessage` generico: il canale può così restare agnostico rispetto al contesto specifico, mentre chi invia e chi riceve un messaggio conosce (tramite pattern matching) a quale enum appartiene e come interpretarlo.

### 4.2 L'interfaccia: `trait GameMessagesChannel`

Il contratto del canale è definito da un trait, che espone cinque operazioni:

- `sendToGui` / `receiveFromGame`: la direzione di flusso dal Model verso la View;
- `sendToGame` / `receiveFromGui`: la direzione di flusso dalla View verso il Model;
- `clear`: lo svuotamento di entrambe le code di messaggi.

Definire il canale come trait, anziché esporre direttamente la classe concreta, permette di programmare contro un'astrazione: il resto del sistema dipende solo da questa interfaccia, il che rende possibile sostituire l'implementazione (ad esempio con una versione fittizia per i test) senza alcun impatto sul codice che la utilizza.

### 4.3 L'implementazione concreta: `GameMessagesChannelImpl`

`GameMessagesChannelImpl` realizza il trait appoggiandosi a due `LinkedBlockingQueue[GameMessage]`, una per ciascuna direzione (`toGui` e `toGame`). L'uso di una coda bloccante è la scelta chiave che rende il canale un vero Monitor produttore-consumatore:

- le operazioni di lettura (`receiveFromGui` tramite `toGame.take()`, `receiveFromGame` tramite `toGui.take()`) si bloccano automaticamente finché non è disponibile un messaggio, senza bisogno di logica di attesa esplicita scritta a mano;
- le operazioni di scrittura (`sendToGui`, `sendToGame`) inseriscono il messaggio nella coda corrispondente tramite `put`, avvolte in un blocco `synchronized` su un oggetto di lock dedicato, condiviso tra le due operazioni di invio;
- l'operazione `clear` svuota entrambe le code, anch'essa protetta dallo stesso lock, per evitare che uno svuotamento avvenga in concomitanza con un invio in corso.

Va notato che le operazioni di lettura (`take()`) non sono racchiuse nello stesso blocco `synchronized`: `LinkedBlockingQueue` è già internamente thread-safe per le operazioni di inserimento ed estrazione, quindi il lock esplicito serve unicamente a coordinare tra loro le scritture (e la pulizia), non le letture, che possono avvenire in sicurezza direttamente sulla coda.


<script type="module">
  import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs';
  mermaid.initialize({ startOnLoad: true, theme: 'neutral' });
</script>