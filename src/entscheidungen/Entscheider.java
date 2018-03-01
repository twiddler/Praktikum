package entscheidungen;

import spiellogik.Karte;
import spiellogik.Spielzustand;

public abstract class Entscheider {

	Bewerter bewerter;

	public Entscheider(final Bewerter bewerter) {
		this.bewerter = bewerter;
	}

	/**
	 * Gegeben einen Spielzustand, gibt uns diese Funktion die Karten, die wir
	 * spielen sollen.
	 */
	public abstract Karte[] entscheiden(final Spielzustand zustand);

	public abstract boolean powerdown(final Spielzustand zustand);
}