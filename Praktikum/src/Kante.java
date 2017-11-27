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

	@Override
	boolean eintreten(Roboter roboter) {
		return false;
	}

	@Override
	boolean austreten(Roboter roboter) {
		return false;
	}

	@Override
	boolean reinlaserbar() {
		return false;
	}

	@Override
	boolean rauslaserbar() {
		return false;
	}

}

class Schlucht extends Kante {

	@Override
	boolean eintreten(Roboter roboter) {
		roboter.zerstoeren();
		return false;
	}

	@Override
	boolean austreten(Roboter roboter) {
		return this.eintreten(roboter);
	}

}

class Einbahn_rein extends Kante {

	@Override
	boolean austreten(Roboter roboter) {
		return false;
	}

	@Override
	boolean rauslaserbar() {
		return false;
	}

}

class Einbahn_raus extends Kante {

	@Override
	boolean eintreten(Roboter roboter) {
		return false;
	}

	@Override
	boolean reinlaserbar() {
		return false;
	}

}

class Laser extends Wand {

	@Override
	void ausfuehren(Feld feld, int richtung, Spielzustand zustand) {
		feld.durchlasern((richtung + 3) % 6, zustand);
	}

}