class Knoten {

	Spielzustand zustand;
	Knoten nachfolger;
	int[] bewertung;
	Karte karte;
	int spieler;

	Knoten(Spielzustand zustand) {
		this.zustand = zustand;
	}

	Knoten(Spielzustand zustand, int spieler, Karte karte) {
		this(zustand);
		this.karte = karte;
		this.spieler = spieler;
		this.zustand.roboter[spieler].karten.remove(karte);
	}

	/**
	 * Erzeugt einen Knoten mit dem Spielzustand, der erreicht wird wenn spieler
	 * karte spielt.
	 */
	Knoten kindMitKarte(int spieler, Karte karte) {
		Spielzustand neuerZustand = this.zustand.karteSpielen(spieler, karte);
		return new Knoten(neuerZustand, spieler, karte);
	}

}