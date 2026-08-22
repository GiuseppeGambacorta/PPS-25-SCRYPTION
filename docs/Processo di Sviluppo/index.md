---

title: Processo di sviluppo
nav_order: 1
parent: Report

---

# Processo di sviluppo

Il processo di sviluppo adottato si ispira a **Scrum**, strutturato su *sprint* iterativi e su un insieme di *task* pianificati. Il tracciamento e la gestione delle attività sono stati curati tramite **GitHub Projects**, sfruttando sia viste a **tabella** sia board **Kanban**:
- Una vista (tabella/Kanban) per il **Product Backlog** generale, ordinato per priorità;
- Viste dedicate (tabella/Kanban) per ciascuno **Sprint**, utilizzate per monitorare lo stato di avanzamento e l'assegnazione dei task dell'iterazione corrente.

## Ruoli

Il team di sviluppo è composto da tre membri: **Giuseppe Gambacorta**, **Enrico Cornacchia** e **Patricia Sanchez Lomas**. All'interno del gruppo sono state definite le seguenti responsabilità:
- **Giuseppe Gambacorta** ha assunto il ruolo di **Product Owner**, coordinando le priorità del backlog, la pianificazione degli sprint e l'allineamento complessivo del gruppo.
- **Patricia Sanchez Lomas** ha ricoperto il ruolo di **Domain Expert**, grazie alla sua conoscenza approfondita delle regole e delle dinamiche del gioco, guidando la corretta modellazione del dominio.
- **Enrico Cornacchia**, insieme a tutti i componenti del team, ha partecipato attivamente allo sviluppo software, alla progettazione architetturale e alle sessioni di review.

## Sprint planning

Gli sprint hanno avuto durata di circa 15 ore a testa. All'inizio di ogni sprint sono stati definiti l'obiettivo da raggiungere e, tramite la gestione dello sprint su GitHub Projects, i task da svolgere con il relativo assegnatario e la stima dell'effort. Al termine di ogni sprint è stata effettuata una **Sprint Review** per verificare il lavoro svolto, aggiornare il backlog e pianificare l'iterazione successiva.

## Definition of Done

Un task o una funzionalità è considerata **Done** solo a seguito del completamento e dell'accettazione della relativa **Pull Request** integrata nel repository, con la contestuale chiusura automatica delle relative **issue collegate**. 

I criteri di accettazione includono:
- Copertura della logica tramite test automatici con esito positivo;
- Presenza della **Scaladoc** su tutte le API pubbliche dei moduli implementati;
- Validazione manuale del corretto funzionamento e assenza di ambiguità nell'interfaccia utente.

## Documentazione

La documentazione è realizzata in formato **Markdown**, contenuta nella directory `docs`, e pubblicata come sito statico tramite **GitHub Pages** (tema [just-the-docs](https://just-the-docs.com/), diagrammi resi con **Mermaid**). Il codice è inoltre corredato di **Scaladoc** su tutte le API pubbliche dei moduli.

## Versioning e Branching Strategy

Il controllo di versione è gestito con **Git** e ospitato su **GitHub**. La strategia di branching adottata si basa su:
- Un branch dedicato per ciascuno **sprint** (es. `sprint-1`, `sprint-2`, ...), utilizzato come linea di integrazione per l'iterazione in corso;
- **Feature branch** specifici creati a partire dal branch dello sprint per lo sviluppo dei singoli task o funzionalità (aprendo una Pull Request verso lo sprint branch per completare la *Definition of Done*);
- Il branch principale (`main`/`master`) destinato a raccogliere le integrazioni stabili a conclusione del progetto.

## Strumenti

- **Linguaggio**: Scala 3 (versione 3.3.5).
- **Build tool**: sbt.
- **Interfaccia Grafica**: Swing (Scala Swing / Java Swing).
- **Testing**: ScalaTest (`AnyFunSuite`).
- **Project Management**: GitHub Projects (viste a tabella e board Kanban per Product e Sprint Backlog) e GitHub Issues/Pull Requests.
- **IDE**: IntelliJ IDEA.
- **Version control e documentazione**: Git, GitHub e GitHub Pages.
