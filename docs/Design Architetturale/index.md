---
layout: default
title: Design architetturale
parent: Report
nav_order: 3
---

# Design architetturale

Il pattern architetturale adottato si basa su una variante del modello **Model-View-ViewModel (MVVM)** orchestrata da un **Controller** centrale. L'architettura è stata progettata per garantire un disaccoppiamento netto e asincrono tra la logica di dominio (Model) e il livello di presentazione (Swing View).

Model e View vivono su **thread di esecuzione separati**: la sincronizzazione e lo scambio dati avvengono per mezzo di un canale di messaggi (`GameMessagesChannel`) che funge da **Monitor** concorrente (produttore-consumatore thread-safe).

<pre class="mermaid">
classDiagram
    direction TB

    class GameController
    class SwingView
    class ViewModel
    class Model
    class GameMessagesChannel

    GameController ..> Model : instantiates / controls
    GameController ..> ViewModel : instantiates
    GameController ..> GameMessagesChannel : creates & injects

    SwingView --> ViewModel : forwards input
    ViewModel ..> SwingView : updates state

    ViewModel --> GameMessagesChannel : sends actions / receives events
    Model --> GameMessagesChannel : sends events / receives actions
</pre>

A differenza di un approccio monolitico o guidato strettamente da chiamate bloccanti, in questa architettura il Controller ha una responsabilità focalizzata: inizializza le componenti, inietta gli estremi del canale di comunicazione tra il Model e il ViewModel, e gestisce il ciclo principale di esecuzione (Game Loop).

## Model

Il Model incapsula lo stato interno e le regole del gioco. Rimane completamente agnostico rispetto alla tecnologia grafica utilizzata.

- Riceve messaggi tipizzati dal canale (azioni del giocatore), calcola le transizioni di stato e deposita sul canale i relativi messaggi di notifica o di richiesta input;
- È completamente disaccoppiato dalla GUI e isolabile per l'esecuzione di test unitari automatici.

## ViewModel

Il ViewModel agisce da intermediario intelligente tra l'interfaccia grafica e il canale del motore di gioco:

- Riceve ed elabora gli eventi provenienti dal Model attraverso il canale, trasformandoli in uno stato facilmente consumabile e visualizzabile dall'interfaccia grafica;
- Espone proprietà e comandi per la View;
- Raccoglie le intenzioni e gli input dell'utente dalla View, traducendoli in messaggi/azioni formattati da inoltrare sul canale verso il Model.

## View

La View è sviluppata con Swing e si occupa esclusivamente della visualizzazione e del rendering grafico dei componenti a schermo.

- Osserva lo stato esposto dal ViewModel e aggiorna di conseguenza gli elementi dell'interfaccia grafica (pannelli, bottoni, carte, animazioni);
- Raccoglie i click e gli eventi generati dall'utente inoltrandoli direttamente al ViewModel, senza interagire mai con il Model o con il canale di gioco sottostante.

## Controller e Game Loop

Il GameController rappresenta il punto di coordinamento principale:

- **Bootstrap**: all'avvio crea le strutture del canale, istanzia il Model, il ViewModel e collega la View;
- **Game Loop**: governa il ciclo di vita della partita gestendo la sequenza dei round e l'avanzamento dei turni, assicurando che lo scambio dei messaggi sul canale avvenga in modo fluido e sincronizzato.

Il flusso di interazione tra i componenti tramite canale è illustrato nel seguente diagramma di sequenza:

<pre class="mermaid">
sequenceDiagram
    autonumber
    participant UI as SwingView (EDT Thread)
    participant VM as ViewModel
    participant CH as GameMessagesChannel (Monitor)
    participant M as Model (Engine Thread)
    participant C as GameController

    Note over C,M: Controller avvia il ciclo di gioco
    C->>M: Avvio step / evento
    M->>CH: send(MessaggioSpecificoEvento)
    Note over M: Model si sospende in attesa sul canale
    CH->>VM: receive() consuma il messaggio
    VM->>UI: Aggiorna stato e componenti visuali
    UI-->>VM: Input utente (click / selezione)
    VM->>CH: send(MessaggioRispostaDedicato)
    CH->>M: receive() risveglia il Model
    M->>M: Calcola transizione di stato
    M->>CH: send(StatoAggiornato / Esito)
    CH->>VM: Notifica esito
    VM->>UI: Aggiorna / Renderizza esito a schermo
</pre>

<script type="module">
  import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs';
  mermaid.initialize({ startOnLoad: true, theme: 'neutral' });
</script>