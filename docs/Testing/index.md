---
title: Testing
parent: Report
---

# Testing

## Approccio

Lo sviluppo dei test non ha seguito rigorosamente il ciclo *red–green–refactor* del Test-Driven Development: nella maggior parte dei casi i test sono stati scritti **in parallelo** all'implementazione, piuttosto che sistematicamente prima di essa. La strategia di test copre comunque in modo esteso tutti i component del *model* con particolare attenzione alla logica di gioco e alla gestione della comunicazione tramite il canale di messaggi tra Model e ViewModel.

## ScalaTest
qui devi dire che abbiamo seguito i requisiti del gioco e
Per i test è stata utilizzata la libreria **ScalaTest**, in stile **BDD** con `AnyFeatureSpec`, `GivenWhenThen` e i `Matchers` (`shouldBe`). Ogni funzionalità è organizzata in `Feature` e `Scenario`, con i passaggi descritti tramite `Given` / `When` / `Then` / `And`, per rendere esplicito sia il comportamento atteso sia il contesto in cui viene verificato.

Un esempio dai test degli eventi di gioco (`ChangeDeckEventsTest`), che verifica l'evento di aggiunta di una nuova carta al mazzo:

```scala
Feature("GetANewCard event") {

  Scenario("Successfully adding a new card when receiving a valid SingleCard message") {
    Given("An initial GameState with 2 cards and a GUIChannel")
    val initialDeck = fromList(squirrel :: bear :: Nil)
    val initialGameState = GameState.getInitialGameState(initialDeck)
    val ch = GameMessagesChannel()
    ch.sendToGame(EventMessages.SingleCard(squirrel))

    When("Executing the GetANewCard event")
    val updatedGameState = getANewCardEvent(initialGameState, ch)

    Then("The resulting deck should equal the initial deck with the chosen card added")
    updatedGameState.deck shouldBe (initialDeck addCard squirrel)
    updatedGameState.deck.size shouldBe initialDeck.size + 1

    And("The game should not be over")
    updatedGameState.isGameOver shouldBe false
  }
}
```

## Testabilità degli eventi e della comunicazione via canale

Poiché la comunicazione tra Model e ViewModel avviene tramite un canale di messaggi (`GameMessagesChannel`) e non tramite chiamate dirette, è possibile testare gli eventi di gioco in modo isolato, senza dover avviare l'intera interfaccia grafica: il test invia direttamente sul canale i messaggi che simulano l'input dell'utente, ed esegue l'evento passandogli lo stato di gioco iniziale e il canale.

Per gli scenari più complessi, in cui l'evento attende più messaggi in sequenza o deve gestire input inatteso, viene avviato un thread separato che simula la GUI. Un helper comune (`runMockGuiWithRetry`) permette di riprodurre facilmente il pattern "prima un messaggio non valido, poi quello corretto", verificando così che l'evento sappia recuperare da una sequenza di messaggi inattesa anziché bloccarsi o fallire:

```scala
private def runMockGuiWithRetry(ch: GameMessagesChannel)(wrongResponses: List[EventMessages], correctResponses: List[EventMessages]): Thread =
  val thread = new Thread(() => {
    ch.receiveFromGame
    wrongResponses.foreach(ch.sendToGame)

    ch.receiveFromGame
    correctResponses.foreach(ch.sendToGame)
  })
  thread.start()
  thread
```

Questo pattern viene riutilizzato in più scenari (ad esempio *Firecamp Attack* e *Sacrifice*) per verificare che, a fronte di un messaggio del tipo sbagliato (`Cards` invece di `SingleCard`, oppure `End` inatteso), l'evento richieda correttamente un nuovo messaggio e prosegua non appena riceve quello valido.

Non tutti i test necessitano di questa simulazione: quando la funzionalità testata è una trasformazione pura dello stato di gioco (ad esempio l'uso di un oggetto dall'inventario, che restituisce un nuovo `FightState` aggiornato), il test costruisce direttamente lo stato iniziale desiderato e verifica lo stato risultante, senza passare da un canale.

## Copertura
La suite di test offre una copertura approfondita di tutti gli aspetti core della logica di gioco:

   - Eventi e gestione mazzo: verifica di ogni evento relativo all'aggiunta, sacrificio, potenziamento e modifica delle carte;

   - Oggetti: logica di acquisizione, gestione dell'inventario e consumo degli oggetti con i relativi effetti di gioco;

   - Combattimento: copertura di ogni casistica interna alla fase di scontro (calcolo danni, risoluzione dei sigilli/seals, posizionamento e movimento delle carte sulla Board, turni dell'IA);

   - Casi limite e stati terminali: gestione di messaggi inattesi/non validi sul canale di comunicazione, condizioni del mazzo vuoto e verifica della condizione di fine partita (isGameOver).