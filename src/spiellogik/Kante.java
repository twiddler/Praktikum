package spiellogik;

/**
 * Jedes Feld hat 6 Kanten, die von einem speziellen Typ sein k�nnen (Schlucht,
 * Wand, Einbahnstra�e rein/raus, Laser). Diese Typen sind Unterklassen von
 * Kante.
 * 
 * @author xXx Players xXx
 *
 */
public class Kante {

	/**
	 * Die Kante f�hrt ihre F�higkeit aus. Das ist nur interessant f�r den
	 * Laserstrahl. Deshalb wird das Feld der Kante und die Richtung der Kante
	 * mitgegeben.
	 */
	void ausfuehren(final Feld feld, final int richtung, final Spielzustand zustand) {
	}

	/**
	 * Gibt zur�ck, ob �ber diese Kante das Feld betreten werden kann, und bringt
	 * den Roboter ggf. um. Bspw. w�rde eine Wand den Roboter nicht �bertreten
	 * lassen, und eine Schlucht zus�tzlich umbringen.
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