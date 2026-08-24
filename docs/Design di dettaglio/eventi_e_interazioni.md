# Eventi

Il concetto di evento rappresenta l'unità fondamentale di lavoro del motore di gioco. Un evento è modellato come una transizione di stato che, a partire dallo stato corrente e sfruttando il canale di comunicazione, produce il nuovo stato della partita.

Questa scelta ha un impatto architetturale rilevante: tutte le dinamiche di gioco (pescare una carta per il mazzo, potenziare una creatura al falò, effettuare un sacrificio, combattere o muoversi sulla mappa) vengono ricondotte a un'interfaccia comune, indipendentemente dalla loro complessità interna.

Le caratteristiche chiave di questo approccio includono:

* **Flessibilità di esecuzione**: un evento può essere immediato (computa direttamente il nuovo stato) oppure interattivo, richiedendo uno o più scambi di messaggi sul canale per acquisire le scelte del giocatore prima di calcolare l'esito finale;
* **Componibilità e modularità**: grazie a una firma uniforme, gli eventi possono essere composti in pipeline, isolati per i test e sostituiti senza impattare la struttura del motore di gioco;
* **Disaccoppiamento della logica**: il Model rimane l'unico responsabile delle regole racchiuse in ciascun evento. Il canale funge esclusivamente da mezzo di sincronizzazione e trasporto verso la GUI, rendendo l'evento completamente agnostico rispetto alle modalità di rendering a schermo.