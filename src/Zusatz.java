
/**
 * Feldzus�tze in der Mitte von Spielfeldern, also Zahltage und Pressen.
 * 
 * @author xXx Players xXx
 *
 */

class Zusatz {

	/**
	 * F�hrt den Feldzusatz aus.
	 */
	void ausfuehren(int position, Spielzustand zustand) {
	}

}

class Zahltag extends Zusatz {

	private int auszahlung;

	Zahltag(int auszahlung) {
		this.auszahlung = auszahlung;
	}

	@Override
	void ausfuehren(int position, Spielzustand zustand) {
		for (Roboter r : zustand.roboter) {
			if (r.stehtAufPosition(position)) {
				r.erhalteGeld(this.auszahlung);
			}
		}
	}

}

class Presse extends Zusatz {

	private boolean[] aktivInZuegen;

	Presse(boolean[] aktivInZuegen) {
		this.aktivInZuegen = aktivInZuegen;
	}

	@Override
	void ausfuehren(int position, Spielzustand zustand) {
		if (aktivInZuegen[zustand.zug]) {
			for (Roboter r : zustand.roboter) {
				if (r.stehtAufPosition(position)) {
					r.zerstoeren();
				}
			}
		}
	}

}