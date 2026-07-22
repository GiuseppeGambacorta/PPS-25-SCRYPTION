---
layout: default
title: Requisiti
---

# Specifica dei requisiti
Durante l'analisi del problema sono stati individuati i seguenti requisiti del sistema da realizzare.
## Business
Creare un sistema in grado di poter effettuare una partita a carte stile Inscryption, completa di tutte le regole, meccaniche di sacrificio, gestione delle risorse e dinamiche della bilancia provviste nel gioco originale.


## Requisiti Funzionali Utente

* **Avvio Nuova Partita:** L'utente deve poter avviare una nuova partita in qualsiasi momento, resettando lo stato del mazzo, le riserve e il progresso di gioco.
* **Scelta del Percorso:** L'utente deve poter selezionare autonomamente il nodo/evento successivo da affrontare lungo il percorso, senza una rappresentazione grafica della mappa.
* **Viste Dedicate per Evento:** Ogni tipologia di evento deve essere fornita di una GUI dedicata.
    * **Schermata Iniziale / Menu Principale:** Per avviare una nuova partita o accedere alle impostazioni.
    * **Vista Selezione Percorso:** Per la scelta del nodo/evento successivo.
    * **Vista Combattimento:** Interfaccia principale dello scontro (Standard e Boss).
    * **Vista Scelta Nuova Carta:** Per selezionare 1 carta tra 3 opzioni casuali.
    * **Vista Fuoco da Campo:** Per il potenziamento di Potenza o Salute.
    * **Vista Altare dei Sacrifici:** Per il trasferimento permanente dei sigilli.
    * **Vista Prova (Trial):** Per il test degli attributi del mazzo.
    * **Vista Micologi:** Per la fusione di carte identiche.


* **Durante un combattimento**, l'interfaccia deve mostrare in modo chiaro, distinto e aggiornato in tempo reale i seguenti elementi:

    * **Mano di Carte:** Visualizzazione delle carte attualmente in possesso del giocatore, **senza limiti di capienza**.
    * **Griglia di Gioco:** Campo tattico diviso in slot per il posizionamento e il tracciamento delle carte sul terreno (Giocatore vs Avversario).
    * **Mazzi di Pescata:** Indicatori e punti di interazione distinti per i mazzi da cui è possibile pescare (es. *Mazzo Base* e *Mazzo Scoiattoli*).
    * **Saldo Ossa:** Display con il conteggio attuale della risorsa Ossa a disposizione del giocatore.
    * **Bilancia dei Punti:** Indicatore visivo del punteggio pendente che traccia i danni e determina la condizione di vittoria o sconfitta.
    * **Attributi delle Carte:** Visualizzazione degli attributi delle carte presenti sia in mano che sulla griglia:
        * **Attributi Statici:** Nome, illustrazione, costo di evocazione (Sangue/Ossa) e sigilli originari.
        * **Attributi Dinamici:** Valori correnti di Attacco e Salute, inclusi danni subiti, modificatori e buff/debuff temporanei.

## Requisiti Funzionali di Sistema

### 1. Gestione della Partita e del Flusso di Gioco
* **Condizioni Iniziali:** Ad ogni avvio di una nuova partita, il giocatore inizia sempre con la medesima configurazione di mazzo predefinita.
* **Gestione Mazzo:** Man mano che il gioco prosegue, le carte verranno rimosse/aggiunte/modificate in base agli eventi, i modificatori che avvengono durante le partire non hanno nessun effetto sul mazzo del giocatore nei combattimenti successivi
* **Al termine della partita:** Al termine della partita, il giocatore si ritroverà nella schermata iniziale del gioco, dovendo ripartire da zero.
* **Sequenza degli Eventi:**
    * Il primo evento di ogni nuova partita è sempre un combattimento.
    * Al termine di ogni battaglia viene sempre proposto un evento successivo o una scelta tra due percorsi/eventi distinti, non deve essere proposta un'altra battaglia.
* **Tipologia eventi sulla mappa:**
    * **Scelta nuova carta :** Presenta una scelta tra tre carte casuali, il giocatore ne deve scegliere una che verrà poi aggiunto al proprio mazzo.
    * **Fuoco da Campo:** Permette di aumentare la Potenza (+1) o la Salute (+2) di una creatura scelta.
        * *Rischio:* Tentare potenziamenti multipli sulla stessa carta aumenta la probabilità che i viandanti la mangino, distruggendola.
        * *Effetto Collaterale (Veleno):* Se viene mangiata una carta velenosa (es. Vipera o Ermellino con veleno), i viandanti muoiono e tutti i successivi fuochi da campo della partita saranno privi di rischi.
    * **Altare dei Sacrifici:** Permette di sacrificare una carta per trasferirne in modo permanente il Sigillo su un'altra carta del mazzo.
    * **Prova:** Sottopone il mazzo a un test su attributi casuali (es. costo totale, numero di sigilli, salute totale). Il superamento della prova garantisce una carta speciale potenziata con sigilli aggiuntivi.
    * **Micologi:** Fondono due carte identiche del mazzo in un'unica carta, sommandone Potenza e Salute e combinandone i rispettivi sigilli.

### 2. Struttura delle Carte
* **Anatomia della Carta:** Ogni carta possiede i seguenti attributi (ciascuno con valore minimo pari a 0):
    * **Attacco** (Potenza)
    * **Salute** (Vita)
    * **Costo di Evocazione** (diviso nelle due tipologie di risorsa: Sangue da sacrifici o Ossa)
    * **Sigillo / Potere speciale**
* **Classificazione e Rarità:**
    * Le carte presentano diversi livelli di rarità.
    * Esistono carte oggetto speciali di supporto (es. Albero).

### 3. Sistema di Combattimento e Griglia
* **Inizializzazione e Turni:**
    * Il primo turno della battaglia spetta sempre al giocatore.
    * All'inizio di ogni proprio turno, il giocatore è obbligato a pescare una carta, scegliendo autonomamente da quale dei due mazzi pescare:
        * **Mazzo Carte (Creature):** Contiene le carte del mazzo corrente del giocatore.
        * **Mazzo Sacrifici (Scoiattoli):** Contiene carte risorsa infinite da usare per i sacrifici di Sangue.
* **Layout del Campo di Gioco:**
    * La griglia si compone di **tre righe per quattro colonne:
        * Il giocatore può posizionare le proprie carte esclusivamente nella riga più vicina a sé (riga frontale).
        * L'avversario posiziona le sue carte sulla propria riga e queste avanzano progressivamente verso la riga centrale di scontro.
* **Risoluzione degli Attacchi:**
    * Un'unità attacca la casella di fronte: se è presente una creatura nemica, il danno viene inflitto alla sua Salute; se la casella è vuota, il danno viene inflitto direttamente all'avversario e calcolato sulla bilancia.
    * Le creature muoiono immediatamente quando la loro Salute scende a 0.
* **Condizione di Fine Combattimento:** La battaglia termina instantaneamente quando uno dei due contendenti porta lo scarto della bilancia a 6 punti a proprio favore (vittoria a +6, sconfitta a -6). In questo caso il giocatore dovrà iniziare da capo il gioco.
* **Meccanica delle Ossa:** Quando una qualsiasi creatura sulla griglia muore (raggiunge 0 di Salute), il saldo Ossa del giocatore aumenta di +1 (salvo bonus da sigilli).


### 4. Sigilli e Abilità Speciali (Poteri)
Il sistema gestisce l'applicazione e l'esecuzione dei seguenti sigilli:
* **Volante (Airborne):** Ignora la carta nemica presidiante la casella di fronte e infligge danno diretto all'avversario/bilancia.
* **Muro / Parata speciale (Wall):** Intercetta e blocca gli attacchi diretti inflitti da creature dotate del sigillo *Volante*.
* **Biforcazione (Bifurcated Strike):** La creatura esegue 2 attacchi indirizzati alle due caselle adiacenti anziché a quella frontale.
* **Triforcazione (Trifurcated Strike):** La creatura esegue 3 attacchi totali: uno sulla casella frontale e due sulle caselle adiacenti.
* **Toccata della Morte (Touch of Death):** Qualsiasi creatura subisca un danno (anche pari a 1) da questa carta viene distrutta all'istante, a prescindere dai suoi punti Salute residui.
* **Guardiano (Guardian):** Quando il nemico gioca una carta su una casella vuota, questa creatura si sposta automaticamente su quel nodo per bloccarla.
* **Spinta (Sprinter):** Al termine del turno del giocatore, la creatura si sposta di una casella nella direzione indicata dal proprio indicatore.
* **Immortale (Unkillable):** Alla morte della carta, questa non finisce nel cimitero ma ritorna immediatamente nella mano del giocatore.
* **Ossa a Volontà (Bone King):** Alla morte della carta, assegna al giocatore 4 Ossa anziché 1.
* **Sacrificio Infinito:** Quando la carta viene usata come sacrificio, rimane sul terreno di gioco invece che andare al cimitero.



## Non Funzionali
### 1. Estensibilità
* **Estensibilità del Catalogo Carte:** Il sistema deve essere progettato in modo da consentire l'aggiunta di nuove carte (con attributi, costi e rarità personalizzate) tramite una semplice configurazione di dati, senza richiedere la modifica della logica di base del gioco.
* **Modularità degli Eventi:** La struttura architetturale deve permettere la creazione e l'integrazione fluida di nuovi eventi di percorso/mappa mediante un approccio a moduli indipendenti.
* **Estensibilità dei Poteri (Sigilli):** Il motore di gioco deve consentire l'implementazione rapida e flessibile di nuovi sigilli o abilità speciali senza impattare la logica delle carte già esistenti.

### 2. Interfaccia Grafica ed Esperienza Utente (GUI & UX)
* **Usabilità e Intuitività:** La GUI deve essere progettata per supportare l'utente nell'esecuzione delle azioni in modo immediato e intuitivo, riducendo al minimo il numero di click necessari per posizionare carte, effettuare sacrifici o interagire con gli elementi di gioco.
* **Chiarezza Visiva:** Gli stati complessi (come modificatori di attributi, sigilli attivi, valore attuale della bilancia e saldo risorse) devono essere chiaramente identificabili a colpo d'occhio tramite indicatori visivi ed elementi grafici dedicati.
## Di implementazione
- Utilizzo di Scala 3.x
- Utilizzo di JDK 25+
- Utilizzo di ScalaTest e ScalaCheck
## Opzionali



* Introduzione di una riserva di oggetti monouso (es. *Forbici*, *Pinza*, *Bottiglia di Scoiattolo*, *Clessidra*) utilizzabili dal giocatore durante la fase di combattimento per modificare istantaneamente lo stato della griglia o delle risorse.

* Classificazione delle carte in famiglie o tribù (es. *Canidi*, *Apostoli*, *Insetti*, *Rettili*, *Volatili*).
* Gestione degli eventi dedicati al Totem, dove il giocatore può combinare la **Testa** (scelta della famiglia) e la **Base** (scelta del sigillo) per applicare un effetto passivo permanente a tutte le carte appartenenti a quella specifica tribù.

* Implementazione di battaglie speciali contro i Boss dotate di meccaniche uniche a fasi (es. il *Cacciatore*, il *Pescatore*, il *Pellicciaio*), con regole e griglie modificate.
* Sviluppo di un motore IA più avanzato per l'avversario, in grado di valutare la giocata ottimale, pianificare sacrifici ed eseguire combo basate sulle carte in arrivo nei turni successivi.



* Meccanismo di serializzazione dello stato di gioco per salvare il progresso attuale (mappa, struttura del mazzo, Ossa accumulate, reliquie e stato dei fuochi/totem) e riprendere la partita in un secondo momento.


* Gestione di tracce audio di sottofondo reattive, in grado di variare d'intensità a seconda della vista attiva (es. mappa, evento, combattimento standard o battaglia contro un Boss).

* Aggiunta di altri poteri per le carte.

