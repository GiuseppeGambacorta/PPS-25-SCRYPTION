Eventi 

Il concetto di evento è la vera unità di lavoro del motore di gioco. Un evento è modellato come una funzione pura che, a partire dallo stato corrente e da un canale di comunicazione, produce il nuovo stato risultante.

Questa scelta ha un effetto molto importante sull'architettura: tutte le attività di gioco come: pescare una carta nuova per il mazzo, potenziare una creatura al falò, effettuare un sacrificio, combattere, muoversi sulla mappa, vengono ricondotte a un'unica interfaccia comune, indipendentemente da quanto siano semplici o articolate internamente.

Un evento può essere immediato (calcola subito il nuovo stato) oppure interattivo, cioè richiedere uno o più scambi di messaggi con l'interfaccia grafica attraverso il canale, per raccogliere le scelte del giocatore prima di determinare l'esito;
Essendo funzioni con una firma uniforme, gli eventi sono componibili, testabili in isolamento e sostituibili tra loro senza che il resto del sistema debba conoscerne i dettagli interni;
Il Model resta l'unico responsabile della logica racchiusa in ogni evento; il canale è semplicemente il tramite con cui l'evento comunica con la GUI, senza che l'evento stesso debba conoscere come i messaggi vengano poi mostrati a schermo.