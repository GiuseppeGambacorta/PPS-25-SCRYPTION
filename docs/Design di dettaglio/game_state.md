# GameState

Il **GameState** rappresenta lo snapshot completo e immutabile della partita in un dato istante temporale. È concepito come un mero contenitore di dati privo di comportamento attivo, aggrega tutte le informazioni necessarie a descrivere lo stato corrente della sessione di gioco (mazzo, inventario ed esito della partita).
 costituisce il punto di ingresso e di uscita deterministico per qualsiasi logica di gioco, dove ogni evento riceve in input lo stato corrente e restituisce il nuovo stato calcolato.