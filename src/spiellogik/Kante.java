package spiellogik;

/**
 * Jedes Feld hat 6 Kanten, die von einem speziellen Typ sein können (Schlucht,
 * Wand, Einbahnstraße rein/raus, Laser). Diese Typen sind Unterklassen von
 * Kante.
 * 
 * @author xXx Players xXx
 *
 */
public class Kante {

	/**
	 * Die Kante führt ihre Fähigkeit aus. Das ist nur interessant für den
	 * Laserstrahl. Deshalb wird das Feld der Kante und die Richtung der Kante
	 * mitgegeben.
	 */
	void ausfuehren(final Feld feld, final int richtung, final Spielzustand zustand) {
	}

	/**
	 * Gibt zurück, ob über diese Kante das Feld betreten werden kann, und bringt
	 * den Roboter ggf. um. Bspw. würde eine Wand den Roboter nicht übertreten
	 * lassen, und eine Schlucht zusätzlich umbringen.
	 */
	boolean eintreten(final Bewegbar b, final Spielzustand zustand) {
		return true;
	}

	/**
	 * Analog zu eintreten().
	 */
	boolean austreten(final Bewegbar b, final Spielzustand zustand) {
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