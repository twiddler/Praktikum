package spiellogik;

public final class Drehfeld extends Feld {

	private int drehungUm;

	public Drehfeld(final int[] nachbarn, final Kante[] kanten, final Zusatz zusatz, final int position,
			final int drehungUm) {
		super(nachbarn, kanten, zusatz, position);
		this.drehungUm = drehungUm;
	}

	@Override
	void ausfuehren(final Spielzustand zustand) {
		this.drehen(this.drehungUm, zustand.roboter);
	}

}