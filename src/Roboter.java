import java.util.ArrayList;

/**
 * Die Roboter.
 * 
 * @author xXx Players xXx
 *
 */
final class Roboter extends Bewegbar implements Cloneable {

	/**
	 * Feld, auf dem der Roboter steht, referenziert durch dessen Position.
	 */
	int blickrichtung;
	int leben;
	int gesundheit;
	int geld;
	boolean zerstoert = false;
	ArrayList<Karte> karten;
	int letzteFlagge;

	Roboter(final int position, final int blickrichtung, final int leben, final int gesundheit, final int geld, final int letzteFlagge) {
		this.position = position;
		this.blickrichtung = blickrichtung;
		this.leben = leben;
		this.gesundheit = gesundheit;
		this.geld = geld;
		this.letzteFlagge = letzteFlagge;
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
		for(int i = 0; i < this.karten.size(); i++) {
			result.karten.add(this.karten.get(i).clone());
		}
		
		return result;
	}

	void drehen(final int drehung) {
		this.blickrichtung = (drehung + this.blickrichtung) % 6;
	}

	/**
	 * Der Roboter soll schritte-mal nach vorne laufen. Dazu soll er den Feldern
	 * mitteilen, dass er sie verl�sst und betritt. Falls er zwischendurch stirbt,
	 * soll er nicht mehr laufen.
	 */
	void laufen(final int schritte, final Spielzustand zustand) {
		for (int i = 0; i < schritte; ++i) {
			if (zerstoert) {
				return;
			}

			Feld feld = zustand.feldAufPosition(position);
			if (feld.verlassen(this, blickrichtung, zustand)) {
				zustand.feldAufPosition(feld.nachbarn[position]).betreten(this, zustand);
			}
		}
	}

	/**
	 * Dekrementiert das Leben des Roboters, setzt seine Gesundheitspunkte auf die
	 * nach einem Respawn, und stellt ihn zur zuletzt erreichten Flagge bzw. auf das
	 * Startfeld.
	 */
	void zerstoeren(final Spielzustand zustand) {
		this.zerstoert = true;
		--this.leben;
		this.gesundheit = Parameter.MAX_GESUNDHEIT_NACH_TOD;

		if(this.letzteFlagge == -1) {
			this.position = 0;
		} else {
			this.position = zustand.flaggen[letzteFlagge].position;
		}
	}

	/**
	 * Verringert die aktuelle Gesundheit um 1. Bei 0 soll der Roboter zerst�rt
	 * werden.
	 */
	void gesundheitVerringern(final Spielzustand zustand) {
		--this.gesundheit;
		if (this.gesundheit <= 0) {
			this.zerstoeren(zustand);
		}
	}

	void reparieren(final int gesundheit) {
		this.gesundheit += gesundheit;
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
			nachbar.durchlasern(this.blickrichtung, zustand);
		}
	}

}
