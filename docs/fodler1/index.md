---
layout: default
title: Architettura del Sistema - Folder 1
---

# Specifica dei requisiti
   Durante l'analisi del problema sono stati individuati i seguenti requisiti del sistema da realizzare.
## Business
  Creare un sistema in grado di poter effettuare una partita a scacchi, completa di tutte le regole di cui è provvisto il gioco originale.
## Utente
- utente deve poter scegliere l'evento - strada da intraprendere

* **Viste Dedicate:** Ogni evento (combattimento, fuoco da campo, altare, ecc.) deve possedere una GUI dedicata.
* **Interfaccia del combattimento:** Durante il combattimento, l'utente deve poter visualizzare in modo chiaro e intuitivo:
  * La propria mano di carte (senza limiti di capienza).
  * La griglia di gioco con le carte in campo.
  * Il saldo corrente della risorsa Ossa.
  * La bilancia dei punti che mostra il punteggio attuale.
  * I mazzi da cui è possibile pescare.
  * Gli attributi attuali delle carte, sia statiche che dinamiche 

## Funzionali
- Ad ogni avvio di una nuova partira il giocatore inizia con la stessa configurazione del mazzo.
- Ad ogni avvio di una nuova partira il primo evento sarà una battaglia.
- Dopo una battaglia, ho sempre un altro tipo di evento, o due.
- A volte devi scegliere una strada.
- Gli eventi : 
  - Carte Normali (Tre carte pelli/animali): Scegli una carta tra tre opzioni casuali.
  
  - Fuoco da Campo (Campfire): Scegli se potenziare la Potenza (+1) o la Salute (+2) di una creatura. Puoi rischiare di lasciare la carta più a lungo sul fuoco per ulteriori potenziamenti, ma i viandanti potrebbero mangiarla (se mangiano la  Vipera o l'Ermellino con veleno, i viandanti muoiono e i successivi fuochi saranno privi di rischi).
  
  - Altare dei Sacrifici (Sacrifice / Stone Altar): Sacrifica una carta per trasferire il suo Simbolo (Sigillo) in modo permanente su un'altra carta.
  
  - Processo / Prova (Trial): Sottoponi il tuo mazzo a una prova casuale (es. somma dei costi, numero di sigilli, salute totale). Se la superi, scegli una carta speciale potenziata con sigilli extra.
  
  - Micologi (Mycologists): Fondono due carte identiche che possiedi nel tuo mazzo, sommandone Potenza e Salute (e combinando i sigilli).


- ogni carta, deve avere, attacco, vita, sacrificio ( ne esistono due) , potere. (possono essere anche 0 per ognuna di loro)
- carte oggetti (albero)
- alcune carte sono piu rare di altre.

- poteri disponibili:
    - Sigilli di Attacco e Posizionamento
    -  Volante (Airborne): La creatura sorvola la carta nemica di fronte e colpisce direttamente l'avversario/la bilancia.
            Muro / Parata speciale (Mighty Leap): Blocca gli attacchi delle creature con il sigillo Volante.
    
  
      - Biforcazione (Bifurcated Strike): La creatura attacca le due caselle adiacenti invece di quella di fronte.
      - Triforcazione (Trifurcated Strike): La creatura attacca la casella di fronte e le due caselle adiacenti (3 attacchi totali).
      - Toccata della Morte (Touch of Death): Qualsiasi creatura danneggiata da questa carta viene distrutta all'istante, indipendentemente dalla sua salute.
  
      - Guardiano (Guardian): Quando il nemico gioca una carta su una casella vuota, questa creatura si sposta in quella casella per bloccarla.
      
      - Spinta (Sprinter): Alla fine del tuo turno, la creatura si sposta di una casella nella direzione indicata dalla freccia.
    
      - Immortale (Unkillable): Quando questa creatura muore, torna direttamente nella tua mano anziché finire nel cimitero.
      - Ossa a Volontà (Bone King): Quando questa creatura muore, fornisce 4 Ossa invece di 1.
  
      - cat power

- Regole Gioco:
  - Ci sono due deck, uno con le creature e un altro con solo sacrifici, questo è infinito.
  - Ogni volta che una creatura muore vengono collezionate ossa, utilizzabili per sacrificio.
  - le creature muoiono quando raggiungono 0 vita.
  - il combattimento finisce quando uno dei due giocatori porta la bilancia a 6 punti.
  - alcune carte possono essere giocate solo su sacrificio (specificato sulla carta)
  - se davanti a una carta non è presente una creatura, l'attacco viene inflitto al giocatore, altrimenti alla creatura.
  - Nessun limite sul numero di carte nella mano
  - Devi decidere da quale deck pescare.
  - All'inizio, devi pescare una carta
  - La tabella ha 3 righe, il giocatore puo posizionare le carte solo nella prima, il nemico le posiziona anche lui nella sua prima e poi vanno avanti.
  - alternarsi dei turni, il primo turno è semrpe del giocatore
 





## Non Funzionali
- Aggiungere facilmente nuove carte al gioco, aggiungere facilmente eventi, aggiungere facilmente poteri
-  L'interfaccia grafica deve ricordare il gioco originale. Il sistema deve fornire un'interfaccia grafica che aiuti il giocatore nella scelta delle azioni in maniera intuitiva e rapida.
## Di implementazione
  Utilizzo di Scala 3.x
  Utilizzo di JDK 25+
## Opzionali
- objects
- totem, and creatures family. with special events where to choose a family
- save game
- music
- other effects
- bosses, AI
- 

