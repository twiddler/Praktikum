class Roboter {

	int position;
	int blickrichtung;
	int leben;
	int gesundheit;
	int geld;
	boolean wurdeZerstoert = false;

	public Roboter(Roboter roboter) {
		this.position = roboter.position;
		this.blickrichtung = roboter.blickrichtung;
		this.leben = roboter.leben;
		this.gesundheit = roboter.gesundheit;
		this.geld = roboter.geld;
		this.wurdeZerstoert = roboter.wurdeZerstoert;
	}

	public void drehen(int drehung) {
		this.blickrichtung = (drehung + this.blickrichtung) % 6;
	}

	public void laufen(int schritte, Spielzustand zustand) {
		for (int i = 0; i < schritte; ++i) {
			if (wurdeZerstoert) {
				return;
			}

			Feld feld = zustand.feldAnPosition(position);
			if (feld.verlassen(this, blickrichtung)) {
				zustand.feldAnPosition(feld.nachbarn[position]).betreten(this);
			}
		}
	}

	public void zerstoeren() {
		this.wurdeZerstoert = true;
		this.leben--;
		this.gesundheit = Parameter.MAX_HEALTH_AFTER_DEATH;

		// to do
	}

	public void gesundheitVerringern() {
		--this.gesundheit;
		if (this.gesundheit <= 0) {
			this.zerstoeren();
		}
	}

	public void reparieren(int heal) {
		this.gesundheit += heal;
		if (this.gesundheit > Parameter.MAX_HEALTH) {
			this.gesundheit = Parameter.MAX_HEALTH;
		}
	}

	public void setzeAufFeld(int position) {
		this.position = position;
	}

	public void erhalteGeld(int geld) {
		this.geld += geld;
	}

	public void lasern(Spielzustand zustand) {
		Feld feld = zustand.feldAnPosition(this.position);
		int richtung = this.blickrichtung;
		Feld nachbar = zustand.feldAnPosition(feld.nachbarn[richtung]);
		if (feld.kanteInRichtung(richtung).rauslaserbar()
				&& nachbar.kanteInRichtung((richtung + 3) % 6).reinlaserbar()) {
			nachbar.durchlasern(richtung, zustand);
		}
	}

}
