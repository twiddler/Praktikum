package spiellogik;

public final class Loch extends Feld {

	public Loch(final int[] nachbarn, final Kante[] kanten, final Zusatz zusatz, final int position) {
		super(nachbarn, kanten, zusatz, position);
	}

	@Override
	void betreten(final Bewegbar b, final Spielzustand zustand) {
		super.betreten(b, zustand);
		if (b instanceof Roboter) {
			((Roboter) b).zerstoeren(zustand);
		}
	}

}