# Combattimento e Logiche di Gioco

Per gestire le interazioni complesse che avvengono sulla griglia di gioco durante uno scontro, le regole di dominio sono state delegate a moduli specializzati (definiti *Manager*), mantenendole concettualmente distaccate dal puro contenimento dei dati.

*   **Calcolo Danni e Bersagli:** Un gestore dedicato orchestra la sequenza degli attacchi dell'intero turno, interpellando un risolutore specifico per determinare i bersagli esatti in base ai poteri passivi posseduti dalla carta (valutando, ad esempio, se la creatura attacca in volo scavalcando le difese o se colpisce bersagli multipli).
*   **Movimenti:** Un modulo indipendente valuta e applica gli spostamenti sulla griglia, analizzando le abilità di reazione (come lo spostamento per bloccare un nemico) e le capacità di muoversi delle carte a fine turno.
*   **Sacrifici:** Le regole e i costi per l'evocazione sono validati da un ulteriore manager, che si occupa di liberare le caselle appropriate e calcolare il recupero extra di risorse innescato da specifici poteri della creatura sacrificata.

Questo approccio favorisce il SRP. Poiché gli effetti dei poteri vengono calcolati e applicati dinamicamente dai manager solo nella corretta fase del turno, si garantisce una risoluzione lineare anche in presenza di eventi simultanei.

### Avversario Artificiale e Oggetti Consumabili
Il bot è stata progettato seguendo lo **Strategy Pattern**. L'orchestrazione delle mosse dell'avversario è disaccoppiata in strategie intercambiabili (es. `RandomBotStrategy` per comportamenti base), che si interfacciano con lo stato del combattimento attraverso le medesime logiche e limitazioni di un giocatore umano. Condividendo queste restrizioni, il sistema garantisce uniformità procedurale ed esclude che il Bot possa generare stati invalidi.

Infine, l'architettura prevedeva opzionalmente l'utilizzo di oggetti consumabili monouso a disposizione del giocatore. Quando attivati, questi oggetti applicano la loro specifica mutazione allo stato corrente. Affinché ciò avvenga in totale sicurezza, le alterazioni provocate dagli oggetti vengono processate dallo stesso sistema di scambio di messaggi standard, assicurando che le modifiche improvvise della griglia avvengano in modo asincrono, evitando sovrapposizioni e corruzioni di stato durante lo svolgimento del round.