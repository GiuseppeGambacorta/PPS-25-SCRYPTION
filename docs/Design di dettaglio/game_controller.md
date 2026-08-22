### GameController e il Game Loop

Il **GameController** è il componente deputato all'orchestrazione complessiva: non include logica di dominio, ma ha la responsabilità di avviare, coordinare e far progredire l'intero ciclo di vita della partita.

Le sue responsabilità principali si articolano in:

* **Bootstrap**: predispone lo stato iniziale del gioco (`GameState`) e la configurazione della mappa degli eventi, avviando l'esecuzione del ciclo di gioco su un **thread dedicato** per preservare la reattività dell'interfaccia grafica (EDT);
* **Game Loop**: a ogni iterazione seleziona l'evento corrente stabilito dalla progressione della mappa, istanzia un canale di comunicazione dedicato (`GameMessagesChannel`), richiede l'aggiornamento della vista corrispondente e delega l'elaborazione della logica all'evento stesso, ricevendone in output il nuovo stato calcolato;
* **Verifica delle condizioni di terminazione**: al completamento di ciascun evento, analizza se la partita è giunta al termine (es. sconfitta o completamento della mappa) oppure se sono presenti ulteriori nodi da affrontare. In caso affermativo reitera il ciclo con lo stato aggiornato, altrimenti finalizza la sessione notificando l'esito tramite callback al livello superiore.

Il Controller preserva così una responsabilità strettamente coordinativa: scandisce i tempi e seleziona le fasi da attivare, rimanendo totalmente agnostico rispetto alla specifica computazione interna di ciascun evento.