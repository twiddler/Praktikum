
/**
 * Feldzusätze in der Mitte von Spielfeldern, also Zahltage und Pressen.
 * 
 * @author xXx Players xXx
 *
 */

class Zusatz {

	/**
	 * Führt den Feldzusatz aus.
	 */
	void ausfuehren(final int position, final Spielzustand zustand) {
	}

}

final class Zahltag extends Zusatz {

	private final int auszahlung;

	Zahltag(int auszahlung) {
		this.auszahlung = auszahlung;
	}

	@Override
	void ausfuehren(final int position, final Spielzustand zustand) {
		for (final Roboter r : zustand.roboter) {
			if (r.stehtAufPosition(position)) {
				r.erhalteGeld(this.auszahlung);
			}
		}
	}

}

final class Presse extends Zusatz {

	private final boolean[] aktivInZuegen;

	Presse(final boolean[] aktivInZuegen) {
		this.aktivInZuegen = aktivInZuegen;
	}

	@Override
	void ausfuehren(final int position, final Spielzustand zustand) {
		if (aktivInZuegen[zustand.zug]) {
			for (final Roboter r : zustand.roboter) {
				if (r.stehtAufPosition(position)) {
					r.zerstoeren();
				}
			}
		}
	}

}