---

title: Design di Dettaglio
parent: Report

---


# Design di Dettaglio

In questa sezione è documentata la progettazione dettagliata dei singoli componenti del sistema.

### 1. Dominio e Strutture Dati
Le entità fondamentali che definiscono le regole pure e lo stato del gioco:
*   [**Card e Sigilli**](card.md)
*   [**Deck e PlayerHand**](deck.md)
*   [**Board**](board.md)
*   [**GameState**](game_state.md)

### 2. Orchestrazione e Concorrenza
Il motore asincrono che fa girare il gioco e comunica con l'interfaccia grafica:
*   [**GameController e Game Loop**](game_controller.md)
*   [**GameMessagesChannel**](game_messages_channel.md)

### 3. Dinamiche di Gioco
La risoluzione delle interazioni complesse sulla griglia:
*   [**Combattimento, Bot e Oggetti**](fight_and_manager.md)
*   [**Eventi e interazioni**](eventi_e_interazioni.md)