package entscheidungen;

import spiellogik.Karte;
import spiellogik.Spielzustand;

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

	Knoten(final Spielzustand zustand) {
		this.zustand = zustand;
	}

	Knoten(final Spielzustand zustand, final int spieler, final Karte karte) {
		this(zustand);
		this.karte = karte;
		this.spieler = spieler;
	}

	/**
	 * Erzeugt einen Knoten mit dem Spielzustand, der erreicht wird wenn spieler
	 * karte spielt.
	 */
	Knoten kindMitKarte(final int spieler, final Karte karte) {
		return new Knoten(this.zustand.karteSpielen(spieler, karte), spieler, karte);
	}

}