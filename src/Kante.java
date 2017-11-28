
/**
 * Jedes Feld hat 6 Kanten, die von einem speziellen Typ sein können (Schlucht,
 * Wand, Einbahnstraße rein/raus, Laser). Diese Typen sind Unterklassen von
 * Kante.
 * 
 * @author xXx Players xXx
 *
 */
class Kante {

	/**
	 * Die Kante führt ihre Fähigkeit aus. Das ist nur interessant für den
	 * Laserstrahl. Deshalb wird das Feld der Kante und die Richtung der Kante
	 * mitgegeben.
	 */
	void ausfuehren(Feld feld, int richtung, Spielzustand zustand) {
	}

	/**
	 * Gibt zurück, ob über diese Kante das Feld betreten werden kann, und bringt
	 * den Roboter ggf. um. Bspw. würde eine Wand den Roboter nicht übertreten
	 * lassen, und eine Schlucht zusätzlich umbringen.
	 */
	boolean eintreten(Roboter roboter) {
		return true;
	}

	/**
	 * Analog zu eintreten().
	 */
	boolean austreten(Roboter roboter) {
		return true;
	}

	/**
	 * Gibt zurück, ob über die Kante in das Feld ein Laser eintreten kann. Über
	 * eine Wand wäre das bspw. nicht möglich, und je nach Einbahnstraße auch nicht.
	 */
	boolean reinlaserbar() {
		return true;
	}

	/**
	 * Analog zu reinlaserbar().
	 */
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