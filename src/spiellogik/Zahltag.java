package spiellogik;

public final class Zahltag extends Zusatz {

	@Override
	void ausfuehren(final int position, final Spielzustand zustand) {
		for (final Roboter r : zustand.roboter) {
			if (r.stehtAufPosition(position)) {
				r.erhalteGeld();
			}
		}
	}

}