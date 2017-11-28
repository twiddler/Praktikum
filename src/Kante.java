
/**
 * Jedes Feld hat 6 Kanten, die von einem speziellen Typ sein k�nnen (Schlucht,
 * Wand, Einbahnstra�e rein/raus, Laser). Diese Typen sind Unterklassen von
 * Kante.
 * 
 * @author xXx Players xXx
 *
 */
class Kante {

	/**
	 * Die Kante f�hrt ihre F�higkeit aus. Das ist nur interessant f�r den
	 * Laserstrahl. Deshalb wird das Feld der Kante und die Richtung der Kante
	 * mitgegeben.
	 */
	void ausfuehren(Feld feld, int richtung, Spielzustand zustand) {
	}

	/**
	 * Gibt zur�ck, ob �ber diese Kante das Feld betreten werden kann, und bringt
	 * den Roboter ggf. um. Bspw. w�rde eine Wand den Roboter nicht �bertreten
	 * lassen, und eine Schlucht zus�tzlich umbringen.
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
	 * Gibt zur�ck, ob �ber die Kante in das Feld ein Laser eintreten kann. �ber
	 * eine Wand w�re das bspw. nicht m�glich, und je nach Einbahnstra�e auch nicht.
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