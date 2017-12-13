package spiellogik;

public final class Reparaturfeld extends Feld {

	private int gesundheit;

	public Reparaturfeld(final int[] nachbarn, final Kante[] kanten, final Zusatz zusatz, final int drehung,
			final int position, final int gesundheit) {
		super(nachbarn, kanten, zusatz, drehung, position);
		this.gesundheit = gesundheit;
	}

	@Override
	void ausfuehren(final Spielzustand zustand) {
		for (final Roboter r : zustand.roboter) {
			if (r.stehtAufPosition(this.position)) {
				r.reparieren(this.gesundheit);
			}
		}
	}

}