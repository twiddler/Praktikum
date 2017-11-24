class Spielzustand {

	Roboter[] roboter;
	private Feld[] felder;

	public Spielzustand(Spielzustand zustand) {
		this.roboter = new Roboter[2];
		for (int i = 0; i < roboter.length; ++i) {
			this.roboter[i] = new Roboter(zustand.roboter[i]);
		}

		this.felder = new Feld[zustand.felder.length];
		for (int i = 0; i < zustand.felder.length; i++) {
			this.felder[i] = zustand.felder[i];
		}
	}

	// public Spielzustand karteSpielen(Karte karte) {}

	Spielzustand sonderfelderAusfuehren() {
		
		// todo
		
		Spielzustand result = new Spielzustand(this);
		for (Feld feld : result.felder) {
			feld.ausfuehren(this);
		}
		return result;
	}

	Spielzustand roboterlaserFeuern() {
		Spielzustand result = new Spielzustand(this);
		for (int i = 0; i < result.roboter.length; ++i) {
			result.roboter[i].lasern(this);
		}
		return result;
	}

	Feld feldAnPosition(int position) {
		return this.felder[position];
	}
}
