---
title: Retrospettiva
nav_order: 7
parent: Report
---

# Retrospettiva

## Processo di sviluppo

Il processo di sviluppo che il gruppo si era prefissato è stato rispettato: sono stati svolti **5 sprint**, ognuno
con il relativo **Sprint Backlog** e una **review** conclusiva, mantenendo aggiornato il
[Product Backlog](https://github.com/users/GiuseppeGambacorta/projects/4/views/13) ad ogni iterazione. La suddivisione
completa dei task per ogni sprint è consultabile nella [tabella riepilogativa degli sprint](https://github.com/users/GiuseppeGambacorta/projects/4/views/3).

La quasi totalità dei task pianificati per ogni sprint è stata completata all'interno dello sprint stesso. Il lavoro è
stato svolto prevalentemente in modo individuale sui rispettivi task, con alcune parti comuni progettate e realizzate
in collaborazione; lo stesso vale per la stesura della documentazione. All'inizio di ogni sprint veniva svolta la
*sprint review* dello sprint appena concluso, per valutare il lavoro svolto e pianificare di conseguenza le attività
successive.

Prima dell'inizio vero e proprio dello sviluppo è stato dedicato uno **Sprint 0** a una serie di incontri tra i membri
del gruppo, con l'obiettivo di analizzare il dominio del gioco, definire e ordinare per priorità il
[Product Backlog](https://github.com/users/GiuseppeGambacorta/projects/4/views/13), concordare il processo di sviluppo
(metodologia Scrum, ruoli, workflow Git), creare e configurare il repository su GitHub con la relativa struttura del
progetto, e definire le principali scelte architetturali di massima.

Gli sprint successivi sono stati così suddivisi:

1. **Sprint 1** — oltre all'implementazione di una prima carta, sia lato *model* che lato *view*, e di un primo evento
   di gioco, buona parte dello sprint è stata dedicata allo studio: del linguaggio Scala 3 e dei suoi costrutti, delle
   librerie da utilizzare (ScalaTest per i test, Swing per la GUI) e delle scelte necessarie per impostare
   concretamente l'architettura definita nello Sprint 0;
2. **Sprint 2** — scrittura degli altri eventi di gioco, implementazione del mazzo (*deck*) e della logica di creazione delle carte, realizzazione della *Board*;
3. **Sprint 3** — *game loop* del combattimento, con integrazione della logica dei sigilli (*seals*);
4. **Sprint 4** — completamento della logica di combattimento, con l'aggiunta di un'IA di base e della movimentazione delle carte nemiche; introduzione del ViewModel e di un Controller tra View e Model per una maggiore flessibilità architetturale; implementazione di una mappa di base;
5. **Sprint 5** — lavoro sui requisiti opzionali: salvataggio della partita (*savegame*) e aggiunta degli oggetti, integrati nel combattimento tramite un nuovo evento dedicato al loro utilizzo.

## Git Workflow

Il branch `master` è stato lasciato intatto e utilizzato esclusivamente per le versioni stabili corrispondenti alle
release. Per ogni sprint è stato creato un **branch dedicato allo sprint**, su cui confluivano le singole **feature
branch** aperte dai membri del gruppo per lavorare sui rispettivi task; al termine dello sprint il branch veniva
integrato in `master` in corrispondenza della relativa release.

Poiché il repository è condiviso e tutti i componenti hanno accesso in scrittura, l'accento è stato posto sulle pratiche
di collaborazione: suddivisione della *ownership* dei file per ridurre i conflitti, `pull` prima di iniziare e prima di
ogni `push`, commit piccoli e frequenti e utilizzo degli strumenti di merge di IntelliJ IDEA per le parti comuni.

Le **release** sono state prodotte in modo **incrementale**, una per sprint, e pubblicate manualmente dalla sezione
*Releases* di GitHub seguendo il **Semantic Versioning**.

## Valutazione conclusiva

Il ricorso ai test ha comunque dato solidità alla logica di gioco e ha reso agevoli i numerosi *refactoring* affrontati
nel corso degli sprint, pur non essendo stati scritti sistematicamente prima dell'implementazione (si veda la sezione
[Testing](Testing/)).

Tra i possibili **miglioramenti futuri**: l'introduzione di un'**intelligenza artificiale per il bot**, per renderne
il comportamento meno prevedibile, e l'aggiunta di una **boss fight**.
