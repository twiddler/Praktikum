class Knoten {

	Spielzustand zustand;
	Knoten nachfolger;
	int[] bewertung;
	Karte karte;

	public Knoten(Spielzustand zustand) {
		this.zustand = zustand;
	}

	/**
	 * Erzeugt einen Knoten mit dem Spielzustand, der erreicht wird wenn spieler
	 * karte spielt.
	 */
	Knoten kindMitKarte(int spieler, Karte karte) {
		Spielzustand neuerZustand = this.zustand.karteSpielen(this.zustand.roboter[spieler], karte);
		Knoten result = new Knoten(neuerZustand);
		result.karte = karte;
		result.zustand.roboter[spieler].karten.remove(karte);
		return result;
	}

}