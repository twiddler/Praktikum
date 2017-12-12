class Knoten {

	Spielzustand zustand;
	Knoten nachfolger;
	int[] bewertung;
	Karte karte;
	/**
	 * Der Spieler, der diesen Knoten herbeigeführt, also als letztes eine Karte
	 * gespielt hat.
	 */
	int spieler;

	Knoten(Spielzustand zustand) {
		this.zustand = zustand;
	}

	Knoten(Spielzustand zustand, int spieler, Karte karte) {
		this(zustand);
		this.karte = karte;
		this.spieler = spieler;
	}

	/**
	 * Erzeugt einen Knoten mit dem Spielzustand, der erreicht wird wenn spieler
	 * karte spielt.
	 */
	Knoten kindMitKarte(int spieler, Karte karte) {
		return new Knoten(this.zustand.karteSpielen(spieler, karte), spieler, karte);
	}

}