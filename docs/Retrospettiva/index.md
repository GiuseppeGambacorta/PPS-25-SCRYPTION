---
title: Retrospettiva
nav_order: 7
parent: Report
---

# Retrospettiva

## Processo di sviluppo

Il processo di sviluppo che il gruppo si era prefissato è stato sostanzialmente rispettato: sono stati svolti **5 sprint**, ognuno con il relativo **Sprint Backlog** e una **review** conclusiva, mantenendo aggiornato il [Product Backlog](https://github.com/users/GiuseppeGambacorta/projects/4/views/13) ad ogni iterazione. La suddivisione completa dei task per ogni sprint è consultabile nella [tabella riepilogativa degli sprint](https://github.com/users/GiuseppeGambacorta/projects/4/views/3).

Il lavoro è stato svolto prevalentemente in modo individuale sui rispettivi task, con alcune parti comuni progettate e realizzate in collaborazione; lo stesso approccio è stato adottato per la stesura della documentazione. All'inizio di ogni iterazione veniva svolta la *sprint review* dello sprint appena concluso, per valutare il lavoro svolto e pianificare di conseguenza le attività successive.

Prima dell'inizio vero e proprio dello sviluppo è stato dedicato uno **Sprint 0** a una serie di incontri tra i membri del gruppo, con l'obiettivo di analizzare il dominio del gioco, definire e ordinare per priorità il [Product Backlog](https://github.com/users/GiuseppeGambacorta/projects/4/views/13), concordare il processo di sviluppo (metodologia Scrum, ruoli, workflow Git), creare e configurare il repository su GitHub con la relativa struttura del progetto, e definire le principali scelte architetturali di massima.

Gli sprint successivi sono stati così suddivisi:

* **Sprint 1**:
    * Implementazione delle carte da gioco, sia lato *model* che lato *view*
    * Definizione del primo evento di gioco

* **Sprint 2**:
    * Scrittura degli altri eventi di gioco
    * Implementazione del mazzo (*deck*) e della logica di creazione delle carte
    * Realizzazione della *Board*

* **Sprint 3**:
    * Implementazione del *game loop* del combattimento
    * Integrazione della logica dei sigilli (*seals*) all'interno del combattimento

* **Sprint 4**:
    * Completamento della logica di combattimento con aggiunta di un'IA di base e movimentazione delle carte nemiche
    * Introduzione del ViewModel e di un Controller tra View e Model per una maggiore flessibilità architetturale
    * Implementazione di una mappa di base

* **Sprint 5**:
    * Salvataggio della partita (*savegame*)
    * Aggiunta degli oggetti, integrazione nel combattimento e creazione di un nuovo evento dedicato

## Git Workflow

L'intenzione iniziale era quella di seguire un flusso di lavoro strutturato con rilasci continui su `master`. Tuttavia, a causa di una certa inesperienza iniziale con la gestione avanzata del branching, il workflow effettivo ha subito una deviazione: lo sviluppo è proseguito a partire da un branch principale di feature/sviluppo, dal quale sono stati diramati i rami dedicati ai singoli sprint e alle relative feature. Di conseguenza, il branch `master` non è stato aggiornato con costanza al termine di ogni iterazione e non sono state generate release formali intermedie.

Nonostante questa imperfezione procedurale, la collaborazione all'interno del repository condiviso è stata gestita con successo ponendo grande attenzione alle buone pratiche:
* Chiara suddivisione della ownership dei file e dei moduli per minimizzare i conflitti
* Sincronizzazione costante del codice (`pull` preventivi prima di iniziare a lavorare e prima di effettuare il `push`)
* Commit atomici, piccoli e frequenti
* Utilizzo degli strumenti integrati di merge e risoluzione dei conflitti di IntelliJ IDEA per le sezioni condivise

## Valutazione conclusiva

Il ricorso ai test ha garantito solidità alla logica di gioco e ha reso agevoli i numerosi *refactoring* affrontati nel corso degli sprint, pur non essendo stati scritti sistematicamente prima dell'implementazione (si veda la sezione [Testing](../Testing/index.md)).

Oltre alle difficoltà riscontrate nella corretta gestione del branching Git, non è stato facile stare al passo con i carichi di lavoro prefissati per ciascuno sprint. In questo contesto, l'utilizzo di **GitHub Projects** si è rivelato fondamentale per mantenere sott'occhio lo stato di avanzamento del progetto e coordinare le singole iterazioni.

Tra i possibili **miglioramenti futuri** si segnalano:
* Perfezionamento dell'**intelligenza artificiale per il bot**, per renderne il comportamento meno prevedibile
* Introduzione di una **boss fight** con meccaniche dedicate


## Considerazioni Finali

### Cornacchia Enrico
Sviluppare questo progetto si è rivelata un'esperienza stimolante, in quanto mi ha permesso
di applicare concretamente concetti avanzati di programmazione funzionale (come l'F-Bounded Polymorphism o
il trait stackable pattern) alla risoluzione di problemi reali all'interno di un dominio complesso come quello
di un videogioco.
Dal punto di vista organizzativo, sebbene non siamo riusciti a sfruttare integralmente tutte le pratiche
della metodologia Scrum, affrontare queste dinamiche sul campo mi ha permesso di comprenderne a fondo il valore reale.
Ho potuto toccare con mano quanto strumenti come il Kanban e la divisione in Sprint siano fondamentali per coordinare
gli sforzi del team, tracciare i progressi ed evitare colli di bottiglia. Nel complesso, ritengo che il progetto
sia stato un'occasione di crescita sia tecnica che metodologica.

### Gambacorta Giuseppe

### Sanchez Lomas Patricia

Questo progetto mi ha aiutato a crescere in ambiti quali lo sviluppo collaborativo con Git e l’applicazione dei principi fondamentali di questo corso. In particolare, lo sviluppo della parte relativa alle mappe è stato un esercizio impegnativo, ma mi ha insegnato molto sulla potenza offerta da Scala per aspetti quali le strutture dati e gli algoritmi ricorsivi. 
Se dovessi rifare il lavoro, presterei maggiore attenzione all’interfaccia grafica per evitare un errore significativo che fa sì che su alcuni dispositivi alcuni elementi appaiano minuscoli e altri sproporzionatamente grandi.
Nel complesso, sono soddisfatta del risultato, soprattutto considerando che è la prima volta che lavoro a un videogioco, ma sono particolarmente grata per l’esperienza e l’aiuto dei miei compagni.