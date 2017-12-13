package spiellogik;

public final class Schlucht extends Kante {

	@Override
	boolean eintreten(final Bewegbar b, final Spielzustand zustand) {
		if (b instanceof Roboter) {
			((Roboter) b).zerstoeren(zustand);
		}
		return false;
	}

	@Override
	boolean austreten(final Bewegbar b, final Spielzustand zustand) {
		return this.eintreten(b, zustand);
	}

}