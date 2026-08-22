### GameState

Il **GameState** rappresenta lo snapshot completo e immutabile della partita in un dato istante temporale. È concepito come un mero contenitore di dati privo di comportamento attivo, aggrega tutte le informazioni necessarie a descrivere lo stato corrente della sessione di gioco (mazzo, inventario ed esito della partita).

I principi cardine del suo design comprendono:

* **Transizioni pure e immutabili**: ogni progressione o azione di gioco non altera l'istanza corrente, ma calcola e restituisce un nuovo `GameState` perfettamente coerente con le modifiche apportate;
* **Thread-safety e assenza di side effect**: trattandosi di una struttura dati pura priva di logica interna mutabile, può essere scambiata liberamente tra i diversi componenti architetturali (Model, canali di sincronizzazione e ViewModel) eliminando alla radice race condition o accessi concorrenti non sicuri;
* **Interfaccia per gli eventi**: costituisce il punto di ingresso e di uscita deterministico per qualsiasi logica di gioco, dove ogni evento riceve in input lo stato corrente e restituisce il nuovo stato calcolato.