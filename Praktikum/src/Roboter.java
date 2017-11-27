class Roboter implements Cloneable {

	int position;
	int blickrichtung;
	int leben;
	int gesundheit;
	int geld;
	boolean wurdeZerstoert = false;

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

	void laufen(int schritte, Spielzustand zustand) {
		for (int i = 0; i < schritte; ++i) {
			if (wurdeZerstoert) {
				return;
			}

			Feld feld = zustand.feldAufPosition(position);
			if (feld.verlassen(this, blickrichtung)) {
				zustand.feldAufPosition(feld.nachbarn[position]).betreten(this);
			}
		}
	}

	void zerstoeren() {
		this.wurdeZerstoert = true;
		--this.leben;
		this.gesundheit = Parameter.MAX_GESUNDHEIT_NACH_TOD;

		// TODO: Roboter respawnen bei letzter Flagge oder Start
	}

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
