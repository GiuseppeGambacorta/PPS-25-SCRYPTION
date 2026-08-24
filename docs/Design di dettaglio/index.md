---

title: Design di Dettaglio
parent: Report

---

# Design di Dettaglio

In questa sezione è documentata la progettazione dettagliata dei singoli componenti del sistema.

### 1. Modello di Dominio e Logica di Gioco
Le entità fondamentali, le strutture dati e le regole che governano lo stato e le interazioni di gioco:
*   [**Card e Sigilli**](card.md)
*   [**Deck e PlayerHand**](deck.md)
*   [**Board e GameState**](board.md)
*   [**Combattimento, Bot e Oggetti**](fight_and_manager.md)
*   [**Mappa**](map.md)
*   [**Eventi e Interazioni**](eventi_e_interazioni.md)

### 2. Orchestrazione e Concorrenza
Il layer applicativo responsabile del flusso di esecuzione e della comunicazione asincrona tra modello e interfaccia grafica:
*   [**GameController e Game Loop**](game_controller.md)
*   [**GameMessagesChannel**](game_messages_channel.md)