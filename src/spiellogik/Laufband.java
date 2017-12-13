package spiellogik;

public final class Laufband extends Feld {

	private int richtung;

	public Laufband(final int[] nachbarn, final Kante[] kanten, final Zusatz zusatz, final int drehung, final int position,
			final int richtung) {
		super(nachbarn, kanten, zusatz, drehung, position);
		this.richtung = richtung;
	}

	@Override
	void ausfuehren(final Spielzustand zustand) {
		for (final Roboter r : zustand.roboter) {
			if (r.stehtAufPosition(this.position)) {
				if (this.verlassen(r, richtung, zustand)) {
					zustand.feldAufPosition(this.nachbarn[richtung]).betreten(r, zustand);
				}
			}
		}

		for (final Flagge flagge : zustand.flaggen) {
			if (flagge.stehtAufPosition(this.position)) {
				if (this.verlassen(flagge, richtung, zustand)) {
					zustand.feldAufPosition(this.nachbarn[richtung]).betreten(flagge, zustand);
				}
			}
		}
	}

}