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
			if (this.zerstoert()) {
				return;
			}

			final Feld feld = zustand.feldAufPosition(this.position);
			if (feld.verlassen(this, this.blickrichtung, zustand)) {
				zustand.feldAufPosition(feld.nachbarn[this.blickrichtung]).betreten(this, zustand);
			}
		}
	}

	void zerstoeren() {
		if (!this.zerstoert()) {
			this.gesundheit = 0;
			--this.leben;
		}
	}

	boolean zerstoert() {
		return this.gesundheit == 0;
	}

	/**
	 * Dekrementiert das Leben des Roboters, setzt seine Gesundheitspunkte auf die
	 * nach einem Respawn, und stellt ihn zur zuletzt erreichten Flagge bzw. auf das
	 * Startfeld.
	 */
	void respawn(final Spielzustand zustand) {
		if (this.leben == 0) {
			return;
		}

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
		return super.stehtAufPosition(position) && !this.zerstoert();
	}

	/**
	 * Verringert die Gesundheit um 1, und zerstört falls sie dann 0 ist.
	 */
	void schaedigen() {
		--this.gesundheit;
		if (this.gesundheit <= 0) {
			this.zerstoeren();
		}
	}

	/**
	 * Erhöht die Gesundheit um 1 (höchstens bis auf das Maximum).
	 */
	void reparieren() {
		++this.gesundheit;
		if (this.gesundheit > Parameter.MAX_GESUNDHEIT) {
			this.gesundheit = Parameter.MAX_GESUNDHEIT;
		}
	}

	void erhalteGeld() {
		this.geld += Parameter.ZAHLTAG_GEHALT;
	}

	/**
	 * Feuert den Laser des Roboters falls er noch lebt.
	 */
	void lasern(final Spielzustand zustand) {
		if (!this.zerstoert()) {
			final Feld feld = zustand.feldAufPosition(this.position);
			final Feld nachbar = zustand.feldAufPosition(feld.nachbarn[this.blickrichtung]);
			if (feld.kanteInRichtung(this.blickrichtung).rauslaserbar()
					&& nachbar.kanteInRichtung((this.blickrichtung + 3) % 6).reinlaserbar()) {
				nachbar.durchlasern(this.blickrichtung, zustand, false);
			}
		}
	}

	/**
	 * Ermittelt welche Karten im Programmslot gespielt werden können. Je nach
	 * Gesundheitspunkten müssen die gesperrten Karten betrachtet werden.
	 */
	public List<Karte> spielbareKarten(int slot) {
		List<Karte> result;
		int gesperrteKarten = this.gesperrteKarten.size();
		if (this.poweredDown || this.leben == 0) {
			result = new ArrayList<Karte>(1);
			result.add(Karte.dummy);
		} else if (slot + gesperrteKarten < Parameter.ZUEGE_PRO_RUNDE) {
			result = this.karten;
		} else {
			result = new ArrayList<Karte>(1);
			result.add(this.gesperrteKarten.get(slot + gesperrteKarten - Parameter.ZUEGE_PRO_RUNDE));
		}
		return result;
	}

}
