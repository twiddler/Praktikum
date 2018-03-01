package spiellogik;

public final class Reparaturfeld extends Feld {

	public Reparaturfeld(final int[] nachbarn, final Kante[] kanten, final Zusatz zusatz, final int position) {
		super(nachbarn, kanten, zusatz, position);
	}

	@Override
	void ausfuehren(final Spielzustand zustand) {
		for (final Roboter r : zustand.roboter) {
			if (r.stehtAufPosition(this.position)) {
				r.reparieren();
			}
		}
	}

}