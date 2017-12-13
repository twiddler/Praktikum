package spiellogik;

public class Mauer extends Kante {

	@Override
	final boolean eintreten(final Bewegbar b, final Spielzustand zustand) {
		return false;
	}

	@Override
	final boolean austreten(final Bewegbar b, final Spielzustand zustand) {
		return false;
	}

	@Override
	final boolean reinlaserbar() {
		return false;
	}

	@Override
	final boolean rauslaserbar() {
		return false;
	}

}