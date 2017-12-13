package spiellogik;

public final class Zahltag extends Zusatz {

	private final int auszahlung;

	public Zahltag(int auszahlung) {
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