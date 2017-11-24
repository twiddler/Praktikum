class Kante {

	void ausfuehren(Feld feld, int richtung, Spielzustand zustand) {
	}

	boolean eintreten(Roboter roboter) {
		return true;
	}

	boolean austreten(Roboter roboter) {
		return true;
	}

	boolean reinlaserbar() {
		return true;
	}

	boolean rauslaserbar() {
		return true;
	}
}

class Wand extends Kante {

	boolean eintreten(Roboter roboter) {
		return false;
	}

	boolean austreten(Roboter roboter) {
		return false;
	}

	boolean reinlaserbar() {
		return false;
	}

	boolean rauslaserbar() {
		return false;
	}

}

class Schlucht extends Kante {

	boolean eintreten(Roboter roboter) {
		roboter.zerstoeren();
		return false;
	}

	boolean austreten(Roboter roboter) {
		return this.eintreten(roboter);
	}

}

class Einbahn_rein extends Kante {

	boolean austreten(Roboter roboter) {
		return false;
	}

	boolean rauslaserbar() {
		return false;
	}

}

class Einbahn_raus extends Kante {

	boolean eintreten(Roboter roboter) {
		return false;
	}

	boolean reinlaserbar() {
		return false;
	}

}

class Laser extends Wand {

	void ausfuehren(Feld feld, int richtung, Spielzustand zustand) {
		feld.durchlasern(richtung, zustand);
	}

}
