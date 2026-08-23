# Deck e PlayerHand

Le entità che raggruppano insiemi di carte, nello specifico il mazzo principale e la mano del giocatore, sono state modellate privilegiando un forte incapsulamento e la rigida immutabilità dello stato. Pur essendo concettualmente delle semplici collezioni sequenziali di carte, il design nasconde questa natura al resto del dominio per garantire maggiore sicurezza strutturale.

*   Le operazioni di manipolazione logica (come l'aggiunta, la rimozione o la pescata) operano in modo puramente funzionale: non alterano mai la collezione originale in memoria, ma restituiscono sempre una nuova istanza aggiornata e coerente.
*   Il sistema modella la fonte dinamica di pescata tramite un aggregatore specifico, che incapsula il mazzo principale e offre parallelamente l'accesso a fonti di risorse inesauribili (come il mazzo dedicato agli Scoiattoli).

Questo livello di astrazione fornisce un'importante garanzia di sicurezza semantica: il design impedisce che componenti esterne possano confondere o scambiare accidentalmente un mazzo con una mano, pur condividendo la stessa natura concettuale. Inoltre, l'assenza di side-effects assicura che ogni pescata generi un nuovo stato matematicamente prevedibile, facilitando notevolmente il tracciamento dei cambiamenti da parte del livello di presentazione grafica.