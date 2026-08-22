### Board

La **Board** rappresenta il campo di gioco, modellato come una griglia a righe e colonne di dimensioni prefissate, in cui ogni cella può ospitare una carta oppure risultare vuota.

Le principali caratteristiche di progettazione includono:

* **Immutabilità dello stato**: analogamente a `Deck` e `PlayerHand`, la struttura è puramente immutabile. Qualsiasi operazione di manipolazione (posizionamento, rimozione di una carta o sostituzione di una riga) produce una nuova istanza coerente della `Board`, preservando l'integrità dello stato precedente;
* **Validazione dei confini**: l'accesso e il posizionamento sulla griglia vengono validati rispetto ai limiti dimensionali della board, impedendo riferimenti a coordinate non valide o esterne alla matrice di gioco;
* **Incapsulamento della struttura interna**: la rappresentazione dati sottostante è completamente celata. Il dominio interagisce con la board esclusivamente mediante l'interfaccia pubblica e le operazioni esposte, assicurando la medesima sicurezza semantica adottata per le altre collezioni del sistema.

 