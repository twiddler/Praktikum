package spiellogik;

import java.util.ArrayList;
import java.util.List;

/**
 * Die Roboter.
 * 
 * @author xXx Players xXx
 *
 */
public final class Roboter extends Bewegbar implements Cloneable {

	/**
	 * Feld, auf dem der Roboter steht, referenziert durch dessen Position.
	 */
	int blickrichtung;
	public int leben;
	public int gesundheit;
	public int geld;
	boolean zerstoert = false;
	public List<Karte> karten;
	public List<Karte> gesperrteKarten;
	public boolean poweredDown = false;

	/**
	 * Die nächste zu berührende Flagge. Die erste Flagge hat die Nummer 0.
	 */
	public int naechsteFlagge;
	boolean virtuell;

	public Roboter(final int position, final int blickrichtung, final int leben, final int gesundheit, final int geld,
			final int naechsteFlagge, boolean virtuell, List<Karte> karten, List<Karte> gesperrteKarten) {
		this.position = position;
		this.blickrichtung = blickrichtung;
		this.leben = leben;
		this.gesundheit = gesundheit;
		this.geld = geld;
		this.naechsteFlagge = naechsteFlagge;
		this.virtuell = virtuell;
		this.karten = karten;
		this.gesperrteKarten = gesperrteKarten;
	}

	@Override
	public Roboter clone() {
		Roboter result = null;
		try {
			result = (Roboter) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		result.karten = new ArrayList<Karte>();
		for (final Karte karte : this.karten) {
			result.karten.add(karte.clone());
		}

		result.gesperrteKarten = new ArrayList<Karte>();
		for (final Karte karte : this.gesperrteKarten) {
			result.gesperrteKarten.add(karte.clone());
		}

		return result;
	}

	void drehen(final int drehung) {
		this.blickrichtung = (drehung + this.blickrichtung) % 6;
	}

	/**
	 * Der Roboter soll schritte-mal nach vorne laufen. Dazu soll er den Feldern
	 * mitteilen, dass er sie verlässt und betritt. Falls er zwischendurch stirbt,
	 * soll er nicht mehr laufen.
	 */
	void laufen(final int schritte, final Spielzustand zustand) {
		for (int i = 0; i < schritte; ++i) {
			if (zerstoert) {
				return;
			}

			final Feld feld = zustand.feldAufPosition(this.position);
			if (feld.verlassen(this, this.blickrichtung, zustand)) {
				zustand.feldAufPosition(feld.nachbarn[this.blickrichtung]).betreten(this, zustand);
			}
		}
	}

	/**
	 * Dekrementiert das Leben des Roboters, setzt seine Gesundheitspunkte auf die
	 * nach einem Respawn, und stellt ihn zur zuletzt erreichten Flagge bzw. auf das
	 * Startfeld.
	 */
	void zerstoeren(final Spielzustand zustand) {
		if (!this.zerstoert) {
			this.zerstoert = true;
			--this.leben;
		}
	}

	void respawn(final Spielzustand zustand) {
		if (this.leben == 0) {
			return;
		}

		this.zerstoert = false;
		this.gesundheit = Parameter.MAX_GESUNDHEIT_NACH_TOD;
		this.virtuell = true;

		// An die letzte Flagge bzw. Spielfeldmitte stellen
		if (this.naechsteFlagge == 0) {
			this.position = 0;
		} else {
			this.position = zustand.flaggen[this.naechsteFlagge].position;
		}
	}

	@Override
	boolean stehtAufPosition(final int position) {
		return super.stehtAufPosition(position) && !this.zerstoert;
	}

	/**
	 * Verringert die aktuelle Gesundheit um 1. Bei 0 soll der Roboter zerstört
	 * werden.
	 */
	void gesundheitVerringern(final Spielzustand zustand) {
		--this.gesundheit;
		if (this.gesundheit <= 0) {
			this.zerstoeren(zustand);
		}
	}

	void reparieren() {
		this.gesundheit += Parameter.REPARATUR_BRINGT_GESUNDHEIT;
		if (this.gesundheit > Parameter.MAX_GESUNDHEIT) {
			this.gesundheit = Parameter.MAX_GESUNDHEIT;
		}
	}

	void erhalteGeld(final int geld) {
		this.geld += geld;
	}

	/**
	 * Feuert den Laser des Roboters.
	 */
	void lasern(final Spielzustand zustand) {
		final Feld feld = zustand.feldAufPosition(this.position);
		final Feld nachbar = zustand.feldAufPosition(feld.nachbarn[this.blickrichtung]);
		if (feld.kanteInRichtung(this.blickrichtung).rauslaserbar()
				&& nachbar.kanteInRichtung((this.blickrichtung + 3) % 6).reinlaserbar()) {
			nachbar.durchlasern(this.blickrichtung, zustand, false);
		}
	}

	/**
	 * Ermittelt welche Karten im Programmslot gespielt werden können. Bei vollem
	 * Leben sind das alles, ansonsten müssen die gesperrten Karten betrachtet
	 * werden.
	 */
	public List<Karte> spielbareKarten(int slot) {
		List<Karte> result;
		int geblockteKarten = this.gesperrteKarten.size();
		if (this.poweredDown || this.leben == 0) {
			result = new ArrayList<Karte>();
			result.add(Karte.dummy);
		} else if (Parameter.ZUEGE_PRO_RUNDE - slot > geblockteKarten) {
			result = this.karten;
		} else {
			result = new ArrayList<Karte>();
			result.add(this.gesperrteKarten.get(slot - geblockteKarten - 1));
		}
		return result;
	}

}
