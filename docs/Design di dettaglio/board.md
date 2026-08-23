# Board

La Board rappresenta il campo di gioco, modellato come una griglia a righe e colonne di dimensioni prefissate, in cui ogni cella può ospitare una carta oppure risultare vuota.

Strutturalmente la Board è composta da un insieme di righe, e ciascuna riga è a sua volta composta da un insieme di celle: ogni cella ospita uno Slot, il quale può contenere una carta oppure risultare vuoto. Questa scelta rende esplicito nel modello il fatto che una posizione della griglia possa non essere occupata, evitando la necessità di valori sentinella o controlli impliciti sparsi nel codice.

Le principali caratteristiche di progettazione includono:
-  Immutabilità dello stato: analogamente a Deck e PlayerHand, la struttura è puramente immutabile. Qualsiasi operazione di manipolazione (posizionamento, rimozione di una carta o sostituzione di una riga) produce una nuova istanza coerente della Board, preservando l'integrità dello stato precedente;
- Validazione dei confini: l'accesso e il posizionamento sulla griglia vengono validati rispetto ai limiti dimensionali della board, impedendo riferimenti a coordinate non valide o esterne alla matrice di gioco;
- Incapsulamento della struttura interna: la rappresentazione dati sottostante è completamente celata. Il dominio interagisce con la board esclusivamente mediante l'interfaccia pubblica e le operazioni esposte, assicurando la medesima sicurezza semantica adottata per le altre collezioni del sistema.

<pre class="mermaid">
classDiagram
    direction TB

    class Board
    class BoardRow
    class Slot
    class Card

    Board "1" *-- "3" BoardRow : contains
    BoardRow "1" *-- "4" Slot : contains
    Slot o-- "0..1" Card : contains
</pre>

<script type="module">
  import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs';
  mermaid.initialize({ startOnLoad: true, theme: 'neutral' });
</script>