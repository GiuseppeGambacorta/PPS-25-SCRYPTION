## Implementazione di Board

Questa sezione descrive nel dettaglio l'implementazione del modello della board di gioco, definito nel modulo boardModel. A differenza della panoramica concettuale già vista in precedenza, qui si entra nel merito delle scelte tecniche adottate: i tipi opachi, il DSL di costruzione basato sugli operatori | e ||, e le operazioni di accesso e aggiornamento.

Tipi opachi: BoardRow e Board

BoardRow e Board sono definiti come tipi opachi (opaque type), rispettivamente Vector[Slot] e Vector[BoardRow]. L'uso di tipi opachi permette di nascondere completamente la rappresentazione interna basata su Vector: dall'esterno del modulo, BoardRow e Board sono tipi a sé stanti, e possono essere manipolati solo attraverso le operazioni esposte come extension methods, senza alcuna possibilità di accedere direttamente alla struttura dati sottostante o di costruirli in modo non conforme alle regole del dominio.

Uno Slot è semplicemente un Option[Card[?]]: Some(carta) se la cella è occupata, None se è vuota. La costante x è definita come alias di None, pensata per essere usata come "cella vuota" all'interno delle espressioni del DSL, rendendole più leggibili.

Il DSL di costruzione: operatori | e ||

Il modulo espone un piccolo DSL interno per costruire righe e board in modo dichiarativo e leggibile, invece di dover chiamare esplicitamente costruttori o factory verbose. Il DSL si basa su due operatori, entrambi definiti come extension methods infissi:

| (pipe singola): compone singoli Slot in sequenza, accumulandoli in una BoardRow. È definito sia a partire da uno Slot (per iniziare una riga: slot1 | slot2) sia a partire da una BoardRow già esistente (per estenderla di uno slot alla volta: riga | nuovoSlot), con una require che garantisce di non superare ColsCount slot nella riga;
|| (doppia pipe): compone BoardRow in una Board. È definito sia a partire da due BoardRow (per creare una board di due righe: riga1 || riga2) sia a partire da una Board già esistente (per aggiungerle un'ulteriore riga: board || riga3), sempre con require a garanzia del numero corretto di colonne e del limite massimo di righe (RowsCount).

In pratica, l'uso combinato dei due operatori permette di scrivere una board leggibile quasi come una griglia visiva nel codice, ad esempio componendo celle vuote (x) e celle occupate (Some(carta)) slot per slot, riga per riga, per poi unire le righe tra loro con ||.

Sistema di Combattimento (fightEvent)

Il combattimento è l'evento più articolato tra quelli previsti dal gioco: a differenza degli altri eventi, che modellano una singola interazione punto-a-punto con il giocatore, `fightEvent` gestisce un intero sotto-ciclo di gioco, scandito da più **stati** che si alternano fino al raggiungimento di una condizione di vittoria o sconfitta.

## Una macchina a stati

L'intero combattimento è governato da una macchina a stati finiti (`TurnState`), che rappresenta in ogni momento la fase in cui si trova il turno:

- **draw**: fase di pesca, in cui il giocatore sceglie una carta da aggiungere alla propria mano;
- **playerTurn**: fase in cui il giocatore gioca le proprie carte sul campo o usa un oggetto;
- **playerFight**: fase in cui viene risolto l'attacco del giocatore contro il bot;
- **botTurn**: fase in cui è il bot a decidere e giocare la propria mossa;
- **botFight**: fase in cui viene risolto l'attacco del bot contro il giocatore.

Queste fasi si susseguono in un ciclo continuo, alternando il turno del giocatore a quello del bot, finché il punteggio di combattimento (`scalePoints`) non raggiunge una delle due soglie che decretano la fine dello scontro: superata in positivo si vince, superata in negativo si perde.

<pre class="mermaid">
stateDiagram-v2
    [*] --> draw
    draw --> playerTurn : pesca effettuata
    draw --> [*] : sconfitta (soglia negativa raggiunta)
    playerTurn --> playerFight : fine turno giocatore
    playerFight --> botTurn
    botTurn --> botFight
    botTurn --> [*] : vittoria (soglia positiva raggiunta)
    botFight --> draw
</pre>





# Implementazione di GameMessagesChannel

Questa sezione descrive nel dettaglio l'implementazione del canale di comunicazione tra Model e View, a partire dalla gerarchia dei messaggi che vi transitano fino ai meccanismi concreti di sincronizzazione.

## La gerarchia dei messaggi: GameMessage

Alla base di tutto c'è `GameMessage`, un `sealed trait` che rappresenta il tipo comune a cui appartiene ogni messaggio scambiabile sul canale. Essendo `sealed`, il compilatore conosce l'insieme chiuso dei suoi sottotipi diretti, il che consente pattern matching esaustivi (senza bisogno di un caso di default "a copertura") ovunque un messaggio venga gestito.

Da `GameMessage` derivano tre enum, ciascuno dedicato a un contesto di gioco specifico:

- **`EventMessages`**: messaggi per gli eventi generici a singola interazione (`Cards`, `SingleCard`, `Items`, `SingleItem`, `End`) — usati ad esempio per la selezione di carte o oggetti;
- **`FightMessages`**: messaggi dedicati al combattimento (`State`, `DrawFromSquirrel`, `DrawFromDeck`, `CardToPlay`, `CardToPlayWithSacrifices`, `UseItem`, `EndPlayerTurn`, `End`) — coprono sia le azioni che il giocatore può compiere sia la notifica dello stato corrente del turno;
- **`MapMessages`**: messaggi per la navigazione sulla mappa (`left`, `right`, `forward`).

Ogni enum estende `GameMessage`, quindi tutti i suoi casi sono automaticamente utilizzabili ovunque sia richiesto un `GameMessage` generico: il canale può così restare agnostico rispetto al contesto specifico, mentre chi invia e chi riceve un messaggio conosce (tramite pattern matching) a quale enum appartiene e come interpretarlo.

## L'interfaccia: trait GameMessagesChannel

Il contratto del canale è definito da un trait, che espone cinque operazioni:

- `sendToGui` / `receiveFromGame`: la direzione di flusso dal Model verso la View;
- `sendToGame` / `receiveFromGui`: la direzione di flusso dalla View verso il Model;
- `clear`: lo svuotamento di entrambe le code di messaggi.

Definire il canale come trait, anziché esporre direttamente la classe concreta, permette di programmare contro un'astrazione: il resto del sistema dipende solo da questa interfaccia, il che rende possibile sostituire l'implementazione (ad esempio con una versione fittizia per i test) senza alcun impatto sul codice che la utilizza.

## L'implementazione concreta: GameMessagesChannelImpl

`GameMessagesChannelImpl` realizza il trait appoggiandosi a due `LinkedBlockingQueue[GameMessage]`, una per ciascuna direzione (`toGui` e `toGame`). L'uso di una coda bloccante è la scelta chiave che rende il canale un vero Monitor produttore-consumatore:

- le operazioni di lettura (`receiveFromGui` tramite `toGame.take()`, `receiveFromGame` tramite `toGui.take()`) si bloccano automaticamente finché non è disponibile un messaggio, senza bisogno di logica di attesa esplicita scritta a mano;
- le operazioni di scrittura (`sendToGui`, `sendToGame`) inseriscono il messaggio nella coda corrispondente tramite `put`, avvolte in un blocco `synchronized` su un oggetto di lock dedicato, condiviso tra le due operazioni di invio;
- l'operazione `clear` svuota entrambe le code, anch'essa protetta dallo stesso lock, per evitare che uno svuotamento avvenga in concomitanza con un invio in corso.

Va notato che le operazioni di lettura (`take()`) non sono racchiuse nello stesso blocco `synchronized`: `LinkedBlockingQueue` è già internamente thread-safe per le operazioni di inserimento ed estrazione, quindi il lock esplicito serve unicamente a coordinare tra loro le scritture (e la pulizia), non le letture, che possono avvenire in sicurezza direttamente sulla coda.

<pre class="mermaid">
classDiagram
    class GameMessage {
        <<sealed trait>>
    }
    class EventMessages {
        <<enum>>
    }
    class FightMessages {
        <<enum>>
    }
    class MapMessages {
        <<enum>>
    }
    GameMessage <|-- EventMessages
    GameMessage <|-- FightMessages
    GameMessage <|-- MapMessages

    class GameMessagesChannel {
        <<trait>>
        +sendToGui(message)
        +receiveFromGui() 
        +sendToGame(message)
        +receiveFromGame()
        +clear()
    }
    class GameMessagesChannelImpl {
        -toGui: LinkedBlockingQueue
        -toGame: LinkedBlockingQueue
        -lock: Object
    }
    GameMessagesChannel <|.. GameMessagesChannelImpl
    GameMessagesChannelImpl --> GameMessage : contiene
</pre>

## Companion object: costruzione del canale

Il companion object `GameMessagesChannel` espone un unico metodo `apply()`, che crea una nuova `GameMessagesChannelImpl` inizializzando al suo interno le due code vuote. Questo è l'unico punto in cui viene istanziata l'implementazione concreta: chi crea un canale ottiene sempre e solo il tipo `GameMessagesChannel` (il trait), senza mai venire a conoscenza dell'esistenza della classe `Impl` o dei dettagli con cui è realizzata — coerentemente con il principio di incapsulamento adottato anche altrove nel sistema.

<script type="module">
  import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs';
  mermaid.initialize({ startOnLoad: true, theme: 'neutral' });
</script>