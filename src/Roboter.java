
/**
 * Die Roboter.
 * 
 * @author xXx Players xXx
 *
 */
class Roboter implements Cloneable {

	/**
	 * Feld, auf dem der Roboter steht, referenziert durch dessen Position.
	 */
	int position;
	int blickrichtung;
	int leben;
	int gesundheit;
	int geld;
	boolean zerstoert = false;

	Roboter(int position, int blickrichtung, int leben, int gesundheit, int geld) {
		this.position = position;
		this.blickrichtung = blickrichtung;
		this.leben = leben;
		this.gesundheit = gesundheit;
		this.geld = geld;
	}

	@Override
	public Roboter clone() {
		Roboter result = null;
		try {
			result = (Roboter) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return result;
	}

	void drehen(int drehung) {
		this.blickrichtung = (drehung + this.blickrichtung) % 6;
	}

	/**
	 * Der Roboter soll schritte-mal nach vorne laufen. Dazu soll er den Feldern
	 * mitteilen, dass er sie verlässt und betritt. Falls er zwischendurch stirbt,
	 * soll er nicht mehr laufen.
	 */
	void laufen(int schritte, Spielzustand zustand) {
		for (int i = 0; i < schritte; ++i) {
			if (zerstoert) {
				return;
			}

			Feld feld = zustand.feldAufPosition(position);
			if (feld.verlassen(this, blickrichtung)) {
				zustand.feldAufPosition(feld.nachbarn[position]).betreten(this);
			}
		}
	}

	/**
	 * Dekrementiert das Leben des Roboters, setzt seine Gesundheitspunkte auf die
	 * nach einem Respawn, und stellt ihn zur zuletzt erreichten Flagge bzw. auf das
	 * Startfeld.
	 */
	void zerstoeren() {
		this.zerstoert = true;
		--this.leben;
		this.gesundheit = Parameter.MAX_GESUNDHEIT_NACH_TOD;

		// TODO: Roboter respawnen bei letzter Flagge oder Start
	}

	/**
	 * Verringert die aktuelle Gesundheit um 1. Bei 0 soll der Roboter zerstört
	 * werden.
	 */
	void gesundheitVerringern() {
		--this.gesundheit;
		if (this.gesundheit <= 0) {
			this.zerstoeren();
		}
	}

	void reparieren(int gesundheit) {
		this.gesundheit += gesundheit;
		if (this.gesundheit > Parameter.MAX_GESUNDHEIT) {
			this.gesundheit = Parameter.MAX_GESUNDHEIT;
		}
	}

	void erhalteGeld(int geld) {
		this.geld += geld;
	}

	/**
	 * Feuert den Laser des Roboters.
	 */
	void lasern(Spielzustand zustand) {
		Feld feld = zustand.feldAufPosition(this.position);
		int richtung = this.blickrichtung;
		Feld nachbar = zustand.feldAufPosition(feld.nachbarn[richtung]);
		if (feld.kanteInRichtung(richtung).rauslaserbar()
				&& nachbar.kanteInRichtung((richtung + 3) % 6).reinlaserbar()) {
			nachbar.durchlasern(richtung, zustand);
		}
	}

	boolean stehtAufPosition(int position) {
		return this.position == position;
	}

}
