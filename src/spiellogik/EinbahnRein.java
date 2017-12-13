package spiellogik;

public final class EinbahnRein extends Kante {

	@Override
	boolean austreten(final Bewegbar b, final Spielzustand zustand) {
		return false;
	}

	@Override
	boolean rauslaserbar() {
		return false;
	}

}